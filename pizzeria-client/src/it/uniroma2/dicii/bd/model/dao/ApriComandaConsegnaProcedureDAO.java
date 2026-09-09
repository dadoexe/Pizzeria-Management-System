package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.dto.AperturaComandaConsegnaRequest;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Operazione G2. La procedura registra il cliente in anagrafica solo se il
 * recapito telefonico non e gia presente, e apre la comanda in un'unica
 * transazione.
 */
public class ApriComandaConsegnaProcedureDAO
        implements GenericProcedureDAO<AperturaComandaConsegnaRequest, Integer> {

    @Override
    public Integer execute(AperturaComandaConsegnaRequest input) throws DAOException {
        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_apri_comanda_consegna(?,?,?,?,?)}");
            cs.setString(1, input.telefono());
            cs.setString(2, input.nome());
            cs.setString(3, input.indirizzo());
            cs.setString(4, input.username());
            cs.registerOutParameter(5, Types.INTEGER);
            cs.execute();
            return cs.getInt(5);
        } catch (SQLException e) {
            throw new DAOException(SqlErrorMessage.of(e));
        }
    }
}
