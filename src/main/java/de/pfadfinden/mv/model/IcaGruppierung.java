package de.pfadfinden.mv.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class IcaGruppierung extends IcaRecord implements RowMapper<IcaGruppierung> {

    public IcaGruppierung(){}

    @Override
    public IcaGruppierung mapRow(ResultSet rs, int rowNum) throws SQLException {
        IcaGruppierung icaGruppierung = new IcaGruppierung();
        icaGruppierung.setId(rs.getInt("id"));
        icaGruppierung.setName(rs.getString("name"));
        icaGruppierung.setEbeneId(rs.getInt("ebene_id"));
        icaGruppierung.setParentGruppierungId(rs.getInt("parent_gruppierung_id"));
        icaGruppierung.setStatus(rs.getString("status"));
        icaGruppierung.setNummer(rs.getString("nummer"));
        icaGruppierung.setMigrationId(rs.getInt("migrationID"));
        icaGruppierung.setAlteId(rs.getString("alteID"));
        icaGruppierung.setVersion(rs.getInt("version"));
        icaGruppierung.setLastUpdated(rs.getTimestamp("lastUpdated"));
        icaGruppierung.setSitzOrt(rs.getString("sitzOrt"));
        icaGruppierung.setEbeneName(rs.getString("ebeneName"));
        return icaGruppierung;
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
