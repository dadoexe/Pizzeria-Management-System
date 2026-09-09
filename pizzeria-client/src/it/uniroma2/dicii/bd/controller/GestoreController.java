package it.uniroma2.dicii.bd.controller;

import it.uniroma2.dicii.bd.exception.ApplicationException;
import it.uniroma2.dicii.bd.exception.ControllerException;
import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.dao.AggiungiElementoProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.ApriComandaAsportoProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.ApriComandaConsegnaProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.ComandeAperteProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.CompletaComandaProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.ConnectionFactory;
import it.uniroma2.dicii.bd.model.dao.ElementiProntiRitiroProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.EntrateGiornaliereProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.EntrateMensiliProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.ListaMenuProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.RegistraDipendenteProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.RimuoviDipendenteProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.SalvaProdottoProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.ScontrinoProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.SegnaConsegnatoProcedureDAO;
import it.uniroma2.dicii.bd.model.domain.Credentials;
import it.uniroma2.dicii.bd.model.domain.Role;
import it.uniroma2.dicii.bd.model.dto.AggiuntaElementoRequest;
import it.uniroma2.dicii.bd.model.dto.AperturaComandaConsegnaRequest;
import it.uniroma2.dicii.bd.model.dto.ConsegnaRequest;
import it.uniroma2.dicii.bd.model.dto.DipendenteRequest;
import it.uniroma2.dicii.bd.model.dto.EntrateMensiliRequest;
import it.uniroma2.dicii.bd.model.dto.ProdottoRequest;
import it.uniroma2.dicii.bd.view.GestoreView;

import java.sql.SQLException;
import java.time.LocalDate;

public class GestoreController implements Controller {

    private final GestoreView view = new GestoreView();
    private final Credentials cred;

    public GestoreController(Credentials cred) {
        this.cred = cred;
    }

    @Override
    public void start() throws ApplicationException {
        try {
            ConnectionFactory.changeRole(Role.GESTORE);
        } catch (SQLException e) {
            throw new ApplicationException(e.getMessage(), e);
        }

        while (true) {
            int choice = view.showMenu();
            try {
                switch (choice) {
                    case 1  -> apriComandaAsporto();
                    case 2  -> apriComandaConsegna();
                    case 3  -> aggiungiProdotto();
                    case 4  -> mostraDaRitirare();
                    case 5  -> segnaRitirato();
                    case 6  -> mostraComandeAperte();
                    case 7  -> completaComanda();
                    case 8  -> stampaScontrino();
                    case 9  -> entrateGiornaliere();
                    case 10 -> entrateMensili();
                    case 11 -> gestisciMenu();
                    case 12 -> registraDipendente();
                    case 13 -> rimuoviDipendente();
                    case 14 -> System.exit(0);
                    default -> throw new ControllerException("Scelta non valida");
                }
            } catch (ControllerException e) {
                view.showError(e.getMessage());
            }
        }
    }

    /** Operazione G1. */
    private void apriComandaAsporto() throws ControllerException {
        try {
            int id = new ApriComandaAsportoProcedureDAO().execute(cred.getUsername());
            view.showEsito("Comanda da asporto " + id + " aperta.");
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Operazione G2. */
    private void apriComandaConsegna() throws ControllerException {
        AperturaComandaConsegnaRequest request = view.askComandaConsegna(cred.getUsername());
        if (request == null) {
            return;
        }
        try {
            int id = new ApriComandaConsegnaProcedureDAO().execute(request);
            view.showEsito("Comanda in consegna " + id + " registrata.");
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Operazione G3. */
    private void aggiungiProdotto() throws ControllerException {
        try {
            view.showComandeAperte(new ComandeAperteProcedureDAO().execute(null));
            view.showMenuProdotti(new ListaMenuProcedureDAO().execute(null));

            Integer idComanda = view.askIdComanda();
            if (idComanda == null) {
                return;
            }
            Integer codice = it.uniroma2.dicii.bd.view.ConsoleInput
                    .readIntOrNull("Codice prodotto: ");
            if (codice == null) {
                return;
            }

            int numero = new AggiungiElementoProcedureDAO()
                    .execute(new AggiuntaElementoRequest(idComanda, codice));
            view.showEsito("Prodotto aggiunto alla comanda " + idComanda
                    + " con numero progressivo " + numero + ".");
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Operazione G4. */
    private void mostraDaRitirare() throws ControllerException {
        try {
            view.showElementiDaRitirare(new ElementiProntiRitiroProcedureDAO().execute(null));
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /**
     * Operazione C4 svolta dal gestore: la specifica prevede che sia lui a
     * ritirare i prodotti dalla cucina per le comande da asporto e in
     * consegna, consegnandoli al cliente o al fattorino.
     */
    private void segnaRitirato() throws ControllerException {
        ConsegnaRequest request = view.askRitiro();
        if (request == null) {
            return;
        }
        try {
            new SegnaConsegnatoProcedureDAO().execute(request);
            view.showEsito("Prodotto segnato come ritirato.");
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Procedura accessoria di supporto alle operazioni sulle comande. */
    private void mostraComandeAperte() throws ControllerException {
        try {
            view.showComandeAperte(new ComandeAperteProcedureDAO().execute(null));
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Operazione G5. */
    private void completaComanda() throws ControllerException {
        try {
            view.showComandeAperte(new ComandeAperteProcedureDAO().execute(null));

            Integer idComanda = view.askIdComanda();
            if (idComanda == null) {
                return;
            }

            new CompletaComandaProcedureDAO().execute(idComanda);
            view.showEsito("Comanda " + idComanda + " completata.");
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Operazione G6. */
    private void stampaScontrino() throws ControllerException {
        Integer idComanda = view.askIdComanda();
        if (idComanda == null) {
            return;
        }
        try {
            view.showScontrino(new ScontrinoProcedureDAO().execute(idComanda));
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Operazione G7. */
    private void entrateGiornaliere() throws ControllerException {
        LocalDate giorno = view.askData();
        if (giorno == null) {
            return;
        }
        try {
            view.showEntrate("del " + giorno,
                    new EntrateGiornaliereProcedureDAO().execute(giorno));
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Operazione G8. */
    private void entrateMensili() throws ControllerException {
        EntrateMensiliRequest request = view.askMese();
        if (request == null) {
            return;
        }
        try {
            view.showEntrate("di " + request.mese() + "/" + request.anno(),
                    new EntrateMensiliProcedureDAO().execute(request));
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Operazione G9. */
    private void gestisciMenu() throws ControllerException {
        try {
            view.showMenuProdotti(new ListaMenuProcedureDAO().execute(null));

            ProdottoRequest request = view.askProdotto();
            if (request == null) {
                return;
            }

            Integer codice = new SalvaProdottoProcedureDAO().execute(request);
            view.showEsito("Prodotto salvato con codice " + codice + ".");
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Operazione G10. */
    private void registraDipendente() throws ControllerException {
        DipendenteRequest request = view.askDipendente();
        if (request == null) {
            return;
        }
        try {
            new RegistraDipendenteProcedureDAO().execute(request);
            view.showEsito("Dipendente " + request.username() + " registrato.");
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Operazione G10. */
    private void rimuoviDipendente() throws ControllerException {
        String username = view.askUsername();
        if (username == null) {
            return;
        }
        try {
            new RimuoviDipendenteProcedureDAO().execute(username);
            view.showEsito("Dipendente " + username + " rimosso.");
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }
}
