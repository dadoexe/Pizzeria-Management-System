package it.uniroma2.dicii.bd.view;

import it.uniroma2.dicii.bd.model.domain.Credentials;

public class LoginView {

    private LoginView() {}

    public static Credentials authenticate() {
        System.out.println("=== Gestione pizzeria ===");
        String username = ConsoleInput.readLine("username: ");
        String password = ConsoleInput.readLine("password: ");
        return new Credentials(username, password, null);
    }
}
