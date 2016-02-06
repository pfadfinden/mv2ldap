package de.pfadfinden.mv.command;

import de.pfadfinden.mv.connector.ConnectorICA;
import de.pfadfinden.mv.connector.ConnectorLDAP;
import de.pfadfinden.mv.model.IcaIdentitaet;
import de.pfadfinden.mv.model.IcaRecord;
import de.pfadfinden.mv.service.IcaRecordService;
import de.pfadfinden.mv.tools.LdapHelper;
import de.pfadfinden.mv.tools.UsernameGenerator;
import org.apache.directory.api.ldap.model.cursor.CursorException;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Philipp on 14.01.2016.
 */
public class CommandIdentitaet {
    final Logger logger = LoggerFactory.getLogger(CommandIdentitaet.class);

    public static Connection connectionICA;
    public static LdapConnection connectionLDAP;

    PreparedStatement icaIdentiaetenStatement;
    IcaRecordService icaRecordService;

    public final String icaIdentiaeten = "" +
            "SELECT *, identitaet.genericField1 AS spitzname " +
            "FROM identitaet " +
            "WHERE status='AKTIV' AND id < 25000 " +
            "ORDER BY id ASC " +
            "LIMIT 0,50";

    public CommandIdentitaet() throws SQLException, LdapException, IOException {
        connectionICA = ConnectorICA.getConnection();
        connectionLDAP = ConnectorLDAP.getConnection();
        icaRecordService = new IcaRecordService();
        icaIdentiaetenStatement = connectionICA.prepareStatement(icaIdentiaeten);
        ResultSet icaIdentitaetenResultset = icaIdentiaetenStatement.executeQuery();
        IcaIdentitaet icaIdentitaet;
        while (icaIdentitaetenResultset.next()) {
            icaIdentitaet = new IcaIdentitaet(icaIdentitaetenResultset);
            try {
                Entry identiaet = icaRecordService.findIdentitaetById(icaIdentitaet.getId());
                if(identiaet == null){
                    logger.debug("Neuanlage Identiaet #{} {}",icaIdentitaet.getId(),icaIdentitaet.getNachname());
                    addIdentitaet(icaIdentitaet);
                } else {
                    logger.debug("Update Identiaet #{} {}",icaIdentitaet.getId(),icaIdentitaet.getNachname());
                    updateIdentitaet(icaIdentitaet,identiaet);
                }
            } catch (Exception e) {
                logger.error("Verarbeitung Identitaet #{} fehlgeschlagen.",icaIdentitaet.getId(),e);
            }
        }
        connectionICA.close();
        connectionLDAP.unBind();
        connectionLDAP.close();
    }


    private void addIdentitaet(IcaIdentitaet icaIdentitaet) throws CursorException, LdapException {
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
        entry.add("icaHash",icaIdentitaet.getHash());

        entry.add("givenName",icaIdentitaet.getVorname());
        entry.add("icaSpitzname","Test Test");

        entry.add("street",icaIdentitaet.getStrasse());
        entry.add("postalCode",icaIdentitaet.getPlz());
        entry.add("l",icaIdentitaet.getOrt());

        entry.add("mail",icaIdentitaet.getEmail());
        entry.add("mobile",icaIdentitaet.getTelefon3());
        entry.add("telephoneNumber",icaIdentitaet.getTelefon1());
        entry.add("facsimileTelephoneNumber",icaIdentitaet.getTelefax());


        Date lastUpdated;
        if(icaIdentitaet.getLastUpdated() == null){
            lastUpdated = new Date();
        } else {
            lastUpdated = icaIdentitaet.getLastUpdated();
        }

        entry.add("icaLastUpdated",new GeneralizedTime(lastUpdated).toString());
        entry.add("sn",icaIdentitaet.getNachname());
    //    entry.add("cn",icaIdentitaet.getVorname()+" "+icaIdentitaet.getNachname());

        connectionLDAP.add(entry);
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