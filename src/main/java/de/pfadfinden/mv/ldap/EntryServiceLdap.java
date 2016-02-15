package de.pfadfinden.mv.ldap;

import de.pfadfinden.mv.database.LdapTemplateDatabase;
import de.pfadfinden.mv.ldap.schema.Gruppe;
import de.pfadfinden.mv.ldap.schema.IcaGruppierung;
import de.pfadfinden.mv.ldap.schema.IcaIdentitaet;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.template.EntryMapper;

public class EntryServiceLdap {

    public static IcaIdentitaet findIcaIdentitaetById(int identitaetId){
        return (IcaIdentitaet) findIcaById(IcaIdentitaet.getEntryMapper(),"icaIdentitaet",identitaetId);
    }

    public static IcaGruppierung findIcaGruppierungById(int gruppierungId){
        return (IcaGruppierung) findIcaById(IcaGruppierung.getEntryMapper(),"icaGruppierung",gruppierungId);
    }

    public static Gruppe findGruppeById(int gruppeId){
        return (Gruppe) findIcaById(Gruppe.getEntryMapper(),"groupOfNames",gruppeId);
    }

    public static Object findIcaById(EntryMapper mapper, String record, int identitaetId){
        String baseDn = "dc=example,dc=com";
        String searchString = String.format("(&(objectClass=%s)(icaId=%d))",record,identitaetId);
        return LdapTemplateDatabase.getLdapConnectionTemplate().searchFirst(baseDn,searchString, SearchScope.SUBTREE, mapper);
    }

}
