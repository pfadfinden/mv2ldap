package de.pfadfinden.mv.command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.util.Date;

import de.pfadfinden.mv.connector.ConnectorLDAP;
import de.pfadfinden.mv.model.IcaGruppierung;
import de.pfadfinden.mv.service.IcaRecordService;
import de.pfadfinden.mv.tools.LdapHelper;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.*;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.pfadfinden.mv.connector.ConnectorICA;

public class CommandGruppierungen {
    final Logger logger = LoggerFactory.getLogger(CommandGruppierungen.class);

    public static Connection connectionICA;
    public static LdapConnection connectionLDAP;

    public IcaRecordService icaRecordService;

    PreparedStatement icaGruppierungenStatement;

    public final String icaGruppierungen = "" +
            "SELECT gruppierung.id,gruppierung.name,nummer,ebene_id,parent_gruppierung_id,status,migrationID,alteID," +
            "version,lastUpdated,sitzOrt, ebene.name as ebeneName, ebene.tiefe " +
            "FROM gruppierung " +
            "LEFT JOIN ebene ON gruppierung.ebene_id = ebene.id " +
            "WHERE status='AKTIV' " +
            "ORDER BY ebene_id,gruppierung.id ASC ";

    public CommandGruppierungen() throws Exception {
        connectionICA = ConnectorICA.getConnection();
        connectionLDAP = ConnectorLDAP.getConnection();
        icaGruppierungenStatement = connectionICA.prepareStatement(icaGruppierungen);
        icaRecordService = new IcaRecordService();
        ResultSet icaGruppierungenResultset = icaGruppierungenStatement.executeQuery();
        IcaGruppierung gruppierung;
        while (icaGruppierungenResultset.next()) {
            gruppierung = new IcaGruppierung(icaGruppierungenResultset);
            execGruppierung(gruppierung);
        }
        connectionICA.close();
        connectionLDAP.unBind();
        connectionLDAP.close();
    }

    private void execGruppierung(IcaGruppierung gruppierung) throws Exception {
        logger.debug("# Start Verarbeitung Gruppierung {} ({})",gruppierung.getId(),gruppierung.getName());
        Entry gruppierungEntry = icaRecordService.findGruppierungById(gruppierung.getId());

        if(gruppierungEntry == null){
            addGruppierung(gruppierung);
        } else {
            if(needUpdateGruppierung(gruppierung,gruppierungEntry)){
                updateGruppierung(gruppierung,gruppierungEntry);
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
    private boolean needUpdateGruppierung(IcaGruppierung gruppierungIca, Entry gruppierungLdap){

        try {
            if(gruppierungLdap.containsAttribute("icaProtected") &&
                    Boolean.valueOf(gruppierungLdap.get("icaProtected").getString())) return false;

        } catch (LdapInvalidAttributeValueException e) {
            e.printStackTrace();
        }

        try {
            if(gruppierungLdap.get("icaLastUpdated")== null) return true;
            GeneralizedTime ldapLastUpdatedGenerialized = new GeneralizedTime(gruppierungLdap.get("icaLastUpdated").getString());
            if(ldapLastUpdatedGenerialized.getDate().before(gruppierungIca.getLastUpdated())) return true;
        } catch (LdapInvalidAttributeValueException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Fuehre Aktualisierung einer Gruppierung durch, indem Attribute ergazent, aktualisiert oder geloescht werden.
     *
     * @return void
     * @author Philipp Steinmetzger
     */
    private void updateGruppierung(IcaGruppierung gruppierungIca, Entry gruppierungLdap) throws LdapException {
        logger.debug("Update Entry: {}",gruppierungIca.getId());

    //    Keine Modifikation der OU (Bestandteil DN) durchfuehren
    //    DefaultModification ouMod = checkAttributeValue(gruppierungLdap,gruppierungIca.getName(),"ou");
    //    if(ouMod != null) connectionLDAP.modify(gruppierungLdap.getDn(),ouMod);

        DefaultModification icaEbeneMod = LdapHelper.checkAttributeValue(gruppierungLdap,gruppierungIca,gruppierungIca.getEbeneName(),"icaEbene");
        if(icaEbeneMod != null) connectionLDAP.modify(gruppierungLdap.getDn(),icaEbeneMod);
        DefaultModification icaStatusMod = LdapHelper.checkAttributeValue(gruppierungLdap,gruppierungIca,gruppierungIca.getStatus(),"icaStatus");
        if(icaStatusMod != null) connectionLDAP.modify(gruppierungLdap.getDn(),icaStatusMod);
        DefaultModification icaVersionMod = LdapHelper.checkAttributeValue(gruppierungLdap,gruppierungIca,gruppierungIca.getVersion(),"icaVersion");
        if(icaVersionMod != null) connectionLDAP.modify(gruppierungLdap.getDn(),icaVersionMod);
        DefaultModification icaSitzOrt = LdapHelper.checkAttributeValue(gruppierungLdap,gruppierungIca,gruppierungIca.getSitzOrt(),"icaSitzOrt");
        if(icaSitzOrt != null) connectionLDAP.modify(gruppierungLdap.getDn(),icaSitzOrt);
        DefaultModification icaLastUpdatedMod = LdapHelper.checkAttributeValue(gruppierungLdap,gruppierungIca,gruppierungIca.getLastUpdated(),"icaLastUpdated");
        if(icaLastUpdatedMod != null) connectionLDAP.modify(gruppierungLdap.getDn(),icaLastUpdatedMod);
    }

    private void addGruppierung(IcaGruppierung gruppierung) throws Exception {

        String baseDn = "dc=example,dc=com";

        Dn dn;
        Entry parentEntry = findParentGruppierung(gruppierung);
        if(parentEntry != null){
            logger.debug("Parent Entry: {}",parentEntry.getDn());
            dn = new Dn(
                    "ou", gruppierung.getName(),
                    parentEntry.getDn().getName()
            );

        } else {
            dn = new Dn(
                    "ou", gruppierung.getName(),
                    baseDn
            );
        }

        logger.debug("DN {}",dn);

        Entry entry = new DefaultEntry();
        dn.add(new Rdn("ou="+gruppierung.getName()));
        entry.add("ObjectClass","top");
        entry.add("ObjectClass","organizationalUnit");
        entry.add("ObjectClass","icaGruppierung");
        entry.add("icaId",String.valueOf(gruppierung.getId()));
        entry.add("icaStatus",gruppierung.getStatus());
        entry.add("icaVersion",String.valueOf(gruppierung.getVersion()));
        if(gruppierung.getLastUpdated() != null) {
            entry.add("icaLastUpdated", new GeneralizedTime(gruppierung.getLastUpdated()).toString());
        }
        entry.add("icaEbene",gruppierung.getEbeneName());
        if(!gruppierung.getSitzOrt().isEmpty()) entry.add("icaSitzOrt",gruppierung.getSitzOrt());

        entry.setDn(dn);
        logger.debug(dn.toString());

        connectionLDAP.add(entry);
        return;

    }

    private Entry findParentGruppierung(IcaGruppierung gruppierung) throws Exception {
        logger.debug("Suche Parent Gruppierung zu {} (ParentID: {})",gruppierung.getName(),gruppierung.getParentGruppierungId());
        Entry entry = icaRecordService.findGruppierungById(gruppierung.getParentGruppierungId());
        if(entry == null){
            logger.error("Zu Gruppierung #{} ist Parent #{} nicht vorhanden.",
                    gruppierung.getId(),gruppierung.getParentGruppierungId());
        }
        return icaRecordService.findGruppierungById(gruppierung.getParentGruppierungId());
    }


}
