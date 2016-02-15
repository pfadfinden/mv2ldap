package de.pfadfinden.mv.ldap.mapper;

import de.pfadfinden.mv.ldap.schema.Gruppe;
import de.pfadfinden.mv.ldap.schema.IcaGruppierung;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.template.EntryMapper;

public class GruppeMapper extends BaseMapper implements EntryMapper<Gruppe>{

    @Override
    public Gruppe map(Entry entry) throws LdapException {
        Gruppe gruppe = new Gruppe();
        gruppe.setDn(entry.getDn());
        gruppe.setIcaId(Integer.parseInt(entry.get("icaId").getString()));
        gruppe.setDescription(getLdapString(entry,"description"));
        gruppe.setCn(getLdapString(entry,"cn"));
        gruppe.setMember(getLdapList(entry,"member"));
        return gruppe;
    }
}
