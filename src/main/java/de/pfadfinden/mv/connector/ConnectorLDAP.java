package de.pfadfinden.mv.connector;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

public final class ConnectorLDAP {

    public static LdapConnection getConnection() {
        LdapConnection connection = new LdapNetworkConnection("localhost",10389);
        try {
            connection.bind();
        } catch (LdapException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
