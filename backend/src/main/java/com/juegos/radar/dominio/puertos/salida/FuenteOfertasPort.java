package com.juegos.radar.dominio.puertos.salida;

import com.juegos.radar.dominio.modelo.OfertaJuego;

import java.util.List;

public interface FuenteOfertasPort {

    List<OfertaJuego> descargarOfertas(int pagina, int tamanoPagina);
}

