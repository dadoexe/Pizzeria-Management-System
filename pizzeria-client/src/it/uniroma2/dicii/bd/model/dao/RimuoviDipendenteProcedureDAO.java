package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Operazione G10. La procedura rifiuta la rimozione di un dipendente che abbia
 * comande o preparazioni registrate a suo nome, a tutela della tracciabilita
 * dei dati contabili.
 */
public class RimuoviDipendenteProcedureDAO
        implements GenericProcedureDAO<String, Void> {

    @Override
    public Void execute(String username) throws DAOException {
        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_rimuovi_dipendente(?)}");
            cs.setString(1, username);
            cs.execute();
        } catch (SQLException e) {
            throw new DAOException(SqlErrorMessage.of(e));
        }

        return null;
    }
}
