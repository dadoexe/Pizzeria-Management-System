package it.uniroma2.dicii.bd.model.domain;

/** Riga restituita da sp_tavoli_liberi. */
public class TavoloLibero {

    private final int numero;
    private final int posti;

    public TavoloLibero(int numero, int posti) {
        this.numero = numero;
        this.posti = posti;
    }

    public int getNumero() {
        return numero;
    }

    public int getPosti() {
        return posti;
    }
}
