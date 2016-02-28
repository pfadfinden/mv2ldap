package de.pfadfinden.mv.command;

import de.pfadfinden.mv.database.LdapDatabase;
import de.pfadfinden.mv.ldap.EntryServiceLdap;
import de.pfadfinden.mv.ldap.schema.IcaGruppierung;
import de.pfadfinden.mv.model.IcaIdentitaet;
import de.pfadfinden.mv.service.ica.IdentitaetService;
import de.pfadfinden.mv.tools.UsernameGenerator;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;
import org.apache.directory.ldap.client.template.RequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandIdentitaet {
    final Logger logger = LoggerFactory.getLogger(CommandIdentitaet.class);

    public CommandIdentitaet(){
    }

    /**
     * Stellt sicher, dass Identitaet in LDAP vorhanden und aktuell ist.
     * @param identitaet
     */
    public de.pfadfinden.mv.ldap.schema.IcaIdentitaet identitaet2Ldap(int identitaet){
        IcaIdentitaet icaIdentitaet = IdentitaetService.findIdentitaetById(identitaet);

        de.pfadfinden.mv.ldap.schema.IcaIdentitaet ldapIdentitaet = EntryServiceLdap.findIcaIdentitaetById(identitaet);

        if(ldapIdentitaet == null){
            addIdentitaet(icaIdentitaet);
        } else {
            if(needUpdate(icaIdentitaet,ldapIdentitaet))
                updateIdentitaet(icaIdentitaet,ldapIdentitaet);
        }
        return EntryServiceLdap.findIcaIdentitaetById(identitaet);
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
        String username;
        try {
            username = UsernameGenerator.getUsername(icaIdentitaet.getNachname(),icaIdentitaet.getVorname());
        } catch (IllegalArgumentException e){
            logger.error("Anlage Identitaet #{} nicht moeglich, da Vor- oder Nachname fehlt.",icaIdentitaet.getId());
            return;
        }

        IcaGruppierung gruppierung = EntryServiceLdap.findIcaGruppierungById(icaIdentitaet.getGruppierungId());
        if(gruppierung == null){
            logger.error("Identitaet #{} konnte keiner Gruppierung zugeordnet werden (ICA Gruppierung #{})",
                    icaIdentitaet.getId(),icaIdentitaet.getGruppierungId());
            return;
        }

        Dn dn = null;
        try {
            dn = new Dn(
                    "uid", username,
                    gruppierung.getDn().getName()
            );
        } catch (LdapInvalidDnException e) {
            logger.error("Erstellung DN fehlerhaft",e);
        }

        LdapConnectionTemplate ldapConnectionTemplate = LdapDatabase.getLdapConnectionTemplate();

        AddResponse addResponse = ldapConnectionTemplate.add(
                dn,
                new RequestBuilder<AddRequest>() {
                    @Override
                    public void buildRequest(AddRequest request) throws LdapException {
                        Entry entry = request.getEntry();
                        entry.add("objectClass","inetOrgPerson","organizationalPerson","person",
                                "top","icaIdentitaet","icaRecord");
                        entry.add("icaId",String.valueOf(icaIdentitaet.getId()));
                        entry.add("icaStatus",icaIdentitaet.getStatus());
                        entry.add("icaVersion",String.valueOf(icaIdentitaet.getVersion()));
                        entry.add("icaMitgliedsnummer",String.valueOf(icaIdentitaet.getMitgliedsNummer()));
                        if(icaIdentitaet.getHash() != null && !icaIdentitaet.getHash().trim().isEmpty()) {
                            entry.add("icaHash", icaIdentitaet.getHash());
                        }

                        entry.add("street",icaIdentitaet.getStrasse());
                        entry.add("postalAddress",icaIdentitaet.getStrasse());
                        entry.add("postalCode",icaIdentitaet.getPlz());
                        entry.add("l",icaIdentitaet.getOrt());
                        entry.add("mail",icaIdentitaet.getEmail());

                        if(icaIdentitaet.getLastUpdated() != null){
                            entry.add("icaLastUpdated",new GeneralizedTime(icaIdentitaet.getLastUpdated()).toString());
                        }

                        if(icaIdentitaet.getSpitzname() != null){
                            entry.add("icaSpitzname",icaIdentitaet.getSpitzname());
                        }

                        entry.add("cn",icaIdentitaet.getCommonName());
                        entry.add("displayName",icaIdentitaet.getDisplayName());
                        entry.add("givenName",icaIdentitaet.getVorname());
                        entry.add("sn",icaIdentitaet.getNachname());

                        entry.add("homePhone",icaIdentitaet.getTelefon1());
                        entry.add("mobile",icaIdentitaet.getTelefon3());
                        entry.add("facsimileTelephoneNumber",icaIdentitaet.getTelefax());
                    }
                }
        );

        if (addResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
            logger.error(addResponse.getLdapResult().getDiagnosticMessage());
        }
        return;
    }

    private void updateIdentitaet(final IcaIdentitaet icaIdentitaet, de.pfadfinden.mv.ldap.schema.IcaIdentitaet ldapIdentitaet){
        logger.debug("Identitaet #{} in LDAP vorhanden, aber Update erforderlich.",icaIdentitaet.getId());
        ModifyResponse modResponse = LdapDatabase.getLdapConnectionTemplate().modify(
                LdapDatabase.getLdapConnectionTemplate().newDn(ldapIdentitaet.getDn().toString()),
                new RequestBuilder<ModifyRequest>() {
                    @Override
                    public void buildRequest(ModifyRequest request) throws LdapException
                    {
                        request.replace("icaStatus",icaIdentitaet.getStatus());
                        request.replace("icaVersion",String.valueOf(icaIdentitaet.getVersion()));
                        request.replace("icaMitgliedsnummer",String.valueOf(icaIdentitaet.getMitgliedsNummer()));
                        if(icaIdentitaet.getHash() != null && !icaIdentitaet.getHash().trim().isEmpty()) {
                            request.replace("icaHash", icaIdentitaet.getHash());
                        }
                        request.replace("givenName",icaIdentitaet.getVorname());

                        if(icaIdentitaet.getSpitzname() != null){
                            request.replace("icaSpitzname",icaIdentitaet.getSpitzname());
                            request.replace("initials",icaIdentitaet.getSpitzname());
                        }

                        request.replace("street",icaIdentitaet.getStrasse());
                        request.replace("postalAddress",icaIdentitaet.getStrasse());
                        request.replace("postalCode",icaIdentitaet.getPlz());
                        request.replace("l",icaIdentitaet.getOrt());
                        request.replace("mail",icaIdentitaet.getEmail());
                        if(icaIdentitaet.getLastUpdated() != null){
                            request.replace("icaLastUpdated",new GeneralizedTime(icaIdentitaet.getLastUpdated()).toString());
                        }
                        request.replace("displayName",icaIdentitaet.getDisplayName());
                        request.replace("cn",icaIdentitaet.getCommonName());
                        request.replace("sn",icaIdentitaet.getNachname());
                    }
                }
        );

        if (modResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
            logger.error(modResponse.getLdapResult().getDiagnosticMessage());
        }
        return;
    }
}