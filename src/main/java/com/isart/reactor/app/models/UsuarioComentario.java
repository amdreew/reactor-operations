package com.isart.reactor.app.models;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioComentario {
    private Usuario usuario;
    private Comentario comentario;
}
