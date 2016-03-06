package de.pfadfinden.mv.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class IcaGruppierung extends IcaRecord {

    public IcaGruppierung(){}

    public IcaGruppierung(ResultSet rs) throws SQLException {
        setId(rs.getInt("id"));
        this.name = rs.getString("name");
        this.ebeneId = rs.getInt("ebene_id");
        this.parentGruppierungId = rs.getInt("parent_gruppierung_id");
        this.status = rs.getString("status");
        this.nummer = rs.getString("nummer");
        this.migrationId = rs.getInt("migrationID");
        this.alteId = rs.getString("alteID");
        this.version = rs.getInt("version");
        this.lastUpdated = rs.getTimestamp("lastUpdated");
        this.sitzOrt = rs.getString("sitzOrt");
        this.ebeneName = rs.getString("ebeneName");
    }

    private String name;
    private int ebeneId;
    private int parentGruppierungId;
    private String status;
    private String nummer;
    private int migrationId;
    private String alteId;
    private int version;
    private Date lastUpdated;
    private String sitzOrt;
    private String ebeneName;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEbeneId() {
        return ebeneId;
    }

    public void setEbeneId(int ebeneId) {
        this.ebeneId = ebeneId;
    }

    public int getParentGruppierungId() {
        return parentGruppierungId;
    }

    public void setParentGruppierungId(int parentGruppierungId) {
        this.parentGruppierungId = parentGruppierungId;
    }

    public String getNummer() {
        return nummer;
    }

    public void setNummer(String nummer) {
        this.nummer = nummer;
    }

    public int getMigrationId() {
        return migrationId;
    }

    public void setMigrationId(int migrationId) {
        this.migrationId = migrationId;
    }

    public String getAlteId() {
        return alteId;
    }

    public void setAlteId(String alteId) {
        this.alteId = alteId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getSitzOrt() {
        return sitzOrt;
    }

    public void setSitzOrt(String sitzOrt) {
        this.sitzOrt = sitzOrt;
    }

    public String getEbeneName() {
        return ebeneName;
    }

    public void setEbeneName(String ebeneName) {
        this.ebeneName = ebeneName;
    }
}
