package com.juegos.radar.dominio.puertos.salida;

import com.juegos.radar.dominio.modelo.ComentarioOferta;

import java.util.List;
import java.util.Optional;

public interface ComentarioOfertaRepositorioPort {

    ComentarioOferta guardar(ComentarioOferta comentario);

    List<ComentarioOferta> listarRecientes(int limite);

    List<ComentarioOferta> listarPorDealId(String dealId);

    default Optional<ComentarioOferta> actualizar(String id, String contenido, int calificacion) {
        return Optional.empty();
    }

    default boolean eliminarPorId(String id) {
        return false;
    }

    long contarTotal();
}

