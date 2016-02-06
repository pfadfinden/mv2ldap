package de.pfadfinden.mv.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SyncBerechtigungsgruppe {

    public SyncBerechtigungsgruppe(ResultSet rs) throws SQLException {
        setId(rs.getInt("id"));
        setTitle(rs.getString("title"));
        setDescription(rs.getString("description"));
        setDeleted(rs.getBoolean("deleted"));
    }

    private int id;
    private String title;
    private String description;
    private boolean deleted;
    private List<SyncTaetigkeit> taetigkeiten;

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

}