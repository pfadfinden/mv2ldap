package de.pfadfinden.mv.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void prepareUsernameString_throwException() {
        assertThrows(IllegalArgumentException.class,() -> UsernameGenerator.prepareUsernameString(""));
    }

}
