package it.uniroma2.dicii.bd.controller;

import it.uniroma2.dicii.bd.exception.ApplicationException;
import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.dao.LoginProcedureDAO;
import it.uniroma2.dicii.bd.model.domain.Credentials;
import it.uniroma2.dicii.bd.model.dto.LoginRequest;
import it.uniroma2.dicii.bd.view.LoginView;

public class LoginController implements Controller {

    private Credentials cred = null;

    @Override
    public void start() throws ApplicationException {
        cred = LoginView.authenticate();

        LoginRequest request = new LoginRequest(cred.getUsername(), cred.getPassword());

        try {
            cred = new LoginProcedureDAO().execute(request);
        } catch (DAOException e) {
            // La procedura sp_login segnala essa stessa le credenziali non valide
            throw new ApplicationException(e.getMessage(), e);
        }
    }

    public Credentials getCred() {
        return cred;
    }
}
