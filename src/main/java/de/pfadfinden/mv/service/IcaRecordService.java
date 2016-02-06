package de.pfadfinden.mv.service;

import de.pfadfinden.mv.connector.ConnectorLDAP;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class IcaRecordService {

    final Logger logger = LoggerFactory.getLogger(IcaRecordService.class);

    public Entry findGruppierungById(int gruppierungId){
        return findRecordById(gruppierungId,"icaGruppierung");
    }

    public Entry findBerechtigungsgruppe(int berechtigungsgruppeId){
        return findRecordById(berechtigungsgruppeId,"groupOfNames");
    }

    public Entry findIdentitaetById(int identitaetId){
        return findRecordById(identitaetId,"icaIdentitaet");
    }

    private Entry findRecordById(int recordId, String objectClass){

        LdapConnection connectionLDAP = ConnectorLDAP.getConnection();
        String searchString = String.format("(&(objectClass=%s)(icaId=%d))",objectClass,recordId);
        Dn dn = null;

        EntryCursor cursor = null;

        try {
            dn = new Dn("dc=example,dc=com");

            cursor = connectionLDAP.search(dn,searchString, SearchScope.SUBTREE);
            while ( cursor.next() )
            {
                Entry entry = cursor.get();
                logger.debug("Datensatz {} #{} gefunden: {}",objectClass,recordId,entry.getDn());
                connectionLDAP.unBind();
                connectionLDAP.close();
                return entry;
            }
        } catch (LdapException e) {
            logger.error(e.getStackTrace().toString());
        } catch (CursorException e) {
            logger.error(e.getStackTrace().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        cursor.close();

        try {
            connectionLDAP.unBind();
            connectionLDAP.close();
        } catch (LdapException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
