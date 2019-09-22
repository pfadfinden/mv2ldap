package de.pfadfinden.mv.command;

import de.pfadfinden.mv.database.LdapDatabase;
import de.pfadfinden.mv.ldap.schema.IcaGruppierung;
import de.pfadfinden.mv.model.IcaIdentitaet;
import de.pfadfinden.mv.service.IcaService;
import de.pfadfinden.mv.service.LdapEntryService;
import de.pfadfinden.mv.tools.IcaUtils;
import de.pfadfinden.mv.tools.UsernameGenerator;
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
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CommandIdentitaet {
    private final Logger logger = LoggerFactory.getLogger(CommandIdentitaet.class);

    private final IcaService icaService;
    private final LdapEntryService ldapEntryService;

    public CommandIdentitaet(IcaService icaService, LdapEntryService ldapEntryService) {
        this.icaService = icaService;
        this.ldapEntryService = ldapEntryService;
    }

    /**
     * Stellt sicher, dass Identitaet in LDAP vorhanden und aktuell ist.
     * @param identitaet
     */
    public de.pfadfinden.mv.ldap.schema.IcaIdentitaet identitaet2Ldap(int identitaet){
        Optional<IcaIdentitaet> icaIdentitaet = icaService.findIdentitaetById(identitaet);

        if(!icaIdentitaet.isPresent()){
            logger.error("Keine IcaIdentitaet Instanz zu #{} aufgebaut: Return null.",identitaet);
            return null;
        }

        Optional<de.pfadfinden.mv.ldap.schema.IcaIdentitaet> ldapIdentitaet = ldapEntryService.findIcaIdentitaetById(identitaet);

        if(!ldapIdentitaet.isPresent()){
            addIdentitaet(icaIdentitaet.get());
        } else {
            if(needUpdate(icaIdentitaet.get(),ldapIdentitaet.get()))
                updateIdentitaet(icaIdentitaet.get(),ldapIdentitaet.get());
        }
        return ldapEntryService.findIcaIdentitaetById(identitaet).get();
    }

    /**
     * Pruefe ob Aktualisierung des vorhandenen LDAP Eintrags erforderlich ist.
     * Dies ist nur dann der Fall, wenn Datensatz in LDAP nicht gesperrt und aelter als ICA ist.
     *
     * @return boolean
     * @author Philipp Steinmetzger
     */
    private boolean needUpdate(de.pfadfinden.mv.model.IcaIdentitaet identitaetIca, de.pfadfinden.mv.ldap.schema.IcaIdentitaet identitaetLdap){
        if(identitaetLdap.isIcaProtected()) return false;
        if(identitaetLdap.getIcaLastUpdated()== null || identitaetIca.getLastUpdated()== null) return true;
        if(identitaetLdap.getIcaLastUpdated().before(identitaetIca.getLastUpdated())) return true;
        return false;
    }

    private void addIdentitaet(final IcaIdentitaet icaIdentitaet){
        logger.debug("Identitaet #{} in LDAP nicht vorhanden. Neuanlage erforderlich.",icaIdentitaet.getId());

        if(!IcaUtils.isValidIdentitaet(icaIdentitaet)){
            logger.warn("Anlage Identitaet #{} nicht moeglich, da Vor/Nachname unplausibel.", icaIdentitaet.getId());
            return;
        }

        if(!IcaUtils.isValidEmail(icaIdentitaet.getEmail())){
            logger.warn("Anlage Identitaet #{} nicht erfolgt da Mail ungueltig.", icaIdentitaet.getId());
            return;
        }

        String username = UsernameGenerator.getUsername(icaIdentitaet.getNachname(),icaIdentitaet.getVorname());

        Optional<IcaGruppierung> gruppierung = ldapEntryService.findIcaGruppierungById(icaIdentitaet.getGruppierungId());

        if(!gruppierung.isPresent()){
            logger.error("Identitaet #{} konnte keiner Gruppierung zugeordnet werden (ICA Gruppierung #{})",
                    icaIdentitaet.getId(),icaIdentitaet.getGruppierungId());
            return;
        }

        try {
            Dn dn = new Dn("uid", username, gruppierung.get().getDn().getName());

            LdapConnectionTemplate ldapConnectionTemplate = LdapDatabase.getLdapConnectionTemplate();

            AddResponse addResponse = ldapConnectionTemplate.add(dn, request -> {
                        Entry entry = request.getEntry();
                        entry.add("objectClass","inetOrgPerson","organizationalPerson","person",
                                "top","icaIdentitaet","icaRecord");

                        // ICA Parameter
                        entry.add("icaId",String.valueOf(icaIdentitaet.getId()));
                        entry.add("icaStatus",icaIdentitaet.getStatus());
                        entry.add("icaVersion",String.valueOf(icaIdentitaet.getVersion()));
                        entry.add("icaMitgliedsnummer",String.valueOf(icaIdentitaet.getMitgliedsNummer()));
                        if(icaIdentitaet.getHash() != null && !icaIdentitaet.getHash().trim().isEmpty()) {
                            entry.add("icaHash", icaIdentitaet.getHash());
                        }

                        if(icaIdentitaet.getLastUpdated() != null){
                            entry.add("icaLastUpdated",new GeneralizedTime(icaIdentitaet.getLastUpdated()).toString());
                        }

                        // Namen
                        entry.add("cn",icaIdentitaet.getCommonName());
                        entry.add("displayName",icaIdentitaet.getDisplayName());
                        entry.add("givenName",icaIdentitaet.getVorname());
                        entry.add("sn",icaIdentitaet.getNachname());
                        if(icaIdentitaet.getSpitzname() != null) entry.add("icaSpitzname",icaIdentitaet.getSpitzname());

                        if (icaIdentitaet.getEmail() != null) {
                            entry.add("mail", icaIdentitaet.getEmail());
                        }

                        // Telefon
                        if(!icaIdentitaet.getTelefon1().isEmpty()) entry.add("telephoneNumber",icaIdentitaet.getTelefon1());
                        if(!icaIdentitaet.getTelefon1().isEmpty()) entry.add("homePhone",icaIdentitaet.getTelefon1());
                        if(!icaIdentitaet.getTelefon2().isEmpty()) entry.add("otherHomePhone",icaIdentitaet.getTelefon2());
                        if(!icaIdentitaet.getTelefon3().isEmpty()) entry.add("mobile",icaIdentitaet.getTelefon3());
                        if(!icaIdentitaet.getTelefax().isEmpty()) entry.add("facsimileTelephoneNumber",icaIdentitaet.getTelefax());

                        // Sonstiges
                        entry.add("ou",gruppierung.get().getIcaEbene()+" "+gruppierung.get().getOu());
                    }
            );

            if (addResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
                logger.error("LDAP Add Identitaet #{}: {} '{}'",
                        icaIdentitaet.getMitgliedsNummer(),
                        addResponse.getLdapResult().getResultCode(),
                        addResponse.getLdapResult().getDiagnosticMessage());
            }

        } catch (LdapInvalidDnException e) {
            logger.error("Erstellung DN fehlerhaft",e);
        }
    }

    private void updateIdentitaet(final IcaIdentitaet icaIdentitaet, de.pfadfinden.mv.ldap.schema.IcaIdentitaet ldapIdentitaet){
        logger.debug("Identitaet #{} in LDAP vorhanden, aber Update erforderlich.",icaIdentitaet.getId());
        ModifyResponse modResponse = LdapDatabase.getLdapConnectionTemplate().modify(
                LdapDatabase.getLdapConnectionTemplate().newDn(ldapIdentitaet.getDn().toString()),request -> {
                    request.replace("icaStatus", icaIdentitaet.getStatus());
                    request.replace("icaVersion", String.valueOf(icaIdentitaet.getVersion()));
                    request.replace("icaMitgliedsnummer", String.valueOf(icaIdentitaet.getMitgliedsNummer()));
                    if (icaIdentitaet.getHash() != null && !icaIdentitaet.getHash().trim().isEmpty()) {
                        request.replace("icaHash", icaIdentitaet.getHash());
                    }

                    if (icaIdentitaet.getLastUpdated() != null) {
                        request.replace("icaLastUpdated", new GeneralizedTime(icaIdentitaet.getLastUpdated()).toString());
                    }

                    // Namen
                    request.replace("cn", icaIdentitaet.getCommonName());
                    request.replace("displayName", icaIdentitaet.getDisplayName());
                    request.replace("givenName", icaIdentitaet.getVorname());
                    request.replace("sn", icaIdentitaet.getNachname());
                    if (icaIdentitaet.getSpitzname() != null)
                        request.replace("icaSpitzname", icaIdentitaet.getSpitzname());

                    if (icaIdentitaet.getEmail() != null){
                        request.replace("mail", icaIdentitaet.getEmail());
                    }
                }
        );

        if (modResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
            logger.error("LDAP Mod Identitaet #{}: {} '{}'",
                    icaIdentitaet.getMitgliedsNummer(),
                    modResponse.getLdapResult().getResultCode(),
                    modResponse.getLdapResult().getDiagnosticMessage());
        }
    }
}