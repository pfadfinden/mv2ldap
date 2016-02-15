package de.pfadfinden.mv.ldap.mapper;

import de.pfadfinden.mv.ldap.schema.IcaGruppierung;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.ldap.client.template.EntryMapper;

import java.text.ParseException;
import java.util.Date;

public class IcaGruppierungMapper extends BaseMapper implements EntryMapper<IcaGruppierung>{

    @Override
    public IcaGruppierung map(Entry entry) throws LdapException {
        IcaGruppierung gruppierung  = new IcaGruppierung();
        gruppierung.setDn(entry.getDn());
        gruppierung.setIcaId(Integer.parseInt(entry.get("icaId").getString()));
        gruppierung.setOu(getLdapString(entry,"ou"));
        gruppierung.setIcaEbene(getLdapString(entry,"icaEbene"));
        gruppierung.setIcaStatus(getLdapString(entry,"icaStatus"));
        gruppierung.setIcaSitzOrt(getLdapString(entry,"icaSitzOrt"));

        if(entry.get("icaLastUpdated") != null) {
            Date date = new Date();
            try {
                date = new GeneralizedTime(entry.get("icaLastUpdated").getString()).getDate();
            } catch (ParseException e) {
                System.out.println(e);
            }
            gruppierung.setIcaLastUpdated(date);
        }
        return gruppierung;
    }
}
