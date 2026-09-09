package it.uniroma2.dicii.bd.view;

import it.uniroma2.dicii.bd.model.domain.ElementoPronto;
import it.uniroma2.dicii.bd.model.domain.TavoloLibero;
import it.uniroma2.dicii.bd.model.domain.VoceMenu;
import it.uniroma2.dicii.bd.model.dto.AggiuntaElementoRequest;
import it.uniroma2.dicii.bd.model.dto.ConsegnaRequest;

import java.util.List;

/**
 * Interfaccia testuale del cameriere. Si occupa esclusivamente di input e
 * output: non effettua controlli sui dati, che spettano alle procedure.
 */
public class CameriereView {

    public int showMenu() {
        System.out.println();
        System.out.println("*********************************");
        System.out.println("*        POSTAZIONE SALA        *");
        System.out.println("*********************************");
        System.out.println();
        System.out.println("1) Assegna tavolo e apri comanda");
        System.out.println("2) Aggiungi prodotto a una comanda");
        System.out.println("3) Prodotti pronti da servire");
        System.out.println("4) Segna prodotto come servito");
        System.out.println("5) Esci");
        return ConsoleInput.readInt("Scelta: ", 1, 5);
    }

    public void showTavoliLiberi(List<TavoloLibero> tavoli) {
        if (tavoli.isEmpty()) {
            System.out.println("\nNessun tavolo libero al momento.");
            return;
        }
        System.out.printf("%n%-9s %s%n", "TAVOLO", "POSTI");
        for (TavoloLibero t : tavoli) {
            System.out.printf("%-9d %d%n", t.getNumero(), t.getPosti());
        }
    }

    public void showMenuProdotti(List<VoceMenu> menu) {
        if (menu.isEmpty()) {
            System.out.println("\nNessun prodotto disponibile.");
            return;
        }
        System.out.printf("%n%-8s %-24s %-10s %s%n", "CODICE", "PRODOTTO", "PREZZO", "TIPO");
        for (VoceMenu v : menu) {
            System.out.printf("%-8d %-24s %-10s %s%n",
                    v.getCodice(), v.getNome(), v.getPrezzo() + " EUR", v.getTipo());
        }
    }

    public void showElementiPronti(List<ElementoPronto> pronti) {
        if (pronti.isEmpty()) {
            System.out.println("\nNessun prodotto pronto da servire.");
            return;
        }
        System.out.printf("%n%-9s %-4s %-24s %s%n", "COMANDA", "N.", "PRODOTTO", "TAVOLO");
        for (ElementoPronto e : pronti) {
            System.out.printf("%-9d %-4d %-24s %d%n",
                    e.getIdComanda(), e.getNumeroProgressivo(),
                    e.getProdotto(), e.getNumeroTavolo());
        }
    }

    /** Restituisce null se il valore inserito non e numerico. */
    public Integer askNumeroTavolo() {
        return ConsoleInput.readIntOrNull("Numero del tavolo da assegnare: ");
    }

    public AggiuntaElementoRequest askAggiuntaElemento() {
        Integer idComanda = ConsoleInput.readIntOrNull("Numero comanda: ");
        if (idComanda == null) {
            return null;
        }
        Integer codice = ConsoleInput.readIntOrNull("Codice prodotto: ");
        if (codice == null) {
            return null;
        }
        return new AggiuntaElementoRequest(idComanda, codice);
    }

    public ConsegnaRequest askConsegna() {
        Integer idComanda = ConsoleInput.readIntOrNull("Numero comanda: ");
        if (idComanda == null) {
            return null;
        }
        Integer numero = ConsoleInput.readIntOrNull("Numero progressivo: ");
        if (numero == null) {
            return null;
        }
        return new ConsegnaRequest(idComanda, numero);
    }

    public void showEsito(String messaggio) {
        System.out.println(messaggio);
    }

    public void showError(String messaggio) {
        System.out.println("Errore: " + messaggio);
    }
}
