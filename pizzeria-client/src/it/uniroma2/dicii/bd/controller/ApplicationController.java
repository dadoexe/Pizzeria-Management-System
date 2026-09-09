package it.uniroma2.dicii.bd.controller;

import it.uniroma2.dicii.bd.exception.ApplicationException;
import it.uniroma2.dicii.bd.model.domain.Credentials;

public class ApplicationController implements Controller {

    private Credentials cred;

    @Override
    public void start() throws ApplicationException {
        LoginController loginController = new LoginController();
        loginController.start();
        cred = loginController.getCred();

        if (cred == null || cred.getRole() == null) {
            throw new ApplicationException("Credenziali non valide");
        }

        switch (cred.getRole()) {
            case PIZZAIOLO -> new PizzaioloController(cred).start();
            case BARMAN    -> new BarmanController(cred).start();
            case CAMERIERE -> new CameriereController(cred).start();
            case GESTORE   -> new GestoreController(cred).start();
        }
    }
}
