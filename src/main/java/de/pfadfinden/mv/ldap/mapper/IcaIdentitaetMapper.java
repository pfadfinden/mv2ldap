package de.pfadfinden.mv.ldap.mapper;

import de.pfadfinden.mv.ldap.schema.IcaIdentitaet;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.ldap.client.template.EntryMapper;

import java.text.ParseException;

public class IcaIdentitaetMapper extends BaseMapper implements EntryMapper<IcaIdentitaet>{

    @Override
    public IcaIdentitaet map(Entry entry) throws LdapException {
        IcaIdentitaet identitaetEntry = new IcaIdentitaet();
        identitaetEntry.setDn(entry.getDn());
        identitaetEntry.setIcaId(Integer.parseInt(entry.get("icaId").getString()));
        identitaetEntry.setCn(getLdapString(entry,"cn"));
        identitaetEntry.setSn(getLdapString(entry,"sn"));
        identitaetEntry.setGivenName(getLdapString(entry,"givenName"));
        identitaetEntry.setFacsimileTelephoneNumber(getLdapString(entry,"facsimileTelephoneNumber"));

        if(entry.get("icaLastUpdated") != null) {
            try {
                identitaetEntry.setIcaLastUpdated(new GeneralizedTime(entry.get("icaLastUpdated").getString()).getDate());
            } catch (ParseException e) {}
        }
        return identitaetEntry;
    }

}
