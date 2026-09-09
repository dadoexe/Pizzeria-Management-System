package it.uniroma2.dicii.bd.controller;

import it.uniroma2.dicii.bd.exception.ApplicationException;
import it.uniroma2.dicii.bd.exception.ControllerException;
import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.dao.AggiungiElementoProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.ApriComandaLocaleProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.ConnectionFactory;
import it.uniroma2.dicii.bd.model.dao.ElementiProntiSalaProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.ListaMenuProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.SegnaConsegnatoProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.TavoliLiberiProcedureDAO;
import it.uniroma2.dicii.bd.model.domain.Credentials;
import it.uniroma2.dicii.bd.model.domain.Role;
import it.uniroma2.dicii.bd.model.dto.AggiuntaElementoRequest;
import it.uniroma2.dicii.bd.model.dto.AperturaComandaLocaleRequest;
import it.uniroma2.dicii.bd.model.dto.ConsegnaRequest;
import it.uniroma2.dicii.bd.view.CameriereView;

import java.sql.SQLException;

public class CameriereController implements Controller {

    private final CameriereView view = new CameriereView();
    private final Credentials cred;

    public CameriereController(Credentials cred) {
        this.cred = cred;
    }

    @Override
    public void start() throws ApplicationException {
        try {
            ConnectionFactory.changeRole(Role.CAMERIERE);
        } catch (SQLException e) {
            throw new ApplicationException(e.getMessage(), e);
        }

        while (true) {
            int choice = view.showMenu();
            try {
                switch (choice) {
                    case 1 -> apriComanda();
                    case 2 -> aggiungiProdotto();
                    case 3 -> mostraProntiDaServire();
                    case 4 -> segnaServito();
                    case 5 -> System.exit(0);
                    default -> throw new ControllerException("Scelta non valida");
                }
            } catch (ControllerException e) {
                view.showError(e.getMessage());
            }
        }
    }

    /** Operazione C1: mostra i tavoli liberi e apre la comanda su quello scelto. */
    private void apriComanda() throws ControllerException {
        try {
            view.showTavoliLiberi(new TavoliLiberiProcedureDAO().execute(null));

            Integer tavolo = view.askNumeroTavolo();
            if (tavolo == null) {
                return;
            }

            AperturaComandaLocaleRequest request =
                    new AperturaComandaLocaleRequest(tavolo, cred.getUsername());
            int idComanda = new ApriComandaLocaleProcedureDAO().execute(request);
            view.showEsito("Comanda " + idComanda + " aperta al tavolo " + tavolo + ".");
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Operazione C2: mostra il menu e aggiunge il prodotto scelto alla comanda. */
    private void aggiungiProdotto() throws ControllerException {
        try {
            view.showMenuProdotti(new ListaMenuProcedureDAO().execute(null));

            AggiuntaElementoRequest request = view.askAggiuntaElemento();
            if (request == null) {
                return;
            }

            int numero = new AggiungiElementoProcedureDAO().execute(request);
            view.showEsito("Prodotto aggiunto alla comanda "
                    + request.idComanda() + " con numero progressivo " + numero + ".");
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Operazione C3. */
    private void mostraProntiDaServire() throws ControllerException {
        try {
            view.showElementiPronti(new ElementiProntiSalaProcedureDAO().execute(null));
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    /** Operazione C4. */
    private void segnaServito() throws ControllerException {
        ConsegnaRequest request = view.askConsegna();
        if (request == null) {
            return;
        }
        try {
            new SegnaConsegnatoProcedureDAO().execute(request);
            view.showEsito("Prodotto segnato come servito.");
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }
}
