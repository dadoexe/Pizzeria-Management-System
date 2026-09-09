package it.uniroma2.dicii.bd.model.dto;

public record DipendenteRequest(String username, String password, String nome,
                                String cognome, String ruolo) {
}
