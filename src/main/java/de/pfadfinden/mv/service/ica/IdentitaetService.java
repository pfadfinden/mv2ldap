package de.pfadfinden.mv.service.ica;

import de.pfadfinden.mv.database.IcaDatabase;
import de.pfadfinden.mv.model.IcaIdentitaet;
import de.pfadfinden.mv.model.SyncBerechtigungsgruppe;
import de.pfadfinden.mv.model.SyncTaetigkeit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class IdentitaetService {

    /**
     * Suche in ICA alle Identitaeten, die der SyncTätigkeit entsprechen.
     *
     * @return Liste mit Identitäten oder NULL
     */
    public static Set<IcaIdentitaet> findIdentitaetByTaetigkeit(SyncTaetigkeit syncTaetigkeit) throws SQLException {

        String icaIdentiaeten = "" +
                "SELECT identitaet.*, identitaet.genericField1 AS spitzname " +
                "FROM taetigkeitassignment LEFT JOIN identitaet ON taetigkeitassignment.mitglied_id = identitaet.id " +
                "WHERE taetigkeit_id = ? AND aktivBis IS NULL";

        Set<IcaIdentitaet> icaIdentitaeten = new HashSet<IcaIdentitaet>();
        try (
                Connection connection = IcaDatabase.getConnection();
                PreparedStatement identitaetenByTaetigkeitStatement = connection.prepareStatement(icaIdentiaeten);
        ) {
            identitaetenByTaetigkeitStatement.setInt(1,syncTaetigkeit.getTaetigkeit_id());
            try (ResultSet results = identitaetenByTaetigkeitStatement.executeQuery()) {
                while (results.next()) {
                    icaIdentitaeten.add(new IcaIdentitaet(results));
                }
            }
        }
        if(icaIdentitaeten.size()==0) return null;
        return icaIdentitaeten;
    }

    public static Set<IcaIdentitaet> findIdentitaetByBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe) throws SQLException {
        Set<IcaIdentitaet> icaIdentitaeten = new HashSet<IcaIdentitaet>();

        for(SyncTaetigkeit syncTaetigkeit : berechtigungsgruppe.getTaetigkeiten()){
            icaIdentitaeten.addAll(findIdentitaetByTaetigkeit(syncTaetigkeit));
        }

        if(icaIdentitaeten.size()==0) return null;
        return icaIdentitaeten;
    }

    public static IcaIdentitaet findIdentitaetById(int icaIdentitaet){

        String findIdentitaetById = "SELECT *, identitaet.genericField1 AS spitzname " +
                "FROM identitaet " +
                "WHERE id=?";

        try (
            Connection connection = IcaDatabase.getConnection();
            PreparedStatement findIdentitaetByIdStatement = connection.prepareStatement(findIdentitaetById);
        ){
            findIdentitaetByIdStatement.setInt(1,icaIdentitaet);
            try (ResultSet results = findIdentitaetByIdStatement.executeQuery()) {
                while (results.next()) {
                    return new IcaIdentitaet(results);
                }
            }
        } catch (SQLException e) {}
        return null;
    }
}