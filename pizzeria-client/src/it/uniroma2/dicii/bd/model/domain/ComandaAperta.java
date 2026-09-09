package it.uniroma2.dicii.bd.model.domain;

import java.sql.Timestamp;

/**
 * Riga restituita da sp_comande_aperte. Tavolo, recapito e indirizzo sono
 * valorizzati soltanto per la modalita che li prevede: sono gli attributi
 * opzionali introdotti accorpando la generalizzazione della comanda.
 */
public class ComandaAperta {

    private final int id;
    private final String tipo;
    private final Timestamp dataApertura;
    private final Integer numeroTavolo;
    private final String telefonoCliente;
    private final String indirizzo;

    public ComandaAperta(int id, String tipo, Timestamp dataApertura,
                         Integer numeroTavolo, String telefonoCliente, String indirizzo) {
        this.id = id;
        this.tipo = tipo;
        this.dataApertura = dataApertura;
        this.numeroTavolo = numeroTavolo;
        this.telefonoCliente = telefonoCliente;
        this.indirizzo = indirizzo;
    }

    public int getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public Timestamp getDataApertura() {
        return dataApertura;
    }

    public Integer getNumeroTavolo() {
        return numeroTavolo;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public String getIndirizzo() {
        return indirizzo;
    }
}
