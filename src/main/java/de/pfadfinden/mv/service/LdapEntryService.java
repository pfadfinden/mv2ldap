package de.pfadfinden.mv.service;

import de.pfadfinden.mv.ldap.schema.Gruppe;
import de.pfadfinden.mv.ldap.schema.IcaGruppierung;
import de.pfadfinden.mv.ldap.schema.IcaIdentitaet;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.template.EntryMapper;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LdapEntryService {

    private final LdapConnectionTemplate ldapConnectionTemplate;
    private final Logger logger = LoggerFactory.getLogger(LdapEntryService.class);

    @Value("${app.ldap.base-dn}")
    private String baseDn;

    public LdapEntryService(LdapConnectionTemplate ldapConnectionTemplate) {
        this.ldapConnectionTemplate = ldapConnectionTemplate;
    }

    public Optional<IcaIdentitaet> findIcaIdentitaetById(int identitaetId){
        return findIcaById(IcaIdentitaet.getEntryMapper(),"icaIdentitaet",identitaetId);
    }

    public Optional<IcaGruppierung> findParentGruppierung(de.pfadfinden.mv.model.IcaGruppierung gruppierung){
        logger.debug("Suche Parent Gruppierung zu {} (ParentID: {})",gruppierung.getName(),gruppierung.getParentGruppierungId());
        return findIcaGruppierungById(gruppierung.getParentGruppierungId());
    }

    public Optional<IcaIdentitaet> findIcaIdentitaetByUid(String uid){
        String searchString = String.format("(&(objectClass=%s)(uid=%s))","icaIdentitaet",uid);
        return Optional.ofNullable(this.ldapConnectionTemplate.searchFirst(this.getBaseDn(),searchString, SearchScope.SUBTREE,
                IcaIdentitaet.getEntryMapper()));
    }

    public Optional<IcaGruppierung> findIcaGruppierungById(int gruppierungId){
        return findIcaById(IcaGruppierung.getEntryMapper(),"icaGruppierung",gruppierungId);
    }

    public Optional<Gruppe> findGruppeById(int gruppeId){
        return findIcaById(Gruppe.getEntryMapper(),"groupOfNames",gruppeId);
    }

    public <T> Optional<T> findIcaById(EntryMapper<T> mapper, String record, int identitaetId){
        String searchString = String.format("(&(objectClass=%s)(icaId=%d))",record,identitaetId);
        return Optional.ofNullable(this.ldapConnectionTemplate.searchFirst(this.getBaseDn(),searchString, SearchScope.SUBTREE, mapper));
    }

    public List<IcaIdentitaet> findOrphanedPersons(){
        String searchString = "(&(objectClass=person)(!(memberOf=*)))";
        return this.ldapConnectionTemplate.search(this.getBaseDn(),searchString, SearchScope.SUBTREE, IcaIdentitaet.getEntryMapper());
    }

    public Dn getBaseDn(){
        return this.ldapConnectionTemplate.newDn(this.baseDn);
    }

}
