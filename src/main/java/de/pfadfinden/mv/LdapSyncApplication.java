package de.pfadfinden.mv;

import de.pfadfinden.mv.command.CommandGruppen;
import de.pfadfinden.mv.command.CommandGruppierungen;
import de.pfadfinden.mv.command.CommandOrphanedPersons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LdapSyncApplication implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(LdapSyncApplication.class);

    private CommandGruppierungen commandGruppierungen;
    private CommandGruppen commandGruppen;
    private CommandOrphanedPersons commandOrphanedPersons;

    public LdapSyncApplication(CommandGruppierungen commandGruppierungen,
                               CommandGruppen commandGruppen,
                               CommandOrphanedPersons commandOrphanedPersons) {
        this.commandGruppierungen = commandGruppierungen;
        this.commandGruppen = commandGruppen;
        this.commandOrphanedPersons = commandOrphanedPersons;
    }

    public static void main(String[] args) {
        SpringApplication.run(LdapSyncApplication.class, args);
    }

    @Override
    public void run(String... args) {

        logger.info("Start MV LDAP Sync");

        this.commandGruppierungen.exec();
        this.commandGruppen.exec();
//        this.commandOrphanedPersons.exec();

        System.exit(0);
    }
}
