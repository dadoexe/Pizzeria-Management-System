package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Operazione G5. In un'unica transazione la procedura verifica che tutti i
 * prodotti siano stati consegnati, calcola e memorizza il totale e libera il
 * tavolo se la comanda era consumata sul posto.
 */
public class CompletaComandaProcedureDAO
        implements GenericProcedureDAO<Integer, Void> {

    @Override
    public Void execute(Integer idComanda) throws DAOException {
        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_completa_comanda(?)}");
            cs.setInt(1, idComanda);
            cs.execute();
        } catch (SQLException e) {
            throw new DAOException(SqlErrorMessage.of(e));
        }

        return null;
    }
}
