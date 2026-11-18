package com.juegos.radar.dominio.puertos.entrada;

import com.juegos.radar.dominio.modelo.ComentarioOferta;

import java.util.List;
import java.util.Optional;

public interface ComentarioOfertaUseCase {

    List<ComentarioOferta> listarRecientes(int limite);

    List<ComentarioOferta> listarPorOferta(String dealId);

    ComentarioOferta publicarComentario(String dealId, String autor, String contenido, int calificacion);

    Optional<ComentarioOferta> editarComentario(String id, String contenido, int calificacion);

    boolean eliminarComentario(String id);
}

