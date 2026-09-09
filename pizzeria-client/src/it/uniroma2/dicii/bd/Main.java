package it.uniroma2.dicii.bd;

import it.uniroma2.dicii.bd.controller.ApplicationController;
import it.uniroma2.dicii.bd.exception.ApplicationException;

public class Main {

    public static void main(String[] args) {
        // Il driver MariaDB, in assenza di un binding SLF4J, stampa sulla
        // console gli errori restituiti dalle procedure, che qui sono invece
        // gestiti e mostrati dalle view. Va impostata prima che il driver
        // venga caricato, quindi come prima istruzione del programma.
        System.setProperty("mariadb.logging.disable", "true");

        try {
            new ApplicationController().start();
        } catch (ApplicationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
