package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.domain.ComandaAperta;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Procedura accessoria: elenca le comande ancora aperte, necessaria al gestore
 * per scegliere su quale operare.
 */
public class ComandeAperteProcedureDAO
        implements GenericProcedureDAO<Void, List<ComandaAperta>> {

    @Override
    public List<ComandaAperta> execute(Void input) throws DAOException {
        List<ComandaAperta> comande = new ArrayList<>();

        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_comande_aperte()}");

            if (cs.execute()) {
                ResultSet rs = cs.getResultSet();
                while (rs.next()) {
                    int tavolo = rs.getInt("numeroTavolo");
                    Integer numeroTavolo = rs.wasNull() ? null : tavolo;

                    comande.add(new ComandaAperta(
                            rs.getInt("ID"),
                            rs.getString("tipo"),
                            rs.getTimestamp("dataApertura"),
                            numeroTavolo,
                            rs.getString("telefonoCliente"),
                            rs.getString("indirizzo")));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Lettura delle comande aperte fallita: "
                    + SqlErrorMessage.of(e));
        }

        return comande;
    }
}
