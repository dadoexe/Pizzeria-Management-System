package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.dto.ProdottoRequest;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Operazione G9: inserisce un nuovo prodotto oppure ne aggiorna uno esistente,
 * inclusa la sospensione temporanea della disponibilita.
 *
 * La procedura restituisce un ResultSet con il codice generato solo nel ramo
 * di inserimento, mentre in quello di modifica non restituisce nulla: il
 * valore booleano di execute() va quindi verificato prima di leggere.
 */
public class SalvaProdottoProcedureDAO
        implements GenericProcedureDAO<ProdottoRequest, Integer> {

    @Override
    public Integer execute(ProdottoRequest input) throws DAOException {
        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_salva_prodotto(?,?,?,?,?)}");

            if (input.codice() == null) {
                cs.setNull(1, Types.INTEGER);
            } else {
                cs.setInt(1, input.codice());
            }
            cs.setString(2, input.nome());
            cs.setBigDecimal(3, input.prezzo());
            cs.setString(4, input.tipo());
            cs.setBoolean(5, input.disponibile());

            if (cs.execute()) {
                ResultSet rs = cs.getResultSet();
                if (rs.next()) {
                    return rs.getInt("codice");
                }
            }
            return input.codice();
        } catch (SQLException e) {
            throw new DAOException(SqlErrorMessage.of(e));
        }
    }
}
