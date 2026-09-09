package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.dto.DipendenteRequest;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Operazione G10. La password viene trasmessa in chiaro alla procedura, che
 * ne calcola il digest prima di memorizzarla: il valore originale non viene
 * mai scritto sulla base di dati.
 */
public class RegistraDipendenteProcedureDAO
        implements GenericProcedureDAO<DipendenteRequest, Void> {

    @Override
    public Void execute(DipendenteRequest input) throws DAOException {
        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_registra_dipendente(?,?,?,?,?)}");
            cs.setString(1, input.username());
            cs.setString(2, input.password());
            cs.setString(3, input.nome());
            cs.setString(4, input.cognome());
            cs.setString(5, input.ruolo());
            cs.execute();
        } catch (SQLException e) {
            throw new DAOException(SqlErrorMessage.of(e));
        }

        return null;
    }
}
