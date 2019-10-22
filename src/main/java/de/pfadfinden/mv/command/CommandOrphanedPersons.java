package de.pfadfinden.mv.command;

import de.pfadfinden.mv.ldap.schema.IcaIdentitaet;
import de.pfadfinden.mv.service.LdapEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(50)
public class CommandOrphanedPersons implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(CommandOrphanedPersons.class);

    private final LdapEntryService ldapEntryService;

    public CommandOrphanedPersons(LdapEntryService ldapEntryService) {
        this.ldapEntryService = ldapEntryService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if(args.containsOption("orphaned")) this.logOrphanedPersons();
    }

    private void logOrphanedPersons() {
        List<IcaIdentitaet> identitaetList = ldapEntryService.findOrphanedPersons();
        identitaetList.forEach(icaIdentitaet -> logger.info("Orphaned User: {} {}",icaIdentitaet.getIcaId(),icaIdentitaet.getCn()));
    }

}
