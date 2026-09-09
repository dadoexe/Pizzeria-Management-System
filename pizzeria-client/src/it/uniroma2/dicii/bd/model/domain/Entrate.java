package it.uniroma2.dicii.bd.model.domain;

import java.math.BigDecimal;

/** Risultato delle operazioni statistiche G7 e G8. */
public class Entrate {

    private final int numeroComande;
    private final BigDecimal incasso;

    public Entrate(int numeroComande, BigDecimal incasso) {
        this.numeroComande = numeroComande;
        this.incasso = incasso;
    }

    public int getNumeroComande() {
        return numeroComande;
    }

    public BigDecimal getIncasso() {
        return incasso;
    }
}
