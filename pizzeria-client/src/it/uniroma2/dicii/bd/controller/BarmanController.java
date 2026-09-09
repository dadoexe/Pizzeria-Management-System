package it.uniroma2.dicii.bd.controller;

import it.uniroma2.dicii.bd.exception.ApplicationException;
import it.uniroma2.dicii.bd.exception.ControllerException;
import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.dao.CodaPreparazioneProcedureDAO;
import it.uniroma2.dicii.bd.model.dao.ConnectionFactory;
import it.uniroma2.dicii.bd.model.dao.SegnaPreparatoProcedureDAO;
import it.uniroma2.dicii.bd.model.domain.Credentials;
import it.uniroma2.dicii.bd.model.domain.ElementoDaPreparare;
import it.uniroma2.dicii.bd.model.domain.Role;
import it.uniroma2.dicii.bd.model.domain.TipoProdotto;
import it.uniroma2.dicii.bd.model.dto.PreparazioneRequest;
import it.uniroma2.dicii.bd.view.BarmanView;

import java.sql.SQLException;
import java.util.List;

public class BarmanController implements Controller {

    private final BarmanView view = new BarmanView();
    private final Credentials cred;

    public BarmanController(Credentials cred) {
        this.cred = cred;
    }

    @Override
    public void start() throws ApplicationException {
        try {
            ConnectionFactory.changeRole(Role.BARMAN);
        } catch (SQLException e) {
            throw new ApplicationException(e.getMessage(), e);
        }

        while (true) {
            int choice = view.showMenu();
            try {
                switch (choice) {
                    case 1 -> mostraCoda();
                    case 2 -> segnaPreparata();
                    case 3 -> System.exit(0);
                    default -> throw new ControllerException("Scelta non valida");
                }
            } catch (ControllerException e) {
                view.showError(e.getMessage());
            }
        }
    }

    private void mostraCoda() throws ControllerException {
        try {
            List<ElementoDaPreparare> coda =
                    new CodaPreparazioneProcedureDAO().execute(TipoProdotto.BEVANDA);
            view.showCoda(coda);
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }

    private void segnaPreparata() throws ControllerException {
        PreparazioneRequest request = view.askPreparazione(cred.getUsername());
        if (request == null) {
            return;
        }
        try {
            new SegnaPreparatoProcedureDAO().execute(request);
            view.showEsito("Bevanda segnata come pronta.");
        } catch (DAOException e) {
            throw new ControllerException(e.getMessage(), e);
        }
    }
}
