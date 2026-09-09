package it.uniroma2.dicii.bd.model.domain;

/**
 * Ruoli previsti dal sistema. I nomi coincidono con i valori dell'attributo
 * ruolo della tabella Dipendente e con i prefissi delle chiavi in db.properties,
 * cosi che ConnectionFactory possa risalire alle credenziali del ruolo.
 */
public enum Role {
    CAMERIERE,
    PIZZAIOLO,
    BARMAN,
    GESTORE;

    public static Role fromString(String name) {
        if (name == null) {
            return null;
        }
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return null;
    }
}
