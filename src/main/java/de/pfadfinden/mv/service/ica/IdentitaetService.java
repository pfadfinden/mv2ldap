package de.pfadfinden.mv.service.ica;

import de.pfadfinden.mv.connector.ConnectorICA;
import de.pfadfinden.mv.model.IcaIdentitaet;
import de.pfadfinden.mv.model.SyncBerechtigungsgruppe;
import de.pfadfinden.mv.model.SyncTaetigkeit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class IdentitaetService {
    final Logger logger = LoggerFactory.getLogger(IdentitaetService.class);


    private String icaIdentiaeten = "" +
            "SELECT identitaet.*, identitaet.genericField1 AS spitzname " +
            "FROM taetigkeitassignment LEFT JOIN identitaet ON taetigkeitassignment.mitglied_id = identitaet.id " +
            "WHERE taetigkeit_id = ? AND aktivBis IS NULL";

    private String findIdentitaetById = "SELECT *, identitaet.genericField1 AS spitzname " +
            "FROM identitaet " +
            "WHERE id=?";

    private Connection connectionICA;
    private PreparedStatement identitaetenByTaetigkeitStatement;
    private PreparedStatement findIdentitaetByIdStatement;

    public IdentitaetService(){
        try {
            connectionICA = ConnectorICA.getConnection();
            identitaetenByTaetigkeitStatement = connectionICA.prepareStatement(icaIdentiaeten);
            findIdentitaetByIdStatement = connectionICA.prepareStatement(findIdentitaetById);
//            connectionICA.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Suche in ICA alle Identitaeten, die der SyncTätigkeit entsprechen.
     *
     * @return Liste mit Identitäten oder NULL
     */
    public Set<IcaIdentitaet> findIdentitaetByTaetigkeit(SyncTaetigkeit syncTaetigkeit) throws SQLException {
        Set<IcaIdentitaet> icaIdentitaeten = new HashSet<IcaIdentitaet>();

        identitaetenByTaetigkeitStatement.clearParameters();
        identitaetenByTaetigkeitStatement.setInt(1,syncTaetigkeit.getTaetigkeit_id());
        ResultSet results = identitaetenByTaetigkeitStatement.executeQuery();

        IcaIdentitaet icaIdentitaet;
        while (results.next()) {
            icaIdentitaet = new IcaIdentitaet(results);
            icaIdentitaeten.add(icaIdentitaet);
        }
        if(icaIdentitaeten.size()==0) return null;
        return icaIdentitaeten;
    }

    public Set<IcaIdentitaet> findIdentitaetByBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe) throws SQLException {
        Set<IcaIdentitaet> icaIdentitaeten = new HashSet<IcaIdentitaet>();

        for(SyncTaetigkeit syncTaetigkeit : berechtigungsgruppe.getTaetigkeiten()){
            icaIdentitaeten.addAll(findIdentitaetByTaetigkeit(syncTaetigkeit));
        }

        if(icaIdentitaeten.size()==0) return null;
        return icaIdentitaeten;
    }

    public IcaIdentitaet findIdentitaetById(int icaIdentitaet){
        try {
            findIdentitaetByIdStatement.clearParameters();
            findIdentitaetByIdStatement.setInt(1,icaIdentitaet);
            ResultSet results = findIdentitaetByIdStatement.executeQuery();
            while (results.next()) {
                return new IcaIdentitaet(results);
            }
        } catch (SQLException e) {
            logger.error("Fehler bei Zugriff auf ICA.",e);
        }

       // finally {
       //     logger.error("Identität #{} in ICA nicht gefunden.",icaIdentitaet);
            return null;
       // }
    }
}