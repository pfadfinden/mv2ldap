package de.pfadfinden.mv.tools;

import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class LdapHelper {
    final static Logger logger = LoggerFactory.getLogger(LdapHelper.class);

    /**
     * Pruefe ob Modifikation eines LDAP Attributes erforderlich ist und ergaenze ModificationRequest
     */
    public static void modifyRequest(ModifyRequest request, Object fieldIca, Object fieldLdap, String field){

        // Wenn Feld in ICA leer, aber in LDAP Attribut vorhanden, dann entferne Attribut in LDAP
        if((fieldIca == null || fieldIca.toString().isEmpty()) && fieldLdap != null){
            request.remove(field);
        }

        // Wenn Feld in ICA gefuellt, aber in LDAP Attribut nicht vorhanden, dann lege Attribut in LDAP an
        if((fieldIca != null && !fieldIca.toString().isEmpty()) && fieldLdap == null){
            request.add(field,fieldIca.toString());
        }

        // Wenn Feld in ICA gefuellt, und in LDAP vorhanden, dann pruefe ob Aktualisierung erforderlich
        if((fieldIca != null && !fieldIca.toString().isEmpty()) && fieldLdap != null){
            logger.debug("Update Feld #{}",field);
            request.replace(field,fieldIca.toString());
        }

    }

    public static Dn getBaseDn(LdapConnectionTemplate ldapConnectionTemplate){
        Properties prop = new Properties();

        try {
            FileReader fr = new FileReader("./config/databaseLdap.properties");
            prop.load(fr);
            return ldapConnectionTemplate.newDn(prop.getProperty("ldapConnection.baseDn"));
        } catch (IOException e) {
            logger.error("Failed creating BaseDN because reading dabaseLdap properties file failed.",e);
        }

        return ldapConnectionTemplate.newDn(prop.getProperty("ldapConnection.baseDn"));
    }

}
