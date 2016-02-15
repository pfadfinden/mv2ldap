package de.pfadfinden.mv.ldap.schema;

import org.apache.directory.api.ldap.model.name.Dn;

import java.util.Date;

public abstract class IcaRecord {

    private Dn dn;
    private boolean icaProtected;
    private Date icaLastUpdated;
    private String icaMigrationId;
    private String icaStatus;
    private int icaVersion;
    private int icaId;

    public Dn getDn() {
        return dn;
    }

    public void setDn(Dn dn) {
        this.dn = dn;
    }

    public boolean isIcaProtected() {
        return icaProtected;
    }

    public void setIcaProtected(boolean icaProtected) {
        this.icaProtected = icaProtected;
    }

    public Date getIcaLastUpdated() {
        return icaLastUpdated;
    }

    public void setIcaLastUpdated(Date icaLastUpdated) {
        this.icaLastUpdated = icaLastUpdated;
    }

    public String getIcaMigrationId() {
        return icaMigrationId;
    }

    public void setIcaMigrationId(String icaMigrationId) {
        this.icaMigrationId = icaMigrationId;
    }

    public String getIcaStatus() {
        return icaStatus;
    }

    public void setIcaStatus(String icaStatus) {
        this.icaStatus = icaStatus;
    }

    public int getIcaVersion() {
        return icaVersion;
    }

    public void setIcaVersion(int icaVersion) {
        this.icaVersion = icaVersion;
    }

    public int getIcaId() {
        return icaId;
    }

    public void setIcaId(int icaId) {
        this.icaId = icaId;
    }
}
