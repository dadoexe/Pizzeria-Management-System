package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.domain.TavoloLibero;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Procedura accessoria di supporto all'operazione C1: elenca i tavoli
 * attualmente liberi, sfruttando l'attributo ridondante stato della tabella
 * Tavolo introdotto in fase di ristrutturazione.
 */
public class TavoliLiberiProcedureDAO
        implements GenericProcedureDAO<Void, List<TavoloLibero>> {

    @Override
    public List<TavoloLibero> execute(Void input) throws DAOException {
        List<TavoloLibero> tavoli = new ArrayList<>();

        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_tavoli_liberi()}");

            if (cs.execute()) {
                ResultSet rs = cs.getResultSet();
                while (rs.next()) {
                    tavoli.add(new TavoloLibero(rs.getInt("numero"), rs.getInt("posti")));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Lettura dei tavoli liberi fallita: " + SqlErrorMessage.of(e));
        }

        return tavoli;
    }
}
