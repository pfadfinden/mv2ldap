package de.pfadfinden.mv.service.ica;

import de.pfadfinden.mv.database.IcaDatabase;
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
    static Logger logger = LoggerFactory.getLogger(IdentitaetService.class);

    /**
     * Suche in ICA alle Identitaeten, die der SyncTätigkeit entsprechen.
     *
     * @return Liste mit Identitäten oder NULL
     */
    public static Set<IcaIdentitaet> findIdentitaetByTaetigkeit(SyncTaetigkeit syncTaetigkeit) throws SQLException {

        String icaIdentiaeten = "" +
                "SELECT Identitaet.*, Identitaet.genericField1 AS spitzname, " +
                "Land.countryCode2, Land.countryCode3, Land.name AS countryName " +
                "FROM TaetigkeitAssignment LEFT JOIN Identitaet ON TaetigkeitAssignment.mitglied_id = Identitaet.id " +
                "LEFT JOIN Land ON Identitaet.land_id = Land.id " +
                "WHERE taetigkeit_id = ? AND (aktivBis is null OR aktivBis > now())";

        if(syncTaetigkeit.getAbteilungId() != 0) icaIdentiaeten += " AND TaetigkeitAssignment.Untergliederung_id = ?";
        if(syncTaetigkeit.getGruppierungId() != 0) icaIdentiaeten += " AND TaetigkeitAssignment.gruppierung_id = ?";


        icaIdentiaeten += " ORDER BY Identitaet.nachnameEnc";

        Set<IcaIdentitaet> icaIdentitaeten = new HashSet<IcaIdentitaet>();
        try (
                Connection connection = IcaDatabase.getConnection();
                PreparedStatement identitaetenByTaetigkeitStatement = connection.prepareStatement(icaIdentiaeten);
        ) {
            identitaetenByTaetigkeitStatement.setInt(1,syncTaetigkeit.getTaetigkeit_id());
            if(syncTaetigkeit.getAbteilungId() != 0) identitaetenByTaetigkeitStatement.setInt(2,syncTaetigkeit.getAbteilungId());
            if(syncTaetigkeit.getGruppierungId() != 0){
                if(syncTaetigkeit.getAbteilungId() != 0) {
                    identitaetenByTaetigkeitStatement.setInt(3,syncTaetigkeit.getGruppierungId());
                } else {
                    identitaetenByTaetigkeitStatement.setInt(2,syncTaetigkeit.getGruppierungId());
                }
            }

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
            Set<IcaIdentitaet> identitaeten = findIdentitaetByTaetigkeit(syncTaetigkeit);
            if(identitaeten == null) {
                logger.debug("Zu SyncTaetigkeit #{} {} keine Identitaeten.",syncTaetigkeit.getId(),syncTaetigkeit.getTaetigkeit());
                continue;
            }
            icaIdentitaeten.addAll(identitaeten);
        }
        if(icaIdentitaeten.size()==0) return null;

        return icaIdentitaeten;
    }

    public static IcaIdentitaet findIdentitaetById(int icaIdentitaet){

        String findIdentitaetById = "" +
                "SELECT *, Identitaet.genericField1 AS spitzname, " +
                "Land.countryCode2, Land.countryCode3, Land.name AS countryName " +
                "FROM Identitaet " +
                "LEFT JOIN Land ON Identitaet.land_id = Land.id " +
                "WHERE Identitaet.id=?";

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
        } catch (SQLException e) {
            logger.error("Fehler bei findIdentitaetById.",e);
        }
        return null;
    }
}