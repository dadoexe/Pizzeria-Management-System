package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.dto.AperturaComandaLocaleRequest;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Operazione C1. La procedura verifica in transazione che il tavolo sia
 * libero, apre la comanda e marca il tavolo come occupato; restituisce
 * l'identificativo della comanda creata.
 */
public class ApriComandaLocaleProcedureDAO
        implements GenericProcedureDAO<AperturaComandaLocaleRequest, Integer> {

    @Override
    public Integer execute(AperturaComandaLocaleRequest input) throws DAOException {
        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_apri_comanda_locale(?,?,?)}");
            cs.setInt(1, input.numeroTavolo());
            cs.setString(2, input.username());
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            return cs.getInt(3);
        } catch (SQLException e) {
            throw new DAOException(SqlErrorMessage.of(e));
        }
    }
}
