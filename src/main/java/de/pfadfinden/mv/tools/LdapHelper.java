package de.pfadfinden.mv.tools;

import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapHelper {
    final static Logger logger = LoggerFactory.getLogger(LdapHelper.class);

    /**
     * Pruefe ob Modifikation eines LDAP Attributes erforderlich ist und erstelle DefaultModification
     */
    public static void stringUpdateHelper(ModifyRequest request, Object fieldIca, Object fieldLdap, String field){

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

}