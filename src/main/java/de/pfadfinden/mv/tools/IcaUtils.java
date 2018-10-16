package de.pfadfinden.mv.tools;

import de.pfadfinden.mv.model.IcaIdentitaet;
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
}

