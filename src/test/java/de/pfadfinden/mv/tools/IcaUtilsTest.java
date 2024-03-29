package de.pfadfinden.mv.tools;

import de.pfadfinden.mv.model.IcaIdentitaet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IcaUtilsTest {

    @Test
    public void isValidIdentitaet() {
        IcaIdentitaet icaIdentitaetValid = new IcaIdentitaet();
        icaIdentitaetValid.setVorname("Max");
        icaIdentitaetValid.setNachname("Muster");
        assertTrue(IcaUtils.isValidIdentitaet(icaIdentitaetValid));

        IcaIdentitaet icaIdentitaetInvalid1 = new IcaIdentitaet();
        icaIdentitaetInvalid1.setVorname("");
        icaIdentitaetInvalid1.setNachname("Muster");
        assertFalse(IcaUtils.isValidIdentitaet(icaIdentitaetInvalid1));

        IcaIdentitaet icaIdentitaetInvalid2 = new IcaIdentitaet();
        icaIdentitaetInvalid2.setVorname("A");
        icaIdentitaetInvalid2.setNachname("");
        assertFalse(IcaUtils.isValidIdentitaet(icaIdentitaetInvalid2));
    }

    @Test
    public void isValidName() {

        assertTrue(IcaUtils.isValidName("Max"));
        assertTrue(IcaUtils.isValidName("Hans "));

        assertFalse(IcaUtils.isValidName(" "));
        assertFalse(IcaUtils.isValidName("#"));
        assertFalse(IcaUtils.isValidName("!"));
        assertFalse(IcaUtils.isValidName(""));
        assertFalse(IcaUtils.isValidName(null));
    }

    @Test
    public void isValidEmail() {
        assertTrue(IcaUtils.isValidEmail("test@test.de"));
        assertTrue(IcaUtils.isValidEmail("test@pfadfinden.de"));
        assertFalse(IcaUtils.isValidEmail(" "));
        assertFalse(IcaUtils.isValidEmail("m"));
        assertFalse(IcaUtils.isValidEmail(""));
        assertFalse(IcaUtils.isValidEmail("@"));
    }
}
