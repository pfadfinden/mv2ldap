package de.pfadfinden.mv.command;

import de.pfadfinden.mv.database.LdapDatabase;
import de.pfadfinden.mv.ldap.schema.Gruppe;
import de.pfadfinden.mv.ldap.schema.IcaGruppierung;
import de.pfadfinden.mv.model.IcaIdentitaet;
import de.pfadfinden.mv.model.SyncBerechtigungsgruppe;
import de.pfadfinden.mv.service.IcaService;
import de.pfadfinden.mv.service.LdapEntryService;
import de.pfadfinden.mv.service.SyncService;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.AddResponse;
import org.apache.directory.api.ldap.model.message.ModifyResponse;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class CommandGruppen {
    final Logger logger = LoggerFactory.getLogger(CommandGruppen.class);

    private final CommandIdentitaet commandIdentitaet;
    private final IcaService icaService;
    private final SyncService syncService;
    private final LdapEntryService ldapEntryService;

    public CommandGruppen(CommandIdentitaet commandIdentitaet,
                          IcaService icaService,
                          SyncService syncService,
                          LdapEntryService ldapEntryService) {
        this.commandIdentitaet = commandIdentitaet;
        this.icaService = icaService;
        this.syncService = syncService;
        this.ldapEntryService = ldapEntryService;
    }

    public void exec() throws SQLException {

        List<SyncBerechtigungsgruppe> syncBerechtigungsgruppeList = this.syncService.getSyncGruppen();

        for(SyncBerechtigungsgruppe berechtigungsgruppe : syncBerechtigungsgruppeList) {
            berechtigungsgruppe.setTaetigkeiten(syncService.getTaetigkeitenZuBerechtigungsgruppe(berechtigungsgruppe));
            Set<IcaIdentitaet> identitaetenZurBerechtigungsgruppe = icaService.findIdentitaetByBerechtigungsgruppe(berechtigungsgruppe);
            if (identitaetenZurBerechtigungsgruppe == null) continue;
            try {
                execBerechtigungsgruppe(berechtigungsgruppe, identitaetenZurBerechtigungsgruppe);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void execBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe, Set<IcaIdentitaet> identitaeten) throws Exception {
        logger.info("## Berechtigungsgruppe #{} '{}' ##",berechtigungsgruppe.getId(),berechtigungsgruppe.getTitle());

        Gruppe gruppe = ldapEntryService.findGruppeById(berechtigungsgruppe.getId());

        if(gruppe == null){
            logger.info("Berechtigungsgruppe in LDAP nicht vorhanden.");
            addBerechtigungsgruppe(berechtigungsgruppe,identitaeten);
        } else {
            logger.info("Berechtigungsgruppe in LDAP vorhanden: {}",gruppe.getDn());
            updateBerechtigungsgruppe(berechtigungsgruppe,gruppe,identitaeten);
        }
    }

    private void addBerechtigungsgruppe(final SyncBerechtigungsgruppe berechtigungsgruppe, final Set<IcaIdentitaet> identitaeten) throws LdapException {
        Dn baseDn;
        final IcaGruppierung ownerGruppierung = ldapEntryService.findIcaGruppierungById(berechtigungsgruppe.getOwnerGruppierung());
        if(berechtigungsgruppe.getOwnerGruppierung()!=0 && ownerGruppierung != null){
            baseDn = new Dn(
                    "cn", berechtigungsgruppe.getTitle(),
                    ownerGruppierung.getDn().getName()
            );
        } else {
            baseDn = new Dn(
                    "cn", berechtigungsgruppe.getTitle(),
                    LdapDatabase.getBaseDn().getName()
            );
        }

        logger.debug("DN: {}",baseDn);

        AddResponse addResponse = LdapDatabase.getLdapConnectionTemplate().add(
                baseDn,
                request -> {
                    Entry entry = request.getEntry();
                    entry.add("ObjectClass","groupOfNames", "icaBerechtigung", "icaRecord");
                    if(berechtigungsgruppe.getDescription()!=null) entry.add("description",berechtigungsgruppe.getDescription());
                    entry.add("icaId",String.valueOf(berechtigungsgruppe.getId()));
                    entry.add("icaLastUpdated",new GeneralizedTime(new Date()).toString());
                    if(ownerGruppierung != null) entry.add("icaGruppierungId",String.valueOf(berechtigungsgruppe.getOwnerGruppierung()));

                    if(identitaeten.size() == 0) return;
                    for(IcaIdentitaet identitaet: identitaeten) {
                        logger.debug("Identitaet #{} muss hinzugefuegt werden", identitaet.getId());
                        de.pfadfinden.mv.ldap.schema.IcaIdentitaet inetOrgPerson = commandIdentitaet.identitaet2Ldap(identitaet.getId());
                        if (inetOrgPerson != null){
                            logger.debug("Hinzufuegen Identitaet {}",inetOrgPerson.getDn().getName());
                            entry.add("member",inetOrgPerson.getDn().getName());
                        } else {
                            logger.error("Identitaet #{} zu Gruppe nicht hinzugefuegt, da nicht in LDAP gefunden.", identitaet.getId());
                        }
                    }

                }
        );

        if (addResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
            logger.error(addResponse.getLdapResult().getDiagnosticMessage());
        }

        return;
    }

    private void updateBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe, Gruppe gruppeLdap, final Set<IcaIdentitaet> identitaeten) {
        LdapConnectionTemplate ldapConnectionTemplate = LdapDatabase.getLdapConnectionTemplate();

        ModifyResponse modifyResponse = ldapConnectionTemplate.modify(
                ldapConnectionTemplate.newDn(gruppeLdap.getDn().toString()),
                request -> {
                    request.remove("member");
                    for(IcaIdentitaet identitaet : identitaeten){
                        de.pfadfinden.mv.ldap.schema.IcaIdentitaet inetOrgPerson = commandIdentitaet.identitaet2Ldap(identitaet.getId());
                        if (inetOrgPerson != null) {
                            request.add("member", inetOrgPerson.getDn().getName());
                        } else {
                            logger.error("Identitaet #{} zu Gruppe nicht hinzugefuegt, da nicht in LDAP gefunden.", identitaet.getId());
                        }
                    }
                }
        );

        if (modifyResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
            logger.error(modifyResponse.getLdapResult().getDiagnosticMessage());
        }
        return;
    }
}