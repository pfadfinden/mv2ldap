package de.pfadfinden.mv.ldap.mapper;

import de.pfadfinden.mv.ldap.schema.IcaGruppierung;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.util.GeneralizedTime;
import org.apache.directory.ldap.client.template.EntryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

public class IcaGruppierungMapper extends BaseMapper implements EntryMapper<IcaGruppierung>{

    private final Logger logger = LoggerFactory.getLogger(IcaGruppierungMapper.class);

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
            try {
                gruppierung.setIcaLastUpdated(new GeneralizedTime(entry.get("icaLastUpdated").getString()).getDate());
            } catch (ParseException e) {
                logger.error("Conversion of lastUpdated timestamp of #{} failed.",gruppierung.getIcaId(),e);
            }
        }
        return gruppierung;
    }
}
