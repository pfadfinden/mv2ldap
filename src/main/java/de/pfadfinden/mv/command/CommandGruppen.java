package de.pfadfinden.mv.command;

import de.pfadfinden.mv.database.LdapDatabase;
import de.pfadfinden.mv.database.SyncDatabase;
import de.pfadfinden.mv.model.IcaIdentitaet;
import de.pfadfinden.mv.model.SyncBerechtigungsgruppe;
import de.pfadfinden.mv.model.SyncTaetigkeit;
import de.pfadfinden.mv.service.IcaRecordService;
import de.pfadfinden.mv.service.ica.IdentitaetService;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.entry.*;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
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
import java.util.*;

public class CommandGruppen {
    final Logger logger = LoggerFactory.getLogger(CommandGruppen.class);

    private IdentitaetService identitaetService;

    IcaRecordService icaRecordService;

    public final String syncGruppen = "SELECT * FROM berechtigungsgruppe " +
            "WHERE deleted = 0 " +
            "ORDER BY id ASC ";

    public final String syncTaetigkeiten = "SELECT * FROM taetigkeit " +
            "WHERE berechtigungsgruppe_id = ? " +
            "ORDER BY id ASC ";

    public CommandGruppen() throws SQLException, LdapException, IOException {

        icaRecordService = new IcaRecordService();
        identitaetService = new IdentitaetService();

        SyncBerechtigungsgruppe berechtigungsgruppe;

        try (
            Connection connection = SyncDatabase.getConnection();
            PreparedStatement statement = connection.prepareStatement(syncGruppen);
            ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                berechtigungsgruppe = new SyncBerechtigungsgruppe(resultSet);
                berechtigungsgruppe.setTaetigkeiten(getTaetigkeitenZuBerechtigungsgruppe(berechtigungsgruppe));
                Set<IcaIdentitaet> identitaetenZurBerechtigungsgruppe = identitaetService.findIdentitaetByBerechtigungsgruppe(berechtigungsgruppe);
                try {
                    execBerechtigungsgruppe(berechtigungsgruppe, identitaetenZurBerechtigungsgruppe);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void execBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe, Set<IcaIdentitaet> identitaeten) throws CursorException, LdapException, Exception {
        logger.info("## Berechtigungsgruppe #{} '{}' ##",berechtigungsgruppe.getId(),berechtigungsgruppe.getTitle());
        Entry gruppierungEntry = icaRecordService.findBerechtigungsgruppe(berechtigungsgruppe.getId());

        if(gruppierungEntry == null){
            logger.info("Berechtigungsgruppe in LDAP nicht vorhanden.");
            addBerechtigungsgruppe(berechtigungsgruppe,identitaeten);
        } else {
            logger.info("Berechtigungsgruppe in LDAP vorhanden: {}",gruppierungEntry.getDn());

            updateBerechtigungsgruppe(berechtigungsgruppe,gruppierungEntry,identitaeten);
//            if(needUpdateGruppierung(gruppierung,gruppierungEntry)){
//                updateGruppierung(gruppierung,gruppierungEntry);
//            }
        }
    }

    private void addBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe, Set<IcaIdentitaet> identitaeten) throws CursorException, LdapException {

        String baseDn = "dc=example,dc=com";

        Dn dn = new Dn(
                "cn", berechtigungsgruppe.getTitle(),
                baseDn
        );

        logger.debug("DN: {}",dn);

        Entry entry = new DefaultEntry();
        entry.setDn(dn);
        entry.add("ObjectClass","groupOfNames");
        entry.add("ObjectClass","icaRecord");

        entry.add("cn",berechtigungsgruppe.getTitle());
        if(berechtigungsgruppe.getDescription()!=null) entry.add("description",berechtigungsgruppe.getDescription());
        entry.add("icaId",String.valueOf(berechtigungsgruppe.getId()));
        entry.add("icaLastUpdated",new GeneralizedTime(new Date()).toString());

    //    entry.add("member","cn=philipp.steinmetzger,ou=Barrakuda,ou=München (Bayern),ou=Bayern,ou=BdP,dc=example,dc=com");
        CommandIdentitaet commandIdentitaet = new CommandIdentitaet();

        if(identitaeten.size() == 0) return;
        for(IcaIdentitaet identitaet: identitaeten) {
            logger.debug("Identität #{} muss hinzugefuegt werden", identitaet.getId());
            Entry inetOrgPerson = commandIdentitaet.identitaet2Ldap(identitaet.getId());
            if (inetOrgPerson != null){
                logger.debug("Hinzufuegen Identitaet {}",inetOrgPerson.getDn().getName());
                entry.add("member",inetOrgPerson.getDn().getName());
            } else {
                logger.error("Identität #{} zu Gruppe nicht hinzugefuegt, da nicht in LDAP gefunden.", identitaet.getId());
                continue;
            }
        }

        LdapConnection ldapConnection = LdapDatabase.getConnection();
        ldapConnection.bind();
        ldapConnection.add(entry);
        ldapConnection.unBind();
        try {
            ldapConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    /**
     * Zentrale Funktion für Aktualisierung eines groupOfName Eintrags in LDAP Directory.
     * Es werden zunächst alle erforderlichen Änderungen analyisiert.
     * Zunaechst Iteration ueber erforderliche Member laut ICA, wenn noch nicht vorhanden, dann hinzufuegen.
     * Dann wird geprueft, ob Member vorhanden sind, die laut ICA nicht erforderlich sind und entsprechend geloescht.
     * Abschließend werden alle gesammelten Modifikationen persistiert.
     *
     * @ToDo Name, Beschreibung aus Berechtigungsgruppe aktualisieren (@todo)
     *
     * @Author Philipp Steinmetzger <philipp.steinmetzger@pfadfinden.de>
     * @param berechtigungsgruppe
     * @param gruppeEntry
     * @param identitaeten
     */
    private void updateBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe, Entry gruppeEntry, Set<IcaIdentitaet> identitaeten) {
        // Sammlung aller Modifikationen für groupOfNames
        Set<DefaultModification> modifications = new HashSet<DefaultModification>();

        // @todo: geht das nicht direkt via Set<IcaIdentitaet> identitaeten?
        Set<Entry> validLdapIdentitaeten = new HashSet<Entry>();

        CommandIdentitaet commandIdentitaet = new CommandIdentitaet();

        for (IcaIdentitaet identitaet : identitaeten) {

            Entry inetOrgPerson = commandIdentitaet.identitaet2Ldap(identitaet.getId());
            validLdapIdentitaeten.add(inetOrgPerson);

            if (gruppeEntry.contains("member", inetOrgPerson.getDn().getName())) {
                logger.debug("Member #{} in Berechtigungsgruppe '{}' bereits enthalten.", identitaet.getId(), berechtigungsgruppe.getTitle());
            } else {
                logger.debug("Member #{} in Berechtigungsgruppe '{}' hinzufuegen.", identitaet.getId(), berechtigungsgruppe.getTitle());
                try {
                    gruppeEntry.get("member").add(inetOrgPerson.getDn().getName());
                } catch (LdapInvalidAttributeValueException e) {
                    logger.error("Konnte Member Berechtiungsgruppe nicht hinzufuegen.", e);
                }
            }
        }

        Iterator iterator = gruppeEntry.get("member").iterator();
        while (iterator.hasNext()) {
            Value<String> value = (Value<String>) iterator.next();
            String identitaetDn = value.getString();
            if(!searchDnIn(validLdapIdentitaeten,identitaetDn)){
                logger.debug("Identiaet {} muss als Member geloescht werden.",identitaetDn);
                iterator.remove();
            }
        }
        modifications.add(new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE,gruppeEntry.get("member")));

        for(DefaultModification modification : modifications){
            if(modification == null) continue;
            try {
                LdapConnection ldapConnection = LdapDatabase.getConnection();
                ldapConnection.bind();
                ldapConnection.modify(gruppeEntry.getDn(),modification);
                ldapConnection.unBind();
                try {
                    ldapConnection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;

            } catch (LdapException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean searchDnIn(Set<Entry> entrySet, String line){
        for(Entry entry: entrySet){
            if(entry.getDn().getName().equals(line)) return true;
        }
        return false;
    }

    private List<SyncTaetigkeit> getTaetigkeitenZuBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe) throws SQLException {
        List<SyncTaetigkeit> taetigkeiten = new ArrayList<SyncTaetigkeit>();

        try (
                Connection connection = SyncDatabase.getConnection();
                PreparedStatement statement = connection.prepareStatement(syncTaetigkeiten);
        ) {
            statement.setInt(1,berechtigungsgruppe.getId());
            try(ResultSet resultSet = statement.executeQuery()){
                while (resultSet.next()) {
                    taetigkeiten.add(new SyncTaetigkeit(resultSet));
                }
            }

        }

        return taetigkeiten;
    }
}