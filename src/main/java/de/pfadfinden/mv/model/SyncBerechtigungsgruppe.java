package de.pfadfinden.mv.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SyncBerechtigungsgruppe implements RowMapper<SyncBerechtigungsgruppe> {

    @Override
    public SyncBerechtigungsgruppe mapRow(ResultSet rs, int rowNum) throws SQLException {
        SyncBerechtigungsgruppe syncBerechtigungsgruppe = new SyncBerechtigungsgruppe();
        syncBerechtigungsgruppe.setId(rs.getInt("id"));
        syncBerechtigungsgruppe.setTitle(rs.getString("title"));
        syncBerechtigungsgruppe.setDescription(rs.getString("description"));
        syncBerechtigungsgruppe.setDeleted(rs.getBoolean("deleted"));
        syncBerechtigungsgruppe.setOwnerGruppierung(rs.getInt("ownerGruppierung"));
        return syncBerechtigungsgruppe;
    }

    private int id;
    private String title;
    private String description;
    private boolean deleted;
    private List<SyncTaetigkeit> taetigkeiten;
    private int ownerGruppierung;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<SyncTaetigkeit> getTaetigkeiten() {
        return taetigkeiten;
    }

    public void setTaetigkeiten(List<SyncTaetigkeit> taetigkeiten) {
        this.taetigkeiten = taetigkeiten;
    }

    public void addTaetigkeit(SyncTaetigkeit taetigkeit){
        taetigkeiten.add(taetigkeit);
    }

    public int getOwnerGruppierung() {
        return ownerGruppierung;
    }

    public void setOwnerGruppierung(int ownerGruppierung) {
        this.ownerGruppierung = ownerGruppierung;
    }

}