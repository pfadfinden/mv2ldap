package de.pfadfinden.mv.command;

import de.pfadfinden.mv.connector.ConnectorICA;
import de.pfadfinden.mv.connector.ConnectorLDAP;
import de.pfadfinden.mv.model.IcaIdentitaet;
import de.pfadfinden.mv.service.IcaRecordService;
import de.pfadfinden.mv.service.ica.IdentitaetService;
import de.pfadfinden.mv.tools.LdapHelper;
import de.pfadfinden.mv.tools.UsernameGenerator;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommandIdentitaet {
    final Logger logger = LoggerFactory.getLogger(CommandIdentitaet.class);

    public static Connection connectionICA;
    public static LdapConnection connectionLDAP;

    IcaRecordService icaRecordService;
    IdentitaetService identitaetService;

    public final String icaIdentiaeten = "" +
            "SELECT *, identitaet.genericField1 AS spitzname " +
            "FROM identitaet " +
            "WHERE status='AKTIV' AND id < 25000 " +
            "ORDER BY id ASC " +
            "LIMIT 0,50";

    public CommandIdentitaet()  {
        connectionICA = ConnectorICA.getConnection();
        connectionLDAP = ConnectorLDAP.getConnection();
        icaRecordService = new IcaRecordService();
        identitaetService = new IdentitaetService();

        try {
            connectionICA.close();
            connectionLDAP.unBind();
            connectionLDAP.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LdapException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stellt sicher, dass Identitaet in LDAP vorhanden und aktuell ist.
     * @param identitaet
     */
    public Entry identitaet2Ldap(int identitaet){
        IcaIdentitaet icaIdentitaet = identitaetService.findIdentitaetById(identitaet);

        Entry inetOrgPerson = icaRecordService.findIdentitaetById(identitaet);

        if(inetOrgPerson == null){
            logger.debug("Identiaet #{} in LDAP nicht vorhanden. Neuanlage erforderlich.",identitaet);
            try {
                addIdentitaet(icaIdentitaet);
            } catch (LdapException e) {
                logger.error("Identiaet konnte nicht angelegt werden.",e);
            }
        } else {
            logger.debug("Identitaet #{} in LDAP vorhanden. Prüfung Update erforderlich.",identitaet);
            try {
                updateIdentitaet(icaIdentitaet,inetOrgPerson);
            } catch (LdapException e) {
                logger.error("Identiaet konnte nicht aktualisiert werden.",e);
            }
        }
        return icaRecordService.findIdentitaetById(identitaet);
    }

    private void addIdentitaet(IcaIdentitaet icaIdentitaet) throws LdapException {
        String username;
        try {
            username = UsernameGenerator.getUsername(icaIdentitaet.getNachname(),icaIdentitaet.getVorname());
        } catch (IllegalArgumentException e){
            logger.error("Anlage Identitaet #{} nicht moeglich, da Vor- oder Nachname fehlt.",icaIdentitaet.getId());
            return;
        }

        Entry gruppierung = icaRecordService.findGruppierungById(icaIdentitaet.getGruppierungId());
        if(gruppierung == null){
            logger.error("Identitaet #{} konnte keiner Gruppierung zugeordnet werden (ICA Gruppierung #{})",
                    icaIdentitaet.getId(),icaIdentitaet.getGruppierungId());
            return;
        }

        Dn dn = new Dn(
                "cn", username,
                gruppierung.getDn().getName()
        );

        logger.debug("DN: {}",dn);

        Entry entry = new DefaultEntry();
        entry.setDn(dn);
        entry.add("ObjectClass","inetOrgPerson");
        entry.add("ObjectClass","organizationalPerson");
        entry.add("ObjectClass","person");
        entry.add("ObjectClass","top");
        entry.add("ObjectClass","icaIdentitaet");
        entry.add("ObjectClass","icaRecord");

        entry.add("icaId",String.valueOf(icaIdentitaet.getId()));
        entry.add("icaStatus",icaIdentitaet.getStatus());
        entry.add("icaVersion",String.valueOf(icaIdentitaet.getVersion()));
        entry.add("icaMitgliedsnummer",String.valueOf(icaIdentitaet.getMitgliedsNummer()));
        if(icaIdentitaet.getHash() != null && !icaIdentitaet.getHash().trim().isEmpty()) {
            entry.add("icaHash", icaIdentitaet.getHash());
        }
        entry.add("givenName",icaIdentitaet.getVorname());
        entry.add("icaSpitzname","Test Test");

        entry.add("street",icaIdentitaet.getStrasse());
        entry.add("postalCode",icaIdentitaet.getPlz());
        entry.add("l",icaIdentitaet.getOrt());

        entry.add("mail",icaIdentitaet.getEmail());
     //   entry.add("mobile",icaIdentitaet.getTelefon3());
     //   entry.add("telephoneNumber",icaIdentitaet.getTelefon1());
     //   if(!icaIdentitaet.getTelefax().trim().isEmpty()) {
     //       entry.add("facsimileTelephoneNumber", icaIdentitaet.getTelefax());
     //   }

        Date lastUpdated;
        if(icaIdentitaet.getLastUpdated() == null){
            lastUpdated = new Date();
        } else {
            lastUpdated = icaIdentitaet.getLastUpdated();
        }

        entry.add("icaLastUpdated",new GeneralizedTime(lastUpdated).toString());
        entry.add("sn",icaIdentitaet.getNachname());
    //    entry.add("cn",icaIdentitaet.getVorname()+" "+icaIdentitaet.getNachname());

        ConnectorLDAP.getConnection().add(entry);
        return;
    }

    private Entry updateIdentitaet(IcaIdentitaet icaIdentitaet, Entry ldapIdentitaet) throws LdapException {
        List<DefaultModification> modifications = new ArrayList<DefaultModification>();

        modifications.add(LdapHelper.checkAttributeValue(ldapIdentitaet,icaIdentitaet,icaIdentitaet.getVersion(),"icaVersion"));
        modifications.add(LdapHelper.checkAttributeValue(ldapIdentitaet,icaIdentitaet,icaIdentitaet.getLastUpdated(),"icaLastUpdated"));
        modifications.add(LdapHelper.checkAttributeValue(ldapIdentitaet,icaIdentitaet,icaIdentitaet.getNachname(),"sn"));

        for(DefaultModification modification : modifications){
            if(modification == null) continue;
            connectionLDAP.modify(ldapIdentitaet.getDn(),modification);
        }

        return null;
    }
}