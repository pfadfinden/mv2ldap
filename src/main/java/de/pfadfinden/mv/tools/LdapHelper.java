package de.pfadfinden.mv.tools;

import de.pfadfinden.mv.model.IcaGruppierung;
import de.pfadfinden.mv.model.IcaRecord;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
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
    public static DefaultModification checkAttributeValue(Entry gruppierungLdap, IcaRecord gruppierungIca, Object icaField, String ldapAttributeName){
        // Wenn Feld in ICA leer, aber in LDAP Attribut vorhanden, dann entferne Attribut in LDAP
        if((icaField == null || icaField.toString().isEmpty()) && gruppierungLdap.containsAttribute(ldapAttributeName)) {
            logger.info("Gruppierung #{} Attribut Modifitcation: REMOVE_ATTRIBUTE {}",gruppierungIca.getId(),ldapAttributeName);
            return new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, ldapAttributeName);
        }
        // Wenn Feld in ICA gefuellt, aber in LDAP Attribut nicht vorhanden, dann lege Attribut in LDAP an
        if((icaField != null && !icaField.toString().isEmpty()) && !gruppierungLdap.containsAttribute(ldapAttributeName)){
            logger.info("Gruppierung #{} Attribut Modifitcation: ADD_ATTRIBUTE {}",gruppierungIca.getId(),ldapAttributeName);
            return new DefaultModification(ModificationOperation.ADD_ATTRIBUTE,ldapAttributeName,icaField.toString());
        }

        // Wenn Feld in ICA gefuellt, und in LDAP vorhanden, dann pruefe ob Aktualisierung erforderlich
        if((icaField != null && !icaField.toString().isEmpty()) && gruppierungLdap.containsAttribute(ldapAttributeName)){
            if(icaField instanceof String) {
                if (icaField.toString() != gruppierungLdap.get(ldapAttributeName).toString()){
                    logger.info("Gruppierung #{} Attribut Modifitcation: REPLACE_ATTRIBUTE {}",gruppierungIca.getId(),ldapAttributeName);
                    return new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE,ldapAttributeName,icaField.toString());
                }
            }
            if(icaField instanceof Date) {
                try {
                    GeneralizedTime icaGeneralizedTime = new GeneralizedTime((Date)icaField);
                    GeneralizedTime ldapGeneralizedTime = new GeneralizedTime(gruppierungLdap.get(ldapAttributeName).getString());
                    if(!icaGeneralizedTime.equals(ldapGeneralizedTime))
                        logger.info("Gruppierung #{} Attribut Modifitcation: REPLACE_ATTRIBUTE {}",gruppierungIca.getId(),ldapAttributeName);
                    return new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE,ldapAttributeName,icaGeneralizedTime.toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (LdapInvalidAttributeValueException e) {
                    e.printStackTrace();
                }
            }
        }

        // Wenn keine Aktualisierung in LDAP notwendig ist, gebe null zurueck.
        return null;
    }

}