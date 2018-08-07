package de.pfadfinden.mv;

import com.google.common.base.Stopwatch;
import de.pfadfinden.mv.command.CommandGruppen;
import de.pfadfinden.mv.command.CommandGruppierungen;
import de.pfadfinden.mv.command.CommandOrphanedPersons;
import de.pfadfinden.mv.database.IcaDatabase;
import de.pfadfinden.mv.database.SyncDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception{
        Stopwatch stopwatch = Stopwatch.createStarted();
        logger.info("Start MV LDAP Sync");

        new CommandGruppierungen();
        new CommandGruppen();
        new CommandOrphanedPersons();

        logger.info("Ende");

        IcaDatabase.close();
        SyncDatabase.close();

        stopwatch.stop();
        logger.info("Runtime: {}",stopwatch.elapsed());
        System.exit(0);
    }
}