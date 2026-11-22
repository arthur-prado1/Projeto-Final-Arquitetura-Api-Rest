package br.com.fatec.equadras.model;

// Enum: limita o valor do campo a uma lista fixa de opções (qualquer outro valor é recusado)
public enum TipoEsporte {
    FUTEBOL_SOCIETY("Futebol Society"),
    FUTSAL("Futsal"),
    VOLEI("Vôlei"),
    BEACH_TENNIS("Beach Tennis"),
    TENIS("Tênis"),
    BASQUETE("Basquete"),
    PADEL("Padel");

    // Nome para exibir em telas (o JSON das quadras continua usando o código, ex.: "FUTSAL")
    private final String nome;

    TipoEsporte(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
