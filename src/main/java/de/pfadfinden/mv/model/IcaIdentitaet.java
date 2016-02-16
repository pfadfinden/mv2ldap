package de.pfadfinden.mv.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class IcaIdentitaet extends IcaRecord{

    public IcaIdentitaet(ResultSet rs) throws SQLException {
        setId(rs.getInt("id"));
        this.mglType = rs.getString("mglType");
        this.status = rs.getString("status");
        this.gruppierungId = rs.getInt("gruppierung_id");
        this.email = rs.getString("email");
        this.mitgliedsNummer = rs.getInt("mitgliedsNummer");
        this.hash = rs.getString("hash");
        this.ort = rs.getString("ortEnc");
        this.plz = rs.getString("plzEnc");
        this.strasse = rs.getString("strasseEnc");
        this.nachname = rs.getString("nachnameEnc");
        this.vorname = rs.getString("vornameEnc");
        this.spitzname = rs.getString("spitzname");
        this.telefon1 = rs.getString("telefon1Enc");
        this.telefon2 = rs.getString("telefon2Enc");
        this.telefon3 = rs.getString("telefon3Enc");
        this.telefax = rs.getString("telefaxEnc");
        this.version = rs.getInt("version");
        this.lastUpdated = rs.getDate("lastUpdated");
    }

    private String mglType;
    private String status;
    private int gruppierungId;
    private String email;
    private int mitgliedsNummer;
    private String hash;
    private String ort;
    private String plz;
    private String strasse;
    private String nachname;
    private String vorname;
    private String spitzname;
    private String telefon1;
    private String telefon2;
    private String telefon3;
    private String telefax;
    private int version;
    private Date lastUpdated;

    public String getMglType() {
        return mglType;
    }

    public void setMglType(String mglType) {
        this.mglType = mglType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getGruppierungId() {
        return gruppierungId;
    }

    public void setGruppierungId(int gruppierungId) {
        this.gruppierungId = gruppierungId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getMitgliedsNummer() {
        return mitgliedsNummer;
    }

    public void setMitgliedsNummer(int mitgliedsNummer) {
        this.mitgliedsNummer = mitgliedsNummer;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getOrt() {
        return ort;
    }

    public void setOrt(String ort) {
        this.ort = ort;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getTelefon1() {
        return telefon1;
    }

    public void setTelefon1(String telefon1) {
        this.telefon1 = telefon1;
    }

    public String getTelefon2() {
        return telefon2;
    }

    public void setTelefon2(String telefon2) {
        this.telefon2 = telefon2;
    }

    public String getTelefon3() {
        return telefon3;
    }

    public void setTelefon3(String telefon3) {
        this.telefon3 = telefon3;
    }

    public String getTelefax() {
        return telefax;
    }

    public void setTelefax(String telefax) {
        this.telefax = telefax;
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

    public String getSpitzname() {
        return spitzname;
    }

    public void setSpitzname(String spitzname) {
        this.spitzname = spitzname;
    }
}
