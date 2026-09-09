package it.uniroma2.dicii.bd.model.dao;

import it.uniroma2.dicii.bd.exception.DAOException;
import it.uniroma2.dicii.bd.model.domain.Credentials;
import it.uniroma2.dicii.bd.model.domain.Role;
import it.uniroma2.dicii.bd.model.dto.LoginRequest;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Operazione L1. La procedura confronta il digest della password e restituisce
 * il ruolo del dipendente; se le credenziali non sono valide solleva un errore,
 * che qui viene tradotto in DAOException.
 */
public class LoginProcedureDAO implements GenericProcedureDAO<LoginRequest, Credentials> {

    @Override
    public Credentials execute(LoginRequest input) throws DAOException {
        String ruolo;

        try {
            Connection conn = ConnectionFactory.getConnection();
            CallableStatement cs = conn.prepareCall("{call sp_login(?,?,?)}");
            cs.setString(1, input.username());
            cs.setString(2, input.password());
            cs.registerOutParameter(3, Types.VARCHAR);
            cs.execute();
            ruolo = cs.getString(3);
        } catch (SQLException e) {
            throw new DAOException(SqlErrorMessage.of(e));
        }

        return new Credentials(input.username(), input.password(), Role.fromString(ruolo));
    }
}
