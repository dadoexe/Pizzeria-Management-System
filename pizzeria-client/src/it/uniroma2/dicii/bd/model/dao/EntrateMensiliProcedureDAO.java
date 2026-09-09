package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.domain.Entrate;
import it.uniroma2.dicii.bd.model.dto.EntrateMensiliRequest;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Operazione G8. */
public class EntrateMensiliProcedureDAO
        implements GenericProcedureDAO<EntrateMensiliRequest, Entrate> {

    @Override
    public Entrate execute(EntrateMensiliRequest input) throws DAOException {
        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_entrate_mensili(?,?)}");
            cs.setInt(1, input.anno());
            cs.setInt(2, input.mese());

            if (cs.execute()) {
                ResultSet rs = cs.getResultSet();
                if (rs.next()) {
                    return new Entrate(rs.getInt("numeroComande"),
                                       rs.getBigDecimal("incasso"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Calcolo delle entrate fallito: " + SqlErrorMessage.of(e));
        }

        return new Entrate(0, BigDecimal.ZERO);
    }
}
