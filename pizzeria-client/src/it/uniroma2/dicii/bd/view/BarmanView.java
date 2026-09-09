package it.uniroma2.dicii.bd.view;

import it.uniroma2.dicii.bd.model.domain.ElementoDaPreparare;
import it.uniroma2.dicii.bd.model.dto.PreparazioneRequest;

import java.util.List;

public class BarmanView {
    public int showMenu() {
        System.out.println();
        System.out.println("*********************************");
        System.out.println("*        POSTAZIONE COCKTAIL      *");
        System.out.println("*********************************");
        System.out.println();
        System.out.println("1) Cocktail da preparare");
        System.out.println("2) Segna drink come pronto");
        System.out.println("3) Esci");
        return ConsoleInput.readInt("Scelta: ", 1, 3);
    }
    public void showCoda(List<ElementoDaPreparare> coda) {
        if (coda.isEmpty()) {
            System.out.println("\nNessuna bevanda in attesa di preparazione.");
            return;
        }

        System.out.printf("%n%-9s %-4s %-22s %-10s %s%n",
                "COMANDA", "N.", "PRODOTTO", "MODALITA", "TAVOLO");
        for (ElementoDaPreparare e : coda) {
            System.out.printf("%-9d %-4d %-22s %-10s %s%n",
                    e.getIdComanda(),
                    e.getNumeroProgressivo(),
                    e.getProdotto(),
                    e.getModalita(),
                    e.getNumeroTavolo() == null ? "-" : e.getNumeroTavolo());
        }
    }
    public PreparazioneRequest askPreparazione(String username) {
        Integer idComanda = ConsoleInput.readIntOrNull("Numero comanda: ");
        if (idComanda == null) {
            return null;
        }
        Integer numero = ConsoleInput.readIntOrNull("Numero progressivo: ");
        if (numero == null) {
            return null;
        }
        return new PreparazioneRequest(idComanda, numero, username);
    }

    public void showEsito(String messaggio) {
        System.out.println(messaggio);
    }

    public void showError(String messaggio) {
        System.out.println("Errore: " + messaggio);
    }

}
