package com.isart.reactor.app.models;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class Comentario {
    public Comentario() {
        this.comentarios = new ArrayList<>();
    }
    private List<String> comentarios;

    public void addComentario(String comentario) {
        this.comentarios.add(comentario);
    }
}
