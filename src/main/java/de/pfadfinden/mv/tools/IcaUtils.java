package de.pfadfinden.mv.tools;

import de.pfadfinden.mv.model.IcaIdentitaet;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class IcaUtils {

    public static boolean isValidIdentitaet(IcaIdentitaet icaIdentitaet) {
        Assert.notNull(icaIdentitaet, "IcaIdentitaet must not be null.");
        return IcaUtils.isValidName(icaIdentitaet.getNachname()) && IcaUtils.isValidName(icaIdentitaet.getVorname());
    }

    public static boolean isValidName(String name) {
        return StringUtils.hasText(StringUtils.deleteAny(name,"#*.!?%_"));
    }

    public static boolean isValidEmail(String email) {
        if(email.equals("")) return false;
        return EmailValidator.getInstance().isValid(email);
    }
}

