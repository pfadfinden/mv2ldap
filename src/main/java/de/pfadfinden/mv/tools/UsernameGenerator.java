package de.pfadfinden.mv.tools;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.text.Normalizer;

public class UsernameGenerator {

    public static String getUsername(String nachname, String vorname){
        return String.format("%s.%s",prepareUsernameString(vorname),prepareUsernameString(nachname));
    }

    public static String prepareUsernameString(String inputString){
        Assert.hasText(inputString,"InputString must contain text.");

        // Kleinschrift und Leerzeichen entfernen
        String outputString = inputString.toLowerCase().trim();

        // Loesche Sonderzeichen
        outputString = StringUtils.deleteAny(outputString,"#*.!?%_");

        // Ersetze Umlaute
        outputString = outputString.replace("ü", "ue")
                .replace("ö", "oe")
                .replace("ä", "ae")
                .replace("ß", "ss");

        // Entferne Accents vgl. https://drillio.com/en/2011/java-remove-accent-diacritic/
        outputString = Normalizer.normalize(outputString, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Ersetze Leerzeichen
        outputString = outputString
                .replaceAll(" ", "-");

        return outputString;
    }
}
