package de.pfadfinden.mv.command;

import de.pfadfinden.mv.model.IcaGruppierung;
import de.pfadfinden.mv.service.IcaService;
import de.pfadfinden.mv.service.LdapEntryService;
import de.pfadfinden.mv.tools.LdapHelper;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.AddResponse;
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
import java.util.List;
import java.util.Optional;

@Component
@Order(10)
public class CommandGruppierungen implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(CommandGruppierungen.class);

    private IcaService icaService;
    private LdapEntryService ldapEntryService;
    private LdapConnectionTemplate ldapConnectionTemplate;

    public CommandGruppierungen(IcaService icaService,
                                LdapEntryService ldapEntryService,
                                LdapConnectionTemplate ldapConnectionTemplate) {
        this.icaService = icaService;
        this.ldapEntryService = ldapEntryService;
        this.ldapConnectionTemplate = ldapConnectionTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<IcaGruppierung> icaGruppierungList = this.icaService.getGruppierungen();
        for(IcaGruppierung icaGruppierung : icaGruppierungList){
            execGruppierung(icaGruppierung);
        }
    }

    private void execGruppierung(IcaGruppierung gruppierung) {
        logger.debug("## Start Verarbeitung Gruppierung {} ({}) ##",gruppierung.getId(),gruppierung.getName());
        Optional<de.pfadfinden.mv.ldap.schema.IcaGruppierung> ldapGruppierung = ldapEntryService.findIcaGruppierungById(gruppierung.getId());

        if(!ldapGruppierung.isPresent()){
            logger.debug("Gruppierung nicht vorhanden. Neuanlage erforderlich.");
            addGruppierung(gruppierung);
        } else {
            logger.debug("Gruppierung vorhanden. DN: {}",ldapGruppierung.get().getDn());
            if(needUpdateGruppierung(gruppierung,ldapGruppierung.get())){
                logger.debug("Aenderungen an Gruppierung erforderlich.");
                updateGruppierung(gruppierung,ldapGruppierung.get());
            } else {
                logger.debug("Keine Aenderungen an Gruppierung erforderlich.");
            }
        }
    }

    /**
     * Pruefe ob Aktualisierung des vorhandenen LDAP Eintrags erforderlich ist.
     * Dies ist nur dann der Fall, wenn Datensatz in LDAP nicht gesperrt und aelter als ICA ist.
     *
     * @return boolean
     * @author Philipp Steinmetzger
     */
    private boolean needUpdateGruppierung(IcaGruppierung gruppierungIca, de.pfadfinden.mv.ldap.schema.IcaGruppierung gruppierungLdap){
        if(gruppierungLdap.isIcaProtected()) return false;
        if(gruppierungLdap.getIcaLastUpdated()== null || gruppierungIca.getLastUpdated()== null) return true;
        if(gruppierungLdap.getIcaLastUpdated().before(gruppierungIca.getLastUpdated())) return true;
        return false;
    }

    /**
     * Fuehre Aktualisierung einer Gruppierung durch, indem Attribute ergazent, aktualisiert oder geloescht werden.
     *
     * @return void
     * @author Philipp Steinmetzger
     */
    private void updateGruppierung(final IcaGruppierung gruppierungIca, final de.pfadfinden.mv.ldap.schema.IcaGruppierung gruppierungLdap) {
        // Keine Modifikation der OU (Bestandteil DN) durchfuehren
        ModifyResponse modResponse = this.ldapConnectionTemplate.modify(
                this.ldapConnectionTemplate.newDn(gruppierungLdap.getDn().toString()), request -> {
                    LdapHelper.modifyRequest(request,gruppierungIca.getEbeneId(),gruppierungLdap.getIcaEbene(),"icaEbene");
                    LdapHelper.modifyRequest(request,gruppierungIca.getSitzOrt(),gruppierungLdap.getIcaSitzOrt(),"icaSitzOrt");
                    LdapHelper.modifyRequest(request,gruppierungIca.getVersion(),gruppierungLdap.getIcaVersion(),"icaVersion");
                    LdapHelper.modifyRequest(request,gruppierungIca.getStatus(),gruppierungLdap.getIcaStatus(),"icaStatus");
                    if(gruppierungLdap.getIcaLastUpdated() == null){
                        request.add("icaLastUpdated",new GeneralizedTime(new Date()).toGeneralizedTime());
                    } else {
                        request.replace("icaLastUpdated",new GeneralizedTime(new Date()).toGeneralizedTime());
                    }
                }
        );

        if (modResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
            logger.error("LDAP Response in updateGruppierung: {}",modResponse.getLdapResult().getDiagnosticMessage());
        }
    }

    private void addGruppierung(final IcaGruppierung gruppierung) {
        if(gruppierung.getParentGruppierungId()==0) return;

        Optional<de.pfadfinden.mv.ldap.schema.IcaGruppierung> parentGruppierung = ldapEntryService.findParentGruppierung(gruppierung);

        Dn parentDn = parentGruppierung.isPresent() ? parentGruppierung.get().getDn() : LdapHelper.getBaseDn(ldapConnectionTemplate);

        try {
            Dn dn = new Dn("ou", gruppierung.getName(), parentDn.getName());
            logger.debug("DN fuer Gruppierung #{} lautet: {}",gruppierung.getId(),dn.toString());

            AddResponse addResponse = this.ldapConnectionTemplate.add(dn, request -> {
                        Entry entry = request.getEntry();
                        entry.add("objectClass", "top", "organizationalUnit", "icaGruppierung");
                        entry.add("ou",gruppierung.getName());
                        entry.add("icaId",String.valueOf(gruppierung.getId()));
                        entry.add("icaStatus",gruppierung.getStatus());
                        entry.add("icaVersion",String.valueOf(gruppierung.getVersion()));
                        if(gruppierung.getLastUpdated() != null) {
                            entry.add("icaLastUpdated", new GeneralizedTime(gruppierung.getLastUpdated()).toString());
                        }
                        entry.add("icaEbene",gruppierung.getEbeneName());
                        if(!gruppierung.getSitzOrt().isEmpty()) entry.add("icaSitzOrt",gruppierung.getSitzOrt());
                    }
            );

            if (addResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
                logger.error("LDAP Response in addGruppierung: {}",addResponse.getLdapResult().getDiagnosticMessage());
            }
        } catch (LdapInvalidDnException e) {
            logger.error("Gruppierung #{} konnte in LDAP nicht angelegt werden.",gruppierung.getId(),e);
        }
    }

}
