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
    public void prepareUsernameString() {
        assertEquals("aeoeuess",UsernameGenerator.prepareUsernameString("äöüß"));
        assertEquals("uo",UsernameGenerator.prepareUsernameString("u!o#"));
        assertEquals("aaa",UsernameGenerator.prepareUsernameString("áàâ"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void prepareUsernameString_throwException() {
        UsernameGenerator.prepareUsernameString("");
    }

}
