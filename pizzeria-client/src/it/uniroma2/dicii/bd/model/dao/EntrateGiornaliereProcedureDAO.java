package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.domain.Entrate;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/** Operazione G7. */
public class EntrateGiornaliereProcedureDAO
        implements GenericProcedureDAO<LocalDate, Entrate> {

    @Override
    public Entrate execute(LocalDate giorno) throws DAOException {
        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_entrate_giornaliere(?)}");
            cs.setDate(1, Date.valueOf(giorno));

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
