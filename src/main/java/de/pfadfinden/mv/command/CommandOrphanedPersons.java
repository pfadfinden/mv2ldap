package de.pfadfinden.mv.command;

import de.pfadfinden.mv.ldap.schema.IcaIdentitaet;
import de.pfadfinden.mv.service.LdapEntryService;
import org.apache.directory.api.ldap.model.message.DeleteResponse;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;
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
    private final LdapConnectionTemplate ldapConnectionTemplate;

    public CommandOrphanedPersons(LdapEntryService ldapEntryService, LdapConnectionTemplate ldapConnectionTemplate) {
        this.ldapEntryService = ldapEntryService;
        this.ldapConnectionTemplate = ldapConnectionTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if(args.containsOption("orphaned")) this.logOrphanedPersons();
    }

    private void logOrphanedPersons() {
        List<IcaIdentitaet> identitaetList = ldapEntryService.findOrphanedPersons();
        identitaetList.forEach(icaIdentitaet -> logger.info("Orphaned User: {} {}",icaIdentitaet.getIcaId(),icaIdentitaet.getCn()));
    }

    private void deletePerson(IcaIdentitaet icaIdentitaet) {
        DeleteResponse deleteResponse =
                ldapConnectionTemplate.delete(ldapConnectionTemplate.newDn(icaIdentitaet.getDn().toString()));
        if (deleteResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
            logger.error(deleteResponse.getLdapResult().getDiagnosticMessage());
        } else {
            logger.info("User has been deleted: {} {}",icaIdentitaet.getIcaId(),icaIdentitaet.getCn());
        }

    }

}
