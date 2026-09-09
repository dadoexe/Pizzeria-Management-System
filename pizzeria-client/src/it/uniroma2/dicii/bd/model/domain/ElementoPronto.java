package it.uniroma2.dicii.bd.model.domain;

/** Riga restituita da sp_elementi_pronti_sala. */
public class ElementoPronto {

    private final int idComanda;
    private final int numeroProgressivo;
    private final String prodotto;
    private final int numeroTavolo;

    public ElementoPronto(int idComanda, int numeroProgressivo,
                          String prodotto, int numeroTavolo) {
        this.idComanda = idComanda;
        this.numeroProgressivo = numeroProgressivo;
        this.prodotto = prodotto;
        this.numeroTavolo = numeroTavolo;
    }

    public int getIdComanda() {
        return idComanda;
    }

    public int getNumeroProgressivo() {
        return numeroProgressivo;
    }

    public String getProdotto() {
        return prodotto;
    }

    public int getNumeroTavolo() {
        return numeroTavolo;
    }
}
