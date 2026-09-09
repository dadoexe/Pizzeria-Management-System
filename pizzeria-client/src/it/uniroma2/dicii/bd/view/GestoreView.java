package it.uniroma2.dicii.bd.view;

import it.uniroma2.dicii.bd.model.domain.ComandaAperta;
import it.uniroma2.dicii.bd.model.domain.ElementoDaRitirare;
import it.uniroma2.dicii.bd.model.domain.Entrate;
import it.uniroma2.dicii.bd.model.domain.RigaScontrino;
import it.uniroma2.dicii.bd.model.domain.Scontrino;
import it.uniroma2.dicii.bd.model.domain.VoceMenu;
import it.uniroma2.dicii.bd.model.dto.AperturaComandaConsegnaRequest;
import it.uniroma2.dicii.bd.model.dto.ConsegnaRequest;
import it.uniroma2.dicii.bd.model.dto.DipendenteRequest;
import it.uniroma2.dicii.bd.model.dto.EntrateMensiliRequest;
import it.uniroma2.dicii.bd.model.dto.ProdottoRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Interfaccia testuale del gestore. Si occupa esclusivamente di input e
 * output: non effettua controlli sui dati, che spettano alle procedure.
 */
public class GestoreView {

    public int showMenu() {
        System.out.println();
        System.out.println("*********************************");
        System.out.println("*        POSTAZIONE CASSA       *");
        System.out.println("*********************************");
        System.out.println();
        System.out.println(" 1) Apri comanda da asporto");
        System.out.println(" 2) Registra comanda telefonica");
        System.out.println(" 3) Aggiungi prodotto a una comanda");
        System.out.println(" 4) Prodotti pronti da ritirare");
        System.out.println(" 5) Segna prodotto come ritirato");
        System.out.println(" 6) Comande aperte");
        System.out.println(" 7) Completa comanda");
        System.out.println(" 8) Stampa scontrino");
        System.out.println(" 9) Entrate giornaliere");
        System.out.println("10) Entrate mensili");
        System.out.println("11) Gestisci menu");
        System.out.println("12) Registra dipendente");
        System.out.println("13) Rimuovi dipendente");
        System.out.println("14) Esci");
        return ConsoleInput.readInt("Scelta: ", 1, 14);
    }

    public void showElementiDaRitirare(List<ElementoDaRitirare> pronti) {
        if (pronti.isEmpty()) {
            System.out.println("\nNessun prodotto pronto da ritirare.");
            return;
        }
        System.out.printf("%n%-9s %-4s %-24s %s%n", "COMANDA", "N.", "PRODOTTO", "MODALITA");
        for (ElementoDaRitirare e : pronti) {
            System.out.printf("%-9d %-4d %-24s %s%n",
                    e.getIdComanda(), e.getNumeroProgressivo(),
                    e.getProdotto(), e.getModalita());
        }
    }

    public void showComandeAperte(List<ComandaAperta> comande) {
        if (comande.isEmpty()) {
            System.out.println("\nNessuna comanda aperta.");
            return;
        }
        System.out.printf("%n%-6s %-10s %-20s %s%n", "ID", "MODALITA", "APERTURA", "DESTINAZIONE");
        for (ComandaAperta c : comande) {
            String destinazione;
            if (c.getNumeroTavolo() != null) {
                destinazione = "Tavolo " + c.getNumeroTavolo();
            } else if (c.getIndirizzo() != null) {
                destinazione = c.getIndirizzo() + " (" + c.getTelefonoCliente() + ")";
            } else {
                destinazione = "Ritiro in sede";
            }
            System.out.printf("%-6d %-10s %-20s %s%n",
                    c.getId(), c.getTipo(), c.getDataApertura(), destinazione);
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

    public void showScontrino(Scontrino scontrino) {
        if (scontrino.isEmpty()) {
            System.out.println("\nNessuna comanda completata con questo numero.");
            return;
        }
        System.out.println();
        System.out.println("--------------------------------------");
        System.out.println("        SCONTRINO NON FISCALE");
        System.out.println("--------------------------------------");
        System.out.println("Comanda:  " + scontrino.getIdComanda() + "  (" + scontrino.getTipo() + ")");
        System.out.println("Chiusura: " + scontrino.getDataChiusura());
        System.out.println("--------------------------------------");
        for (RigaScontrino r : scontrino.getRighe()) {
            System.out.printf("%-3d %-24s %8s%n",
                    r.getNumeroProgressivo(), r.getProdotto(), r.getPrezzoUnitario());
        }
        System.out.println("--------------------------------------");
        System.out.printf("%-28s %8s%n", "TOTALE", scontrino.getTotale() + " EUR");
        System.out.println("--------------------------------------");
    }

    public void showEntrate(String periodo, Entrate entrate) {
        System.out.printf("%nEntrate %s: %d comande completate, incasso %s EUR%n",
                periodo, entrate.getNumeroComande(), entrate.getIncasso());
    }

    public AperturaComandaConsegnaRequest askComandaConsegna(String username) {
        String telefono = ConsoleInput.readLine("Recapito telefonico: ").trim();
        if (telefono.isEmpty()) {
            System.out.println("Recapito obbligatorio, operazione annullata.");
            return null;
        }
        String nome = ConsoleInput.readLine("Nome del cliente: ").trim();
        String indirizzo = ConsoleInput.readLine("Indirizzo di consegna: ").trim();
        if (nome.isEmpty() || indirizzo.isEmpty()) {
            System.out.println("Nome e indirizzo obbligatori, operazione annullata.");
            return null;
        }
        return new AperturaComandaConsegnaRequest(telefono, nome, indirizzo, username);
    }

    public ConsegnaRequest askRitiro() {
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

    /** Restituisce null se il valore inserito non e numerico. */
    public Integer askIdComanda() {
        return ConsoleInput.readIntOrNull("Numero comanda: ");
    }

    public LocalDate askData() {
        String testo = ConsoleInput.readLine("Data (aaaa-mm-gg, vuoto per oggi): ").trim();
        if (testo.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(testo);
        } catch (DateTimeParseException e) {
            System.out.println("Data non valida, operazione annullata.");
            return null;
        }
    }

    public EntrateMensiliRequest askMese() {
        Integer anno = ConsoleInput.readIntOrNull("Anno: ");
        if (anno == null) {
            return null;
        }
        int mese = ConsoleInput.readInt("Mese (1-12): ", 1, 12);
        return new EntrateMensiliRequest(anno, mese);
    }

    /**
     * Un codice vuoto indica un nuovo prodotto; se valorizzato, la procedura
     * aggiorna quello esistente.
     */
    public ProdottoRequest askProdotto() {
        String codiceTesto = ConsoleInput.readLine(
                "Codice prodotto da modificare (vuoto per inserirne uno nuovo): ").trim();

        Integer codice = null;
        if (!codiceTesto.isEmpty()) {
            try {
                codice = Integer.valueOf(codiceTesto);
            } catch (NumberFormatException e) {
                System.out.println("Codice non valido, operazione annullata.");
                return null;
            }
        }

        String nome = ConsoleInput.readLine("Nome: ").trim();
        if (nome.isEmpty()) {
            System.out.println("Nome obbligatorio, operazione annullata.");
            return null;
        }

        BigDecimal prezzo;
        try {
            prezzo = new BigDecimal(ConsoleInput.readLine("Prezzo: ").trim());
        } catch (NumberFormatException e) {
            System.out.println("Prezzo non valido, operazione annullata.");
            return null;
        }

        int tipo = ConsoleInput.readInt("Tipo (1 = pizza, 2 = bevanda): ", 1, 2);
        int disp = ConsoleInput.readInt("Disponibile (1 = si, 0 = no): ", 0, 1);

        return new ProdottoRequest(codice, nome, prezzo,
                tipo == 1 ? "PIZZA" : "BEVANDA", disp == 1);
    }

    public DipendenteRequest askDipendente() {
        String username = ConsoleInput.readLine("Username: ").trim();
        String password = ConsoleInput.readLine("Password: ").trim();
        String nome = ConsoleInput.readLine("Nome: ").trim();
        String cognome = ConsoleInput.readLine("Cognome: ").trim();

        if (username.isEmpty() || password.isEmpty() || nome.isEmpty() || cognome.isEmpty()) {
            System.out.println("Tutti i campi sono obbligatori, operazione annullata.");
            return null;
        }

        int scelta = ConsoleInput.readInt(
                "Ruolo (1 = cameriere, 2 = pizzaiolo, 3 = barman, 4 = gestore): ", 1, 4);
        String ruolo = switch (scelta) {
            case 1 -> "CAMERIERE";
            case 2 -> "PIZZAIOLO";
            case 3 -> "BARMAN";
            default -> "GESTORE";
        };

        return new DipendenteRequest(username, password, nome, cognome, ruolo);
    }

    public String askUsername() {
        String username = ConsoleInput.readLine("Username del dipendente: ").trim();
        return username.isEmpty() ? null : username;
    }

    public void showEsito(String messaggio) {
        System.out.println(messaggio);
    }

    public void showError(String messaggio) {
        System.out.println("Errore: " + messaggio);
    }
}
