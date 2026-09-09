package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.domain.RigaScontrino;
import it.uniroma2.dicii.bd.model.domain.Scontrino;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Operazione G6. La procedura restituisce una riga per prodotto, ciascuna
 * ripetendo i dati di intestazione della comanda: questi vengono quindi
 * valorizzati una sola volta, alla prima riga letta.
 */
public class ScontrinoProcedureDAO
        implements GenericProcedureDAO<Integer, Scontrino> {

    @Override
    public Scontrino execute(Integer idComanda) throws DAOException {
        Scontrino scontrino = new Scontrino();

        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_stampa_scontrino(?)}");
            cs.setInt(1, idComanda);

            if (cs.execute()) {
                ResultSet rs = cs.getResultSet();
                boolean primaRiga = true;
                while (rs.next()) {
                    if (primaRiga) {
                        scontrino.setIntestazione(
                                rs.getInt("ID"),
                                rs.getString("tipo"),
                                rs.getTimestamp("dataChiusura"),
                                rs.getBigDecimal("totale"));
                        primaRiga = false;
                    }
                    scontrino.addRiga(new RigaScontrino(
                            rs.getInt("numeroProgressivo"),
                            rs.getString("prodotto"),
                            rs.getBigDecimal("prezzoUnitario")));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Stampa dello scontrino fallita: " + SqlErrorMessage.of(e));
        }

        return scontrino;
    }
}
