package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.domain.ElementoDaRitirare;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Operazione G4: prodotti pronti delle comande da asporto e in consegna. */
public class ElementiProntiRitiroProcedureDAO
        implements GenericProcedureDAO<Void, List<ElementoDaRitirare>> {

    @Override
    public List<ElementoDaRitirare> execute(Void input) throws DAOException {
        List<ElementoDaRitirare> pronti = new ArrayList<>();

        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_elementi_pronti_ritiro()}");

            if (cs.execute()) {
                ResultSet rs = cs.getResultSet();
                while (rs.next()) {
                    pronti.add(new ElementoDaRitirare(
                            rs.getInt("IDComanda"),
                            rs.getInt("numeroProgressivo"),
                            rs.getString("prodotto"),
                            rs.getString("modalita")));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Lettura dei prodotti da ritirare fallita: "
                    + SqlErrorMessage.of(e));
        }

        return pronti;
    }
}
