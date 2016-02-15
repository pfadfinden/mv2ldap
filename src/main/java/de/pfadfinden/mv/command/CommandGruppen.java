package de.pfadfinden.mv.command;

import de.pfadfinden.mv.database.LdapTemplateDatabase;
import de.pfadfinden.mv.database.SyncDatabase;
import de.pfadfinden.mv.ldap.EntryServiceLdap;
import de.pfadfinden.mv.ldap.schema.Gruppe;
import de.pfadfinden.mv.model.IcaIdentitaet;
import de.pfadfinden.mv.model.SyncBerechtigungsgruppe;
import de.pfadfinden.mv.model.SyncTaetigkeit;
import de.pfadfinden.mv.service.ica.IdentitaetService;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.entry.*;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;
import org.apache.directory.ldap.client.template.RequestBuilder;
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

    public final String syncGruppen = "SELECT * FROM berechtigungsgruppe " +
            "WHERE deleted = 0 " +
            "ORDER BY id ASC ";

    public final String syncTaetigkeiten = "SELECT * FROM taetigkeit " +
            "WHERE berechtigungsgruppe_id = ? " +
            "ORDER BY id ASC ";

    public CommandGruppen() throws SQLException, LdapException, IOException {

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

        Gruppe gruppe = EntryServiceLdap.findGruppeById(berechtigungsgruppe.getId());

        if(gruppe == null){
            logger.info("Berechtigungsgruppe in LDAP nicht vorhanden.");
            addBerechtigungsgruppe(berechtigungsgruppe,identitaeten);
        } else {
            logger.info("Berechtigungsgruppe in LDAP vorhanden: {}",gruppe.getDn());
            updateBerechtigungsgruppe(berechtigungsgruppe,gruppe,identitaeten);
        }
    }

    private void addBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe, Set<IcaIdentitaet> identitaeten) throws CursorException, LdapException {

        String baseDn = "dc=example,dc=com";

        Dn dn = new Dn(
                "cn", berechtigungsgruppe.getTitle(),
                baseDn
        );

        logger.debug("DN: {}",dn);

        CommandIdentitaet commandIdentitaet = new CommandIdentitaet();

        AddResponse addResponse = LdapTemplateDatabase.getLdapConnectionTemplate().add(
                dn,
                new RequestBuilder<AddRequest>() {
                    @Override
                    public void buildRequest(AddRequest request) throws LdapException {
                        Entry entry = request.getEntry();
                        entry.add("ObjectClass","groupOfNames", "icaRecord");
                        if(berechtigungsgruppe.getDescription()!=null) entry.add("description",berechtigungsgruppe.getDescription());
                        entry.add("icaId",String.valueOf(berechtigungsgruppe.getId()));
                        entry.add("icaLastUpdated",new GeneralizedTime(new Date()).toString());

                        if(identitaeten.size() == 0) return;
                        for(IcaIdentitaet identitaet: identitaeten) {
                            logger.debug("Identität #{} muss hinzugefuegt werden", identitaet.getId());
                            de.pfadfinden.mv.ldap.schema.IcaIdentitaet inetOrgPerson = commandIdentitaet.identitaet2Ldap(identitaet.getId());
                            if (inetOrgPerson != null){
                                logger.debug("Hinzufuegen Identitaet {}",inetOrgPerson.getDn().getName());
                                entry.add("member",inetOrgPerson.getDn().getName());
                            } else {
                                logger.error("Identität #{} zu Gruppe nicht hinzugefuegt, da nicht in LDAP gefunden.", identitaet.getId());
                                continue;
                            }
                        }

                    }
                }
        );

        if (addResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
            logger.error(addResponse.getLdapResult().getDiagnosticMessage());
        }

        return;
    }

    private void updateBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe, Gruppe gruppeLdap, Set<IcaIdentitaet> identitaeten) {
        LdapConnectionTemplate ldapConnectionTemplate = LdapTemplateDatabase.getLdapConnectionTemplate();

        CommandIdentitaet commandIdentitaet = new CommandIdentitaet();

        ModifyResponse modifyResponse = ldapConnectionTemplate.modify(
                ldapConnectionTemplate.newDn(gruppeLdap.getDn().toString()),
                new RequestBuilder<ModifyRequest>() {
                    @Override
                    public void buildRequest(ModifyRequest request) throws LdapException {
                        request.remove("member");
                        for(IcaIdentitaet identitaet : identitaeten){
                            de.pfadfinden.mv.ldap.schema.IcaIdentitaet inetOrgPerson = commandIdentitaet.identitaet2Ldap(identitaet.getId());
                            request.add("member",inetOrgPerson.getDn().getName());
                        }
                    }
                }
        );

        if (modifyResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
            logger.error(modifyResponse.getLdapResult().getDiagnosticMessage());
        }
        return;
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