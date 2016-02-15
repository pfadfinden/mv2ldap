package de.pfadfinden.mv.ldap.schema;

import de.pfadfinden.mv.ldap.mapper.IcaGruppierungMapper;
import org.apache.directory.ldap.client.template.EntryMapper;

public class IcaGruppierung extends IcaRecord {

    public IcaGruppierung(){}

    public static EntryMapper<IcaGruppierung> getEntryMapper() {
        return new IcaGruppierungMapper();
    }

    private String icaSitzOrt;
    private String icaEbene;
    private String icaGruppierung;

    private String ou;


    public String getIcaSitzOrt() {
        return icaSitzOrt;
    }

    public void setIcaSitzOrt(String icaSitzOrt) {
        this.icaSitzOrt = icaSitzOrt;
    }

    public String getIcaEbene() {
        return icaEbene;
    }

    public void setIcaEbene(String icaEbene) {
        this.icaEbene = icaEbene;
    }

    public String getIcaGruppierung() {
        return icaGruppierung;
    }

    public void setIcaGruppierung(String icaGruppierung) {
        this.icaGruppierung = icaGruppierung;
    }

    public String getOu() {
        return ou;
    }

    public void setOu(String ou) {
        this.ou = ou;
    }
}
