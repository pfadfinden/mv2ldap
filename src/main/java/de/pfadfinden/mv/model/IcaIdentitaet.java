package de.pfadfinden.mv.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class IcaIdentitaet extends IcaRecord implements RowMapper<IcaIdentitaet> {

    @Override
    public IcaIdentitaet mapRow(ResultSet rs, int rowNum) throws SQLException {
        IcaIdentitaet icaIdentitaet = new IcaIdentitaet();
        icaIdentitaet.setId(rs.getInt("id"));
        icaIdentitaet.setMglType(rs.getString("mglType"));
        icaIdentitaet.setStatus(rs.getString("status"));
        icaIdentitaet.setGruppierungId(rs.getInt("gruppierung_id"));
        icaIdentitaet.setEmail(rs.getString("emailEnc"));
        icaIdentitaet.setMitgliedsNummer(rs.getInt("mitgliedsNummer"));
        icaIdentitaet.setHash(rs.getString("hash"));
        icaIdentitaet.setOrt(rs.getString("ortEnc"));
        icaIdentitaet.setPlz(rs.getString("plzEnc"));
        icaIdentitaet.setStrasse(rs.getString("strasseEnc"));
        icaIdentitaet.setNachname(rs.getString("nachnameEnc"));
        icaIdentitaet.setVorname(rs.getString("vornameEnc"));
        icaIdentitaet.setSpitzname(rs.getString("spitzname"));
        icaIdentitaet.setTelefon1(rs.getString("telefon1Enc"));
        icaIdentitaet.setTelefon2(rs.getString("telefon2Enc"));
        icaIdentitaet.setTelefon3(rs.getString("telefon3Enc"));
        icaIdentitaet.setTelefax(rs.getString("telefaxEnc"));
        icaIdentitaet.setVersion(rs.getInt("version"));
        icaIdentitaet.setLastUpdated(rs.getTimestamp("lastUpdated"));
        icaIdentitaet.setCountryCode2(rs.getString("countryCode2"));
        icaIdentitaet.setCountryCode3(rs.getString("countryCode3"));
        icaIdentitaet.setCountryName(rs.getString("countryName"));
        return icaIdentitaet;
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
    private String countryCode2;
    private String countryCode3;
    private String countryName;
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
        this.telefon1 = telefon1.replaceAll("([^0-9-\\s])"," ").trim();
    }

    public String getTelefon2() {
        return telefon2;
    }

    public void setTelefon2(String telefon2) {
        this.telefon2 = telefon2.replaceAll("([^0-9-\\s])"," ").trim();
    }

    public String getTelefon3() {
        return telefon3;
    }

    public void setTelefon3(String telefon3) {
        this.telefon3 = telefon3.replaceAll("([^0-9-\\s])"," ").trim();
    }

    public String getTelefax() {
        return telefax;
    }

    public void setTelefax(String telefax) {
        this.telefax = telefax.replaceAll("([^0-9-\\s])"," ").trim();
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
        if (spitzname == null || spitzname.trim().isEmpty()) return null;
        return spitzname.trim();
    }

    public void setSpitzname(String spitzname) {
        this.spitzname = spitzname;
    }


    public String getCountryCode2() {
        return countryCode2;
    }

    public void setCountryCode2(String countryCode2) {
        this.countryCode2 = countryCode2;
    }

    public String getCountryCode3() {
        return countryCode3;
    }

    public void setCountryCode3(String countryCode3) {
        this.countryCode3 = countryCode3;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCommonName() {
        String cn = new String();

        if(this.getVorname() != null){
            cn+=this.getVorname().trim()+" ";
        }

        if(this.getNachname() != null){
            cn+=this.getNachname().trim();
        }

        if(this.getSpitzname() != null){
            cn+=" ("+this.getSpitzname()+")";
        }
        return cn.trim();
    }

    public String getDisplayName() {
        String cn = new String();

        if(this.getNachname() != null){
            cn+=this.getNachname().trim();
        }

        if(this.getVorname() != null){
            cn+=", "+this.getVorname().trim();
        }

        if(this.getSpitzname() != null){
            cn+=" ("+this.getSpitzname()+")";
        }
        return cn.trim();
    }

}
