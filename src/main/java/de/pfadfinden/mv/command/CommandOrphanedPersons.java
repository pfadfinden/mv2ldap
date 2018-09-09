package de.pfadfinden.mv.command;

import de.pfadfinden.mv.ldap.schema.IcaIdentitaet;
import de.pfadfinden.mv.service.LdapEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandOrphanedPersons {
    private final Logger logger = LoggerFactory.getLogger(CommandOrphanedPersons.class);

    private final LdapEntryService ldapEntryService;

    public CommandOrphanedPersons(LdapEntryService ldapEntryService) {
        this.ldapEntryService = ldapEntryService;
    }

    public void exec() {
        List<IcaIdentitaet> identitaetList = ldapEntryService.findOrphanedPersons();
        identitaetList.forEach(icaIdentitaet -> {
            logger.info("Orphaned User: {} {}",icaIdentitaet.getIcaId(),icaIdentitaet.getCn());
        });
    }
}
