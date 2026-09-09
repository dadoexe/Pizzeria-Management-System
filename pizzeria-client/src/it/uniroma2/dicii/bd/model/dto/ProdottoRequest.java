package it.uniroma2.dicii.bd.model.dto;

import java.math.BigDecimal;

/** Il codice e null quando si tratta di un nuovo prodotto da inserire. */
public record ProdottoRequest(Integer codice, String nome, BigDecimal prezzo,
                              String tipo, boolean disponibile) {
}
