package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.dto.AggiuntaElementoRequest;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Operazioni C2 e G3. La procedura verifica che la comanda sia ancora aperta
 * e il prodotto disponibile, congela il prezzo corrente sull'elemento e
 * restituisce il numero progressivo assegnato.
 */
public class AggiungiElementoProcedureDAO
        implements GenericProcedureDAO<AggiuntaElementoRequest, Integer> {

    @Override
    public Integer execute(AggiuntaElementoRequest input) throws DAOException {
        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_aggiungi_elemento(?,?,?)}");
            cs.setInt(1, input.idComanda());
            cs.setInt(2, input.codiceProdotto());
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            return cs.getInt(3);
        } catch (SQLException e) {
            throw new DAOException(SqlErrorMessage.of(e));
        }
    }
}
