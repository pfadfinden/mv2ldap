package de.pfadfinden.mv.command;

import de.pfadfinden.mv.connector.ConnectorLDAP;
import de.pfadfinden.mv.connector.ConnectorSync;
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
import org.w3c.dom.Attr;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CommandGruppen {
    final Logger logger = LoggerFactory.getLogger(CommandGruppen.class);

    public static Connection connectionSync;
    public static LdapConnection connectionLDAP;

    private IdentitaetService identitaetService;

    PreparedStatement syncGruppenStatement;
    PreparedStatement syncTaetigkeitenStatement;

    IcaRecordService icaRecordService;

    public final String syncGruppen = "SELECT * FROM berechtigungsgruppe " +
            "WHERE deleted = 0 " +
            "ORDER BY id ASC ";

    public final String syncTaetigkeiten = "SELECT * FROM taetigkeit " +
            "WHERE berechtigungsgruppe_id = ? " +
            "ORDER BY id ASC ";

    public CommandGruppen() throws SQLException, LdapException, IOException {
        connectionSync = ConnectorSync.getConnection();
        connectionLDAP = ConnectorLDAP.getConnection();

        icaRecordService = new IcaRecordService();
        identitaetService = new IdentitaetService();
        syncGruppenStatement = connectionSync.prepareStatement(syncGruppen);
        syncTaetigkeitenStatement = connectionSync.prepareStatement(syncTaetigkeiten);

        ResultSet berechtigungsgruppenResultset = syncGruppenStatement.executeQuery();

        SyncBerechtigungsgruppe berechtigungsgruppe;
        while (berechtigungsgruppenResultset.next()) {
            berechtigungsgruppe = new SyncBerechtigungsgruppe(berechtigungsgruppenResultset);
            berechtigungsgruppe.setTaetigkeiten(getTaetigkeitenZuBerechtigungsgruppe(berechtigungsgruppe));

            Set<IcaIdentitaet> identitaetenZurBerechtigungsgruppe = identitaetService.findIdentitaetByBerechtigungsgruppe(berechtigungsgruppe);

            try {
                execBerechtigungsgruppe(berechtigungsgruppe,identitaetenZurBerechtigungsgruppe);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        connectionSync.close();
        connectionLDAP.unBind();
        connectionLDAP.close();
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
        entry.add("description",berechtigungsgruppe.getDescription());
        entry.add("icaId",String.valueOf(berechtigungsgruppe.getId()));
        entry.add("icaLastUpdated",new GeneralizedTime(new Date()).toString());

    //    entry.add("member","cn=philipp.steinmetzger,ou=Barrakuda,ou=München (Bayern),ou=Bayern,ou=BdP,dc=example,dc=com");

        if(identitaeten.size() == 0) return;
        for(IcaIdentitaet identitaet: identitaeten) {
            logger.debug("Identität #{} muss hinzugefuegt werden", identitaet.getId());
            Entry addIdentitaet = icaRecordService.findIdentitaetById(identitaet.getId());
            if (addIdentitaet != null){
                logger.debug("Hinzufuegen Identitaet {}",addIdentitaet.getDn().getName());
                entry.add("member",addIdentitaet.getDn().getName());
            } else {
                logger.error("Identität #{} zu Gruppe nicht hinzugefuegt, da nicht in LDAP gefunden.", identitaet.getId());
                continue;
            }
        }

        connectionLDAP.add(entry);
        return;
    }

    private void updateBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe, Entry gruppeEntry, Set<IcaIdentitaet> identitaeten) {
        // @todo: Namen und Beschreibung von Berechtigungsgruppe aktualisieren

        Set<Entry> validLdapIdentitaeten = new HashSet<Entry>();
        Set<DefaultModification> modifications = new HashSet<DefaultModification>();

        for (IcaIdentitaet identitaet : identitaeten) {
            Entry ldapIdentitaet = icaRecordService.findIdentitaetById(identitaet.getId());
            if (ldapIdentitaet == null) continue;
            validLdapIdentitaeten.add(ldapIdentitaet);

            if (gruppeEntry.contains("member", ldapIdentitaet.getDn().getName())) {
                logger.debug("Member #{} in Berechtigungsgruppe '{}' bereits enthalten.", identitaet.getId(), berechtigungsgruppe.getTitle());
            } else {
                logger.debug("Member #{} in Berechtigungsgruppe '{}' hinzufuegen.", identitaet.getId(), berechtigungsgruppe.getTitle());
                modifications.add(new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, "member", ldapIdentitaet.getDn().getName()));
            }

            Iterator iterator = gruppeEntry.get("member").iterator();
            while (iterator.hasNext()) {
                Value<?> value = (Value<?>) iterator.next();
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
                    connectionLDAP.modify(gruppeEntry.getDn(),modification);
                } catch (LdapException e) {
                    e.printStackTrace();
                }
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
        syncTaetigkeitenStatement.clearParameters();
        syncTaetigkeitenStatement.setInt(1,berechtigungsgruppe.getId());

        ResultSet taetigkeitenResultset = syncTaetigkeitenStatement.executeQuery();
        SyncTaetigkeit syncTaetigkeit;

        List<SyncTaetigkeit> taetigkeiten = new ArrayList<SyncTaetigkeit>();
        while (taetigkeitenResultset.next()) {
            syncTaetigkeit = new SyncTaetigkeit(taetigkeitenResultset);
            taetigkeiten.add(syncTaetigkeit);
        }

        return taetigkeiten;
    }
}