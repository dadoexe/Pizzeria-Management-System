package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.model.domain.Role;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestisce l'unica connessione dell'applicazione.
 *
 * Prima dell'autenticazione la connessione e aperta con l'utente app_login,
 * che puo eseguire soltanto sp_login. Una volta noto il ruolo, changeRole()
 * riapre la connessione con le credenziali dell'utente corrispondente: da quel
 * momento il DBMS stesso impedisce di invocare procedure non di competenza.
 */
public class ConnectionFactory {

    private static final String PROPERTIES_PATH = "resources/db.properties";

    private static Connection connection;

    private ConnectionFactory() {}

    static {
        try {
            connection = open("LOGIN");
        } catch (IOException | SQLException e) {
            throw new ExceptionInInitializerError(
                    "Impossibile connettersi alla base di dati: " + e.getMessage());
        }
    }

    private static Connection open(String prefix) throws IOException, SQLException {
        try (InputStream input = new FileInputStream(PROPERTIES_PATH)) {
            Properties properties = new Properties();
            properties.load(input);

            String url = properties.getProperty("CONNECTION_URL");
            String user = properties.getProperty(prefix + "_USER");
            String pass = properties.getProperty(prefix + "_PASS");

            return DriverManager.getConnection(url, user, pass);
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    /**
     * Riapre la connessione con le credenziali del ruolo indicato. La vecchia
     * connessione viene chiusa solo dopo che la nuova e stata stabilita, cosi
     * che un errore non lasci l'applicazione senza connessione.
     */
    public static void changeRole(Role role) throws SQLException {
        Connection nuova;
        try {
            nuova = open(role.name());
        } catch (IOException e) {
            throw new SQLException("Lettura di " + PROPERTIES_PATH + " fallita", e);
        }

        Connection vecchia = connection;
        connection = nuova;

        if (vecchia != null && !vecchia.isClosed()) {
            vecchia.close();
        }
    }
}
