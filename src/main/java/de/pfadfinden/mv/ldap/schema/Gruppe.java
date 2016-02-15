package de.pfadfinden.mv.ldap.schema;

import de.pfadfinden.mv.ldap.mapper.GruppeMapper;
import org.apache.directory.ldap.client.template.EntryMapper;

import java.util.List;

public class Gruppe extends IcaRecord{

    public static EntryMapper<Gruppe> getEntryMapper() {
        return new GruppeMapper();
    }

    public Gruppe(){}

    private String description;
    private String cn;
    private List<String> member;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public List<String> getMember() {
        return member;
    }

    public void setMember(List<String> member) {
        this.member = member;
    }
}
