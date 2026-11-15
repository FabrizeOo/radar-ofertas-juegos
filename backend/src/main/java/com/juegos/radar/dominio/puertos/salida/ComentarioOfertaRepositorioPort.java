package com.juegos.radar.dominio.puertos.salida;

import com.juegos.radar.dominio.modelo.ComentarioOferta;

import java.util.List;

public interface ComentarioOfertaRepositorioPort {

    ComentarioOferta guardar(ComentarioOferta comentario);

    List<ComentarioOferta> listarRecientes(int limite);

    List<ComentarioOferta> listarPorDealId(String dealId);

    long contarTotal();
}

