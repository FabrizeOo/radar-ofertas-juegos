package com.juegos.radar.dominio.puertos.entrada;

import com.juegos.radar.dominio.modelo.EstadisticaRadar;
import com.juegos.radar.dominio.modelo.OfertaJuego;

import java.util.List;
import java.util.Optional;

public interface OfertaUseCase {

    List<OfertaJuego> buscarOfertas(String texto, String tienda, double ahorroMinimo, int pagina, int tamano);

    Optional<OfertaJuego> obtenerPorDealId(String dealId);

    int sincronizarMasivo(int paginas);

    Optional<OfertaJuego> darLike(String dealId);

    EstadisticaRadar obtenerEstadisticas();
}

