package de.pfadfinden.mv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LdapSyncApplication {


    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(LdapSyncApplication.class, args)));
    }

}
