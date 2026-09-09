package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.dto.PreparazioneRequest;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Operazioni P2 e B2. Ogni controllo (esistenza dell'elemento, stato di
 * preparazione, corrispondenza tra ruolo e tipo di prodotto) e effettuato
 * dalla procedura: qui non viene replicata alcuna verifica.
 */
public class SegnaPreparatoProcedureDAO
        implements GenericProcedureDAO<PreparazioneRequest, Void> {

    @Override
    public Void execute(PreparazioneRequest input) throws DAOException {
        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_segna_elemento_preparato(?,?,?)}");
            cs.setInt(1, input.idComanda());
            cs.setInt(2, input.numeroProgressivo());
            cs.setString(3, input.username());
            cs.execute();
        } catch (SQLException e) {
            throw new DAOException(SqlErrorMessage.of(e));
        }

        return null;
    }
}
