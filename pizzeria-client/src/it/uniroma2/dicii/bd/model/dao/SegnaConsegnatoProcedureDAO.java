package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.dto.ConsegnaRequest;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Operazione C4. La procedura effettua verifica e aggiornamento in un'unica
 * istruzione, cosi che due camerieri non possano registrare due volte la
 * stessa consegna.
 */
public class SegnaConsegnatoProcedureDAO
        implements GenericProcedureDAO<ConsegnaRequest, Void> {

    @Override
    public Void execute(ConsegnaRequest input) throws DAOException {
        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_segna_elemento_consegnato(?,?)}");
            cs.setInt(1, input.idComanda());
            cs.setInt(2, input.numeroProgressivo());
            cs.execute();
        } catch (SQLException e) {
            throw new DAOException(SqlErrorMessage.of(e));
        }

        return null;
    }
}
