package de.pfadfinden.mv.tools;

import java.text.Normalizer;

public class UsernameGenerator {

    public static String getUsername(String nachname, String vorname){

        if(nachname == null || nachname.trim().isEmpty()){
            throw new IllegalArgumentException("Nachname nicht vorhanden oder leer.");
        }

        if(vorname == null || vorname.trim().isEmpty()){
            throw new IllegalArgumentException("Vorname nicht vorhanden oder leer.");
        }

        return String.format("%s.%s",prepareUsernameString(vorname),prepareUsernameString(nachname));
    }

    public static String prepareUsernameString(String inputString){
        String outputString = inputString;

        // 1. Kleinschrift
        outputString = outputString.toLowerCase();

        // 2. Leerzeichen entfernen
        outputString = outputString.trim();

        // 3. Ersetze alle Umlaute
        outputString = outputString
                .replaceAll("ü", "ue")
                .replaceAll("ö", "oe")
                .replaceAll("ä", "ae")
                .replaceAll("ß", "ss");

        // 4. Entferne Accents vgl. https://drillio.com/en/2011/java-remove-accent-diacritic/
        outputString = Normalizer.normalize(outputString, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 5. Ersetze Leerzeichen
        outputString = outputString
                .replaceAll(" ", "-");

        return outputString;
    }
}
