package de.pfadfinden.mv.command;

import de.pfadfinden.mv.ldap.schema.Gruppe;
import de.pfadfinden.mv.ldap.schema.IcaGruppierung;
import de.pfadfinden.mv.model.IcaIdentitaet;
import de.pfadfinden.mv.model.SyncBerechtigungsgruppe;
import de.pfadfinden.mv.service.IcaService;
import de.pfadfinden.mv.service.LdapEntryService;
import de.pfadfinden.mv.service.SyncService;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.AddResponse;
import org.apache.directory.api.ldap.model.message.DeleteResponse;
import org.apache.directory.api.ldap.model.message.ModifyResponse;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Component
@Order(20)
public class CommandGruppen implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(CommandGruppen.class);

    private final CommandIdentitaet commandIdentitaet;
    private final IcaService icaService;
    private final SyncService syncService;
    private final LdapEntryService ldapEntryService;
    private final LdapConnectionTemplate ldapConnectionTemplate;

    public CommandGruppen(CommandIdentitaet commandIdentitaet,
                          IcaService icaService,
                          SyncService syncService,
                          LdapEntryService ldapEntryService,
                          LdapConnectionTemplate ldapConnectionTemplate) {
        this.commandIdentitaet = commandIdentitaet;
        this.icaService = icaService;
        this.syncService = syncService;
        this.ldapEntryService = ldapEntryService;
        this.ldapConnectionTemplate = ldapConnectionTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        this.syncService.getSyncGruppen().forEach(this::execBerechtigungsgruppe);
    }

    private void execBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe){
        logger.info("## Berechtigungsgruppe #{} '{}' ##",berechtigungsgruppe.getId(),berechtigungsgruppe.getTitle());
        berechtigungsgruppe.setTaetigkeiten(syncService.getTaetigkeitenZuBerechtigungsgruppe(berechtigungsgruppe));
        Set<IcaIdentitaet> identitaeten = icaService.findIdentitaetByBerechtigungsgruppe(berechtigungsgruppe);

        logger.info("Berechtigungsgruppe #{} hat {} Identitaeten in MV.",
                berechtigungsgruppe.getId(),
                identitaeten.size());

        Optional<Gruppe> gruppe = ldapEntryService.findGruppeById(berechtigungsgruppe.getId());

        if(!gruppe.isPresent()){
            if (identitaeten.size() != 0) {
                logger.info("Berechtigungsgruppe in LDAP nicht vorhanden, Anlage erforderlich.");
                addBerechtigungsgruppe(berechtigungsgruppe, identitaeten);
            } else {
                logger.info("Berechtigungsgruppe in LDAP nicht vorhanden, keine Anlage da 0 Identitaeten.");
            }
        } else {
            logger.info("Berechtigungsgruppe in LDAP bereits vorhanden: {}",gruppe.get().getDn());
            if (identitaeten.size() == 0) {
                logger.warn("Berechtigungsgruppe '{}' ohne TaetigkeitsAssignment in MV.", berechtigungsgruppe.getTitle());
                deleteBerechtigungsgruppe(gruppe.get());
            } else {
                updateBerechtigungsgruppe(gruppe.get(),identitaeten);
            }
        }
    }

    private void addBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe, Set<IcaIdentitaet> identitaeten){
        Optional<IcaGruppierung> ownerGruppierung = ldapEntryService.findIcaGruppierungById(berechtigungsgruppe.getOwnerGruppierung());

        Dn parentDn = (berechtigungsgruppe.getOwnerGruppierung()!=0 && ownerGruppierung.isPresent()) ?
                ownerGruppierung.get().getDn() : this.ldapEntryService.getBaseDn();
        Dn baseDn = this.getBaseDn(parentDn,berechtigungsgruppe);
        if (baseDn == null) return;

        AddResponse addResponse = this.ldapConnectionTemplate.add(
                baseDn,
                request -> {
                    Entry entry = request.getEntry();
                    entry.add("ObjectClass","groupOfNames", "icaBerechtigung", "icaRecord");
                    entry.add("icaId",String.valueOf(berechtigungsgruppe.getId()));
                    entry.add("icaLastUpdated",new GeneralizedTime(new Date()).toString());

                    if(berechtigungsgruppe.getDescription()!=null) entry.add("description",berechtigungsgruppe.getDescription());
                    if(ownerGruppierung.isPresent()) entry.add("icaGruppierungId",String.valueOf(berechtigungsgruppe.getOwnerGruppierung()));

                    for(IcaIdentitaet identitaet: identitaeten) {
                        logger.debug("Identitaet #{} muss hinzugefuegt werden", identitaet.getId());
                        try{
                            entry.add("member",commandIdentitaet.identitaet2Ldap(identitaet.getId()).getDn().getName());
                        } catch (Exception e){
                            logger.warn("Hinzufuegen Identitaet '{}' zu Gruppe fehlgeschlagen.",identitaet.getId());
                        }
                    }
                });

        if (addResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
            logger.error("Anlage Berechtigungsgruppe fehlgeschlagen. ResultCode={} DiagnosticMessage={}",
                    addResponse.getLdapResult().getResultCode(),
                    addResponse.getLdapResult().getDiagnosticMessage());
        }

    }

    private void updateBerechtigungsgruppe(Gruppe gruppeLdap, Set<IcaIdentitaet> identitaeten) {
        ModifyResponse modifyResponse = this.ldapConnectionTemplate.modify(
                ldapConnectionTemplate.newDn(gruppeLdap.getDn().toString()),
                request -> {
                    request.remove("member");
                    for(IcaIdentitaet identitaet : identitaeten){
                        logger.debug("Identitaet #{} muss hinzugefuegt werden", identitaet.getId());
                        try{
                            request.add("member", commandIdentitaet.identitaet2Ldap(identitaet.getId()).getDn().getName());
                        } catch (Exception e){
                            logger.warn("Hinzufuegen Identitaet '{}' zu Gruppe fehlgeschlagen.",identitaet.getId());
                        }
                    }
                });

        if (modifyResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
            logger.error("Update Berechtigungsgruppe fehlgeschlagen. ResultCode={} DiagnosticMessage={}",
                    modifyResponse.getLdapResult().getResultCode(),
                    modifyResponse.getLdapResult().getDiagnosticMessage());
        }
    }

    private void deleteBerechtigungsgruppe(Gruppe gruppeLdap) {
        DeleteResponse deleteResponse =
                this.ldapConnectionTemplate.delete(ldapConnectionTemplate.newDn(gruppeLdap.getDn().toString()));

        if (deleteResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
            logger.error("Löschen Berechtigungsgruppe fehlgeschlagen. ResultCode={} DiagnosticMessage={}",
                    deleteResponse.getLdapResult().getResultCode(),
                    deleteResponse.getLdapResult().getDiagnosticMessage());
        } else {
            logger.info("Berechtigungsgruppe in LDAP geloescht: {}",gruppeLdap.getCn());
        }
    }

    private Dn getBaseDn(Dn parentDn, SyncBerechtigungsgruppe berechtigungsgruppe){
        try {
            return new Dn("cn", berechtigungsgruppe.getTitle(), parentDn.getName());
        } catch (LdapInvalidDnException e) {
            logger.error("Anlage BaseDN fehlgeschlagen. Verarbeitung der Berechtigungsgruppe abgebrochen.",e);
            return null;
        }
    }

}
