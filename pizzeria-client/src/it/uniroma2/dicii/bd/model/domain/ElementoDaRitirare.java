package it.uniroma2.dicii.bd.model.domain;

/** Riga restituita da sp_elementi_pronti_ritiro. */
public class ElementoDaRitirare {

    private final int idComanda;
    private final int numeroProgressivo;
    private final String prodotto;
    private final String modalita;

    public ElementoDaRitirare(int idComanda, int numeroProgressivo,
                              String prodotto, String modalita) {
        this.idComanda = idComanda;
        this.numeroProgressivo = numeroProgressivo;
        this.prodotto = prodotto;
        this.modalita = modalita;
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
}
