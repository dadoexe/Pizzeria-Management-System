package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/** Operazione G1: apre una comanda da asporto e ne restituisce l'identificativo. */
public class ApriComandaAsportoProcedureDAO
        implements GenericProcedureDAO<String, Integer> {

    @Override
    public Integer execute(String username) throws DAOException {
        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_apri_comanda_asporto(?,?)}");
            cs.setString(1, username);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.execute();
            return cs.getInt(2);
        } catch (SQLException e) {
            throw new DAOException(SqlErrorMessage.of(e));
        }
    }
}
