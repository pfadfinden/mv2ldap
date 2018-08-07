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

        // Kleinschrift und Leerzeichen entfernen
        String outputString = inputString.toLowerCase().trim();

        // Ersetze alle Umlaute
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
