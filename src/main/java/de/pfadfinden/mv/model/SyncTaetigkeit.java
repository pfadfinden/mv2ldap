package de.pfadfinden.mv.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SyncTaetigkeit {

    public SyncTaetigkeit(ResultSet rs) throws SQLException {
        setId(rs.getInt("id"));
        setTaetigkeit_id(rs.getInt("taetigkeit_id"));
        setTaetigkeit(rs.getString("taetigkeit"));
        setAbteilung_id(rs.getInt("abteilung_id"));
        setAbteilung(rs.getString("abteilung"));
        setAbteilung_id(rs.getInt("abteilung_id"));
        setGruppierung(rs.getString("gruppierung"));
        setGruppierungId(rs.getInt("gruppierung_id"));
    }

    private int id;
    private int taetigkeit_id;
    private String taetigkeit;
    private int abteilung_id;
    private String abteilung;
    private int gruppierungId;
    private String gruppierung;

    public String getAbteilung() {
        return abteilung;
    }

    public void setAbteilung(String abteilung) {
        this.abteilung = abteilung;
    }

    public int getAbteilung_id() {
        return abteilung_id;
    }

    public int getAbteilungId() {
        return abteilung_id;
    }

    public void setAbteilung_id(int abteilung_id) {
        this.abteilung_id = abteilung_id;
    }

    public String getTaetigkeit() {
        return taetigkeit;
    }

    public void setTaetigkeit(String taetigkeit) {
        this.taetigkeit = taetigkeit;
    }

    public int getTaetigkeit_id() {
        return taetigkeit_id;
    }

    public void setTaetigkeit_id(int taetigkeit_id) {
        this.taetigkeit_id = taetigkeit_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getString(){
        return "#"+getId()+" ("+getTaetigkeit()+")";
    }

    public int getGruppierungId() {
        return gruppierungId;
    }

    public void setGruppierungId(int gruppierungId) {
        this.gruppierungId = gruppierungId;
    }

    public String getGruppierung() {
        return gruppierung;
    }

    public void setGruppierung(String gruppierung) {
        this.gruppierung = gruppierung;
    }


}
