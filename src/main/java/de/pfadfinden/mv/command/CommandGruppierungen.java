package de.pfadfinden.mv.command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import de.pfadfinden.mv.database.IcaDatabase;
import de.pfadfinden.mv.database.LdapDatabase;
import de.pfadfinden.mv.ldap.EntryServiceLdap;
import de.pfadfinden.mv.model.IcaGruppierung;
import de.pfadfinden.mv.tools.LdapHelper;
import org.apache.directory.api.ldap.model.entry.*;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.ldap.client.template.RequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandGruppierungen {
    final Logger logger = LoggerFactory.getLogger(CommandGruppierungen.class);

    public final String icaGruppierungen = "" +
            "SELECT gruppierung.id,gruppierung.name,nummer,ebene_id,parent_gruppierung_id,status,migrationID,alteID," +
            "version,lastUpdated,sitzOrt, ebene.name as ebeneName, ebene.tiefe " +
            "FROM gruppierung " +
            "LEFT JOIN ebene ON gruppierung.ebene_id = ebene.id " +
            "WHERE status='AKTIV' " +
            "ORDER BY ebene_id,gruppierung.id ASC ";

    public CommandGruppierungen() throws SQLException, LdapException {
        IcaGruppierung gruppierung;

        try(
            Connection connection = IcaDatabase.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(icaGruppierungen);
        ){
            try (ResultSet icaGruppierungenResultset = preparedStatement.executeQuery()) {
                while (icaGruppierungenResultset.next()) {
                    gruppierung = new IcaGruppierung(icaGruppierungenResultset);
                    execGruppierung(gruppierung);
                }
            }
        }
    }

    private void execGruppierung(IcaGruppierung gruppierung) throws LdapException {
        logger.debug("# Start Verarbeitung Gruppierung {} ({})",gruppierung.getId(),gruppierung.getName());
        de.pfadfinden.mv.ldap.schema.IcaGruppierung ldapGruppierung = EntryServiceLdap.findIcaGruppierungById(gruppierung.getId());

        if(ldapGruppierung == null){
            addGruppierung(gruppierung);
        } else {
            if(needUpdateGruppierung(gruppierung,ldapGruppierung)){
                updateGruppierung(gruppierung,ldapGruppierung);
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
        if(gruppierungLdap.getIcaLastUpdated()== null) return true;
        if(gruppierungLdap.getIcaLastUpdated().before(gruppierungIca.getLastUpdated())) return true;
        return false;
    }

    /**
     * Fuehre Aktualisierung einer Gruppierung durch, indem Attribute ergazent, aktualisiert oder geloescht werden.
     *
     * @return void
     * @author Philipp Steinmetzger
     */
    private void updateGruppierung(IcaGruppierung gruppierungIca, de.pfadfinden.mv.ldap.schema.IcaGruppierung gruppierungLdap) throws LdapException {
        //    Keine Modifikation der OU (Bestandteil DN) durchfuehren
        ModifyResponse modResponse = LdapDatabase.getLdapConnectionTemplate().modify(
                LdapDatabase.getLdapConnectionTemplate().newDn(gruppierungLdap.getDn().toString()),
                new RequestBuilder<ModifyRequest>() {
                    @Override
                    public void buildRequest(ModifyRequest request) throws LdapException
                    {
                        LdapHelper.stringUpdateHelper(request,gruppierungIca.getEbeneId(),gruppierungLdap.getIcaEbene(),"icaEbene");
                        LdapHelper.stringUpdateHelper(request,gruppierungIca.getSitzOrt(),gruppierungLdap.getIcaSitzOrt(),"icaSitzOrt");
                        LdapHelper.stringUpdateHelper(request,gruppierungIca.getVersion(),gruppierungLdap.getIcaVersion(),"icaVersion");
                        LdapHelper.stringUpdateHelper(request,gruppierungIca.getStatus(),gruppierungLdap.getIcaStatus(),"icaStatus");
                        if(gruppierungLdap.getIcaLastUpdated() == null){
                            request.add("icaLastUpdated",new GeneralizedTime(new Date()).toGeneralizedTime());
                        } else {
                            request.replace("icaLastUpdated",new GeneralizedTime(new Date()).toGeneralizedTime());
                        }
                    }
                }
        );

        if (modResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
            logger.error(modResponse.getLdapResult().getDiagnosticMessage());
        }
    }


    private void addGruppierung(IcaGruppierung gruppierung) throws LdapInvalidDnException {

        Dn dn;
        de.pfadfinden.mv.ldap.schema.IcaGruppierung parentGruppierung = findParentGruppierung(gruppierung);
        if(parentGruppierung != null){
            logger.debug("Parent Entry: {}",parentGruppierung.getDn());
            dn = new Dn(
                    "ou", gruppierung.getName(),
                    parentGruppierung.getDn().getName()
            );

        } else {
            dn = new Dn(
                    "ou", gruppierung.getName(),
                    "dc=example,dc=com"
            );
        }

        LdapDatabase.getLdapConnectionTemplate().add(
                dn,
                new RequestBuilder<AddRequest>() {
                    @Override
                    public void buildRequest(AddRequest request) throws LdapException {
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
                }
        );

    }

    private de.pfadfinden.mv.ldap.schema.IcaGruppierung findParentGruppierung(IcaGruppierung gruppierung){
        logger.debug("Suche Parent Gruppierung zu {} (ParentID: {})",gruppierung.getName(),gruppierung.getParentGruppierungId());
        return EntryServiceLdap.findIcaGruppierungById(gruppierung.getParentGruppierungId());
    }

}
