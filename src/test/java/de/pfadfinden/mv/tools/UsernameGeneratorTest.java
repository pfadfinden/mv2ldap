package de.pfadfinden.mv.tools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UsernameGeneratorTest {

    @Test
    public void combineVornameNachname() {
        String vorname = "max";
        String nachname = "muster";
        String username = UsernameGenerator.getUsername(nachname,vorname);
        assertEquals("max.muster",username);
    }

    @Test
    public void ersetzeUmlaute() {
        assertEquals("aeoeuess",UsernameGenerator.prepareUsernameString("äöüß"));
    }

    @Test
    public void ersetzeAccents() {
        assertEquals("aaa",UsernameGenerator.prepareUsernameString("áàâ"));
    }

}
