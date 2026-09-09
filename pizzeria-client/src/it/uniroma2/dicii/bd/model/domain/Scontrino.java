package it.uniroma2.dicii.bd.model.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Scontrino non fiscale di riepilogo di una comanda completata.
 * L'intestazione e comune a tutte le righe restituite dalla procedura e
 * viene quindi valorizzata una sola volta, alla prima riga letta.
 */
public class Scontrino {

    private int idComanda;
    private String tipo;
    private Timestamp dataChiusura;
    private BigDecimal totale;
    private final List<RigaScontrino> righe = new ArrayList<>();

    public void setIntestazione(int idComanda, String tipo,
                                Timestamp dataChiusura, BigDecimal totale) {
        this.idComanda = idComanda;
        this.tipo = tipo;
        this.dataChiusura = dataChiusura;
        this.totale = totale;
    }

    public void addRiga(RigaScontrino riga) {
        righe.add(riga);
    }

    public boolean isEmpty() {
        return righe.isEmpty();
    }

    public int getIdComanda() {
        return idComanda;
    }

    public String getTipo() {
        return tipo;
    }

    public Timestamp getDataChiusura() {
        return dataChiusura;
    }

    public BigDecimal getTotale() {
        return totale;
    }

    public List<RigaScontrino> getRighe() {
        return righe;
    }
}
