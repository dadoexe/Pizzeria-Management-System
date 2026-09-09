package it.uniroma2.dicii.bd.view;

import java.util.Scanner;

/**
 * Sorgente di input condivisa da tutte le view.
 *
 * Istanziare piu lettori su System.in non e sicuro: ciascuno bufferizza per
 * conto proprio e puo consumare righe destinate agli altri. Tutte le view
 * leggono quindi da questa unica istanza.
 */
public final class ConsoleInput {

    private static final Scanner SCANNER = new Scanner(System.in);

    private ConsoleInput() {}

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine();
    }

    /**
     * Legge un intero compreso nell'intervallo indicato, richiedendolo
     * finche non e valido.
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(readLine(prompt).trim());
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // ricade nel messaggio di errore
            }
            System.out.println("Valore non valido");
        }
    }

    /**
     * Legge un intero senza vincoli di intervallo. Restituisce null se il
     * valore inserito non e numerico, cosi che il chiamante possa annullare
     * l'operazione.
     */
    public static Integer readIntOrNull(String prompt) {
        try {
            return Integer.parseInt(readLine(prompt).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
