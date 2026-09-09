package it.uniroma2.dicii.bd.model.dao;

import java.sql.SQLException;

/**
 * Estrae il messaggio applicativo da una SQLException.
 *
 * Gli errori sollevati dalle procedure con SIGNAL SQLSTATE '45000' arrivano al
 * client con un messaggio preceduto da informazioni tecniche del driver, del
 * tipo "(conn=5) Comanda gia completata": qui il prefisso viene rimosso, cosi
 * che alla view arrivi il solo testo scritto nella procedura.
 */
final class SqlErrorMessage {

    private SqlErrorMessage() {}

    static String of(SQLException e) {
        String message = e.getMessage();
        if (message == null) {
            return "Errore imprevisto nell'accesso alla base di dati";
        }

        int end = message.indexOf(')');
        if (message.startsWith("(conn=") && end > 0) {
            message = message.substring(end + 1).trim();
        }
        return message;
    }
}
