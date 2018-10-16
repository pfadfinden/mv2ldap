package de.pfadfinden.mv.service;

import de.pfadfinden.mv.model.IcaGruppierung;
import de.pfadfinden.mv.model.IcaIdentitaet;
import de.pfadfinden.mv.model.SyncBerechtigungsgruppe;
import de.pfadfinden.mv.model.SyncTaetigkeit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IcaService {

    private Logger logger = LoggerFactory.getLogger(IcaService.class);

    private final JdbcTemplate jdbcTemplate;

    public IcaService(@Qualifier("jdbcIca") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Suche in ICA alle Identitaeten, die der SyncTätigkeit entsprechen.
     *
     * @return Liste mit Identitäten
     */
    public List<IcaIdentitaet> findIdentitaetByTaetigkeit(SyncTaetigkeit syncTaetigkeit) {

        if(syncTaetigkeit.getTaetigkeit_id() == 0 && syncTaetigkeit.getGruppierungId() == 0){
            logger.warn("SyncTaetigkeit #{} ueberlesen, da ohne Gruppierung/Taetigkeit.", syncTaetigkeit.getId());
            return Collections.emptyList();
        }

        List<Object> args = new ArrayList<>();

        String icaIdentiaeten = "" +
                "SELECT ident.*, ident.genericField1 AS spitzname, " +
                "Land.countryCode2, Land.countryCode3, Land.name AS countryName " +
                "FROM TaetigkeitAssignment AS ta LEFT JOIN Identitaet AS ident ON ta.mitglied_id = ident.id " +
                "LEFT JOIN Land ON ident.land_id = Land.id " +
                "WHERE ta.aktivVon <= now() AND (ta.aktivBis is null OR ta.aktivBis > now())";

        if(syncTaetigkeit.getTaetigkeit_id() != 0) {
            icaIdentiaeten += " AND ta.taetigkeit_id = ?";
            args.add(syncTaetigkeit.getTaetigkeit_id());
        }

        if(syncTaetigkeit.getAbteilungId() != 0) {
            icaIdentiaeten += " AND ta.Untergliederung_id = ?";
            args.add(syncTaetigkeit.getAbteilungId());
        }

        if(syncTaetigkeit.getGruppierungId() != 0) {
            icaIdentiaeten += " AND ta.gruppierung_id = ?";
            args.add(syncTaetigkeit.getGruppierungId());
        }

        icaIdentiaeten += " ORDER BY ident.nachnameEnc";

        return jdbcTemplate.query(icaIdentiaeten,args.toArray(),new IcaIdentitaet());
    }

    public Set<IcaIdentitaet> findIdentitaetByBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe) {
        Set<IcaIdentitaet> icaIdentitaeten = new HashSet<>();

        for(SyncTaetigkeit syncTaetigkeit : berechtigungsgruppe.getTaetigkeiten()){
            List<IcaIdentitaet> identitaeten = findIdentitaetByTaetigkeit(syncTaetigkeit);
            if(identitaeten == null) {
                logger.debug("Zu SyncTaetigkeit #{} {} keine Identitaeten.",syncTaetigkeit.getId(),syncTaetigkeit.getTaetigkeit());
                continue;
            }
            icaIdentitaeten.addAll(identitaeten);
        }
        if(icaIdentitaeten.size()==0) return null;

        return icaIdentitaeten;
    }

    public Optional<IcaIdentitaet> findIdentitaetById(int icaIdentitaet){

        String findIdentitaetById = "" +
                "SELECT *, Identitaet.genericField1 AS spitzname, " +
                "Land.countryCode2, Land.countryCode3, Land.name AS countryName " +
                "FROM Identitaet " +
                "LEFT JOIN Land ON Identitaet.land_id = Land.id " +
                "WHERE Identitaet.id=?";

        return Optional.ofNullable(jdbcTemplate.queryForObject(findIdentitaetById, new Object[]{icaIdentitaet}, new IcaIdentitaet()));
    }

    public List<IcaGruppierung> getGruppierungen() {

        String icaGruppierungen = "" +
                "SELECT Gruppierung.id,Gruppierung.name,nummer,ebene_id,parent_gruppierung_id,status,migrationID,alteID," +
                "version,lastUpdated,sitzOrt, Ebene.name as ebeneName, Ebene.tiefe " +
                "FROM Gruppierung " +
                "LEFT JOIN Ebene ON Gruppierung.ebene_id = Ebene.id " +
                "WHERE status='AKTIV' " +
                "ORDER BY ebene_id,Gruppierung.id ASC ";

        return jdbcTemplate.query(icaGruppierungen,new IcaGruppierung());
    }
}
