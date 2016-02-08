import de.pfadfinden.mv.command.CommandGruppen;
import de.pfadfinden.mv.command.CommandGruppierungen;
import de.pfadfinden.mv.command.CommandIdentitaet;
import de.pfadfinden.mv.database.IcaDatabase;
import de.pfadfinden.mv.database.SyncDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception{
        long startTime = System.nanoTime();
        logger.info("Start MV LDAP Sync");

        // new CommandGruppierungen();
        new CommandGruppen();

        logger.info("Ende");

        IcaDatabase.close();
        SyncDatabase.close();

        long stopTime = System.nanoTime();
        long elapsedTime = stopTime-startTime;
        double seconds = (double)elapsedTime / 1000000000.0;
        logger.info("Runtime: {}",seconds);
        System.exit(0);
    }
}
