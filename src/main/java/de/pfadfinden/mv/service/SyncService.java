package de.pfadfinden.mv.service;

import de.pfadfinden.mv.model.SyncBerechtigungsgruppe;
import de.pfadfinden.mv.model.SyncTaetigkeit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncService {

    private final Logger logger = LoggerFactory.getLogger(SyncService.class);

    private final JdbcTemplate jdbcTemplate;

    public SyncService(@Qualifier("jdbcSync") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SyncBerechtigungsgruppe> getSyncGruppen() {

        String syncGruppen = "SELECT * FROM berechtigungsgruppe " +
                "WHERE deleted = 0 " +
                "ORDER BY id ASC ";

        return jdbcTemplate.query(syncGruppen,new SyncBerechtigungsgruppe());
    }

    public List<SyncTaetigkeit> getTaetigkeitenZuBerechtigungsgruppe(SyncBerechtigungsgruppe berechtigungsgruppe) {

        String syncTaetigkeiten = "SELECT * FROM taetigkeit " +
                "WHERE berechtigungsgruppe_id = ? " +
                "ORDER BY id ASC ";

        int id = berechtigungsgruppe.getId();

        return jdbcTemplate.query(syncTaetigkeiten,new Object[]{id},new SyncTaetigkeit());
    }

}
