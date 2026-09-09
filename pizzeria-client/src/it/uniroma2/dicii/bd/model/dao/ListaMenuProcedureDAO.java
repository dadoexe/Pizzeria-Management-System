package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.domain.VoceMenu;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Procedura accessoria di supporto alle operazioni C2 e G3: elenca i soli
 * prodotti attualmente disponibili, gli unici che possono essere aggiunti
 * a una comanda.
 */
public class ListaMenuProcedureDAO
        implements GenericProcedureDAO<Void, List<VoceMenu>> {

    @Override
    public List<VoceMenu> execute(Void input) throws DAOException {
        List<VoceMenu> menu = new ArrayList<>();

        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_lista_menu()}");

            if (cs.execute()) {
                ResultSet rs = cs.getResultSet();
                while (rs.next()) {
                    menu.add(new VoceMenu(
                            rs.getInt("codice"),
                            rs.getString("nome"),
                            rs.getBigDecimal("prezzo"),
                            rs.getString("tipo")));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Lettura del menu fallita: " + SqlErrorMessage.of(e));
        }

        return menu;
    }
}
