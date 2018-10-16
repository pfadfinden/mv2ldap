package de.pfadfinden.mv.service;

import de.pfadfinden.mv.database.LdapDatabase;
import de.pfadfinden.mv.ldap.schema.Gruppe;
import de.pfadfinden.mv.ldap.schema.IcaGruppierung;
import de.pfadfinden.mv.ldap.schema.IcaIdentitaet;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.template.EntryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LdapEntryService {
    private Logger logger = LoggerFactory.getLogger(LdapEntryService.class);

    public Optional<IcaIdentitaet> findIcaIdentitaetById(int identitaetId){
        return findIcaById(IcaIdentitaet.getEntryMapper(),"icaIdentitaet",identitaetId);
    }

    public Optional<IcaGruppierung> findParentGruppierung(de.pfadfinden.mv.model.IcaGruppierung gruppierung){
        logger.debug("Suche Parent Gruppierung zu {} (ParentID: {})",gruppierung.getName(),gruppierung.getParentGruppierungId());
        return findIcaGruppierungById(gruppierung.getParentGruppierungId());
    }

    public Optional<IcaIdentitaet> findIcaIdentitaetByUid(String uid){
        Dn baseDn = LdapDatabase.getBaseDn();
        String searchString = String.format("(&(objectClass=%s)(uid=%s))","icaIdentitaet",uid);
        return Optional.ofNullable(LdapDatabase.getLdapConnectionTemplate().searchFirst(baseDn,searchString, SearchScope.SUBTREE, IcaIdentitaet.getEntryMapper()));
    }

    public Optional<IcaGruppierung> findIcaGruppierungById(int gruppierungId){
        return findIcaById(IcaGruppierung.getEntryMapper(),"icaGruppierung",gruppierungId);
    }

    public Optional<Gruppe> findGruppeById(int gruppeId){
        return findIcaById(Gruppe.getEntryMapper(),"groupOfNames",gruppeId);
    }

    public <T> Optional<T> findIcaById(EntryMapper<T> mapper, String record, int identitaetId){
        Dn baseDn = LdapDatabase.getBaseDn();
        String searchString = String.format("(&(objectClass=%s)(icaId=%d))",record,identitaetId);
        return Optional.ofNullable(LdapDatabase.getLdapConnectionTemplate().searchFirst(baseDn,searchString, SearchScope.SUBTREE, mapper));
    }

    public List<IcaIdentitaet> findOrphanedPersons(){
        Dn baseDn = LdapDatabase.getBaseDn();
        String searchString = "(&(objectClass=person)(!(memberOf=*)))";
        return LdapDatabase.getLdapConnectionTemplate().search(baseDn,searchString, SearchScope.SUBTREE, IcaIdentitaet.getEntryMapper());
    }

}
