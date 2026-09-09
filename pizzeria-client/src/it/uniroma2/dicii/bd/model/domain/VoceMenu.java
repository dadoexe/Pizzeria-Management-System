package it.uniroma2.dicii.bd.model.domain;

import java.math.BigDecimal;

/**
 * Riga restituita da sp_lista_menu. Il prezzo e un BigDecimal perche la
 * colonna corrispondente e DECIMAL: usare un double introdurrebbe errori
 * di arrotondamento nelle somme.
 */
public class VoceMenu {

    private final int codice;
    private final String nome;
    private final BigDecimal prezzo;
    private final String tipo;

    public VoceMenu(int codice, String nome, BigDecimal prezzo, String tipo) {
        this.codice = codice;
        this.nome = nome;
        this.prezzo = prezzo;
        this.tipo = tipo;
    }

    public int getCodice() {
        return codice;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public String getTipo() {
        return tipo;
    }
}
