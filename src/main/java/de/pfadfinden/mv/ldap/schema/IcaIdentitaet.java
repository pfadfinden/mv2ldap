package de.pfadfinden.mv.ldap.schema;

import de.pfadfinden.mv.ldap.mapper.IcaIdentitaetMapper;
import org.apache.directory.ldap.client.template.EntryMapper;

public class IcaIdentitaet extends IcaRecord {

    public IcaIdentitaet(){}

    public static EntryMapper<IcaIdentitaet> getEntryMapper() {
        return new IcaIdentitaetMapper();
    }

    private String icaHash;
    private int icaMitgliedsnummer;
    private String icaSpitzname;

    private String cn;
    private String sn;
    private String givenName;
    private String facsimileTelephoneNumber;
    private String l;
    private String mail;
    private String mobile;
    private String postalCode;
    private String street;
    private String telephoneNumber;

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFacsimileTelephoneNumber() {
        return facsimileTelephoneNumber;
    }

    public void setFacsimileTelephoneNumber(String facsimileTelephoneNumber) {
        this.facsimileTelephoneNumber = facsimileTelephoneNumber;
    }

    public String getIcaHash() {
        return icaHash;
    }

    public void setIcaHash(String icaHash) {
        this.icaHash = icaHash;
    }

    public int getIcaMitgliedsnummer() {
        return icaMitgliedsnummer;
    }

    public void setIcaMitgliedsnummer(int icaMitgliedsnummer) {
        this.icaMitgliedsnummer = icaMitgliedsnummer;
    }

    public String getIcaSpitzname() {
        return icaSpitzname;
    }

    public void setIcaSpitzname(String icaSpitzname) {
        this.icaSpitzname = icaSpitzname;
    }


    public String getL() {
        return l;
    }

    public void setL(String l) {
        this.l = l;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }
}
