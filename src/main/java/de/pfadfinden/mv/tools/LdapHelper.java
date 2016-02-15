package de.pfadfinden.mv.tools;

import de.pfadfinden.mv.model.IcaGruppierung;
import de.pfadfinden.mv.model.IcaRecord;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.util.GeneralizedTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Philipp on 19.01.2016.
 */
public class LdapHelper {
    final static Logger logger = LoggerFactory.getLogger(LdapHelper.class);

    /**
     * Pruefe ob Modifikation eines LDAP Attributes erforderlich ist und erstelle DefaultModification
     *
     * @return DefaultModification
     * @author Philipp Steinmetzger
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