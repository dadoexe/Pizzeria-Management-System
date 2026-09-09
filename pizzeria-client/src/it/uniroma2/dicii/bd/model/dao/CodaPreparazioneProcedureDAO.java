package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.domain.ElementoDaPreparare;
import it.uniroma2.dicii.bd.model.domain.TipoProdotto;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Operazioni P1 e B1. La procedura e unica per pizzaiolo e barman ed e
 * parametrizzata sul tipo di prodotto; le righe arrivano gia ordinate per
 * data di apertura della comanda, cosi da rispettare l'ordine di arrivo.
 */
public class CodaPreparazioneProcedureDAO
        implements GenericProcedureDAO<TipoProdotto, List<ElementoDaPreparare>> {

    @Override
    public List<ElementoDaPreparare> execute(TipoProdotto tipo) throws DAOException {
        List<ElementoDaPreparare> coda = new ArrayList<>();

        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_coda_preparazione(?)}");
            cs.setString(1, tipo.name());

            if (cs.execute()) {
                ResultSet rs = cs.getResultSet();
                while (rs.next()) {
                    // wasNull() si riferisce all'ultima colonna letta: va
                    // interrogato subito dopo getInt, prima delle altre letture.
                    int tavolo = rs.getInt("numeroTavolo");
                    Integer numeroTavolo = rs.wasNull() ? null : tavolo;

                    coda.add(new ElementoDaPreparare(
                            rs.getInt("IDComanda"),
                            rs.getInt("numeroProgressivo"),
                            rs.getString("prodotto"),
                            rs.getString("modalita"),
                            numeroTavolo));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Lettura della coda fallita: " + SqlErrorMessage.of(e));
        }

        return coda;
    }
}
