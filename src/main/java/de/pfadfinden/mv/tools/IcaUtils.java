package de.pfadfinden.mv.tools;

import de.pfadfinden.mv.model.IcaIdentitaet;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class IcaUtils {

    public static boolean isValidIdentitaet(IcaIdentitaet icaIdentitaet) {
        Assert.notNull(icaIdentitaet, "IcaIdentitaet must not be null.");

        if (!IcaUtils.isValidName(icaIdentitaet.getNachname()) || !IcaUtils.isValidName(icaIdentitaet.getVorname())) {
            return false;
        }

        return true;
    }

    public static boolean isValidName(String name) {
        name = StringUtils.deleteAny(name,"#*.!?%_");
        if(!StringUtils.hasText(name)) return false;
        return true;
    }
}

