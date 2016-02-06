package de.pfadfinden.mv.service.ica;

import de.pfadfinden.mv.connector.ConnectorICA;
import de.pfadfinden.mv.model.IcaIdentitaet;
import de.pfadfinden.mv.model.SyncBerechtigungsgruppe;
import de.pfadfinden.mv.model.SyncTaetigkeit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IdentitaetService {

    private String icaIdentiaeten = "" +
            "SELECT identitaet.*, identitaet.genericField1 AS spitzname " +
            "FROM taetigkeitassignment LEFT JOIN identitaet ON taetigkeitassignment.mitglied_id = identitaet.id " +
            "WHERE taetigkeit_id = ? AND aktivBis IS NULL";

    private Connection connectionICA;
    private PreparedStatement identitaetenByTaetigkeitStatement;

    public IdentitaetService(){
        try {
            connectionICA = ConnectorICA.getConnection();
            identitaetenByTaetigkeitStatement = connectionICA.prepareStatement(icaIdentiaeten);
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

}
