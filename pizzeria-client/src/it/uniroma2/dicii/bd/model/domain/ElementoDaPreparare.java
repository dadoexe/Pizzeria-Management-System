package it.uniroma2.dicii.bd.model.domain;

/**
 * Riga della coda di preparazione, come restituita da sp_coda_preparazione.
 * Il numero del tavolo e nullo per le comande da asporto e in consegna.
 */
public class ElementoDaPreparare {

    private final int idComanda;
    private final int numeroProgressivo;
    private final String prodotto;
    private final String modalita;
    private final Integer numeroTavolo;

    public ElementoDaPreparare(int idComanda, int numeroProgressivo, String prodotto,
                               String modalita, Integer numeroTavolo) {
        this.idComanda = idComanda;
        this.numeroProgressivo = numeroProgressivo;
        this.prodotto = prodotto;
        this.modalita = modalita;
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

    public String getModalita() {
        return modalita;
    }

    public Integer getNumeroTavolo() {
        return numeroTavolo;
    }
}
