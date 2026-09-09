package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.domain.ElementoPronto;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Operazione C3. Restituisce i prodotti pronti delle sole comande consumate
 * sul posto, con il tavolo a cui vanno serviti; il numero del tavolo non e
 * mai nullo, essendo la vista filtrata sulla modalita LOCALE.
 */
public class ElementiProntiSalaProcedureDAO
        implements GenericProcedureDAO<Void, List<ElementoPronto>> {

    @Override
    public List<ElementoPronto> execute(Void input) throws DAOException {
        List<ElementoPronto> pronti = new ArrayList<>();

        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_elementi_pronti_sala()}");

            if (cs.execute()) {
                ResultSet rs = cs.getResultSet();
                while (rs.next()) {
                    pronti.add(new ElementoPronto(
                            rs.getInt("IDComanda"),
                            rs.getInt("numeroProgressivo"),
                            rs.getString("prodotto"),
                            rs.getInt("numeroTavolo")));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Lettura dei prodotti pronti fallita: " + SqlErrorMessage.of(e));
        }

        return pronti;
    }
}
