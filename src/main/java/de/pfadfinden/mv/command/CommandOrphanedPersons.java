package de.pfadfinden.mv.command;

import de.pfadfinden.mv.database.LdapDatabase;
import de.pfadfinden.mv.ldap.schema.IcaIdentitaet;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CommandOrphanedPersons {
    private final Logger logger = LoggerFactory.getLogger(CommandOrphanedPersons.class);

    public CommandOrphanedPersons() {

        Dn baseDn = LdapDatabase.getBaseDn();
        String searchString = "(&(objectClass=person)(!(memberOf=*)))";
        List<IcaIdentitaet> identitaetList = LdapDatabase.getLdapConnectionTemplate().search(
                baseDn,searchString, SearchScope.SUBTREE, IcaIdentitaet.getEntryMapper()
        );

        identitaetList.forEach(icaIdentitaet -> {
            logger.info("Orphaned User: {} {}",icaIdentitaet.getIcaId(),icaIdentitaet.getCn());
        });
    }
}
