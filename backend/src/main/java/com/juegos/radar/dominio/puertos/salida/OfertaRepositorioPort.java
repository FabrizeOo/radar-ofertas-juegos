package com.juegos.radar.dominio.puertos.salida;

import com.juegos.radar.dominio.modelo.EstadisticaRadar;
import com.juegos.radar.dominio.modelo.OfertaJuego;

import java.util.List;
import java.util.Optional;

public interface OfertaRepositorioPort {

    void guardarLote(List<OfertaJuego> ofertas);

    List<OfertaJuego> buscarConFiltros(String texto, String tienda, double ahorroMinimo, int pagina, int tamano);

    Optional<OfertaJuego> buscarPorDealId(String dealId);

    Optional<OfertaJuego> incrementarLike(String dealId);

    long contarTotal();

    EstadisticaRadar calcularEstadisticas(long totalComentarios);
}

