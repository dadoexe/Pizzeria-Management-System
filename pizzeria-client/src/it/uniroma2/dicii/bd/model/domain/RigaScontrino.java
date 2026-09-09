package it.uniroma2.dicii.bd.model.domain;

import java.math.BigDecimal;

/**
 * Singola riga dello scontrino. Il prezzo e quello congelato sull'elemento
 * al momento dell'ordinazione, non quello corrente del menu.
 */
public class RigaScontrino {

    private final int numeroProgressivo;
    private final String prodotto;
    private final BigDecimal prezzoUnitario;

    public RigaScontrino(int numeroProgressivo, String prodotto, BigDecimal prezzoUnitario) {
        this.numeroProgressivo = numeroProgressivo;
        this.prodotto = prodotto;
        this.prezzoUnitario = prezzoUnitario;
    }

    public int getNumeroProgressivo() {
        return numeroProgressivo;
    }

    public String getProdotto() {
        return prodotto;
    }

    public BigDecimal getPrezzoUnitario() {
        return prezzoUnitario;
    }
}
