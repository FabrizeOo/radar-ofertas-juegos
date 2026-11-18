package com.juegos.radar.dominio.puertos.entrada;

import com.juegos.radar.dominio.modelo.EstadisticaRadar;
import com.juegos.radar.dominio.modelo.OfertaJuego;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OfertaUseCase {

    List<OfertaJuego> buscarOfertas(String texto, String tienda, double ahorroMinimo, int pagina, int tamano);

    Optional<OfertaJuego> obtenerPorDealId(String dealId);

    int sincronizarMasivo(int paginas);

    Optional<OfertaJuego> darLike(String dealId);

    Optional<OfertaJuego> actualizarDestacadoYAlerta(String dealId, Double precioAlerta, Boolean destacada);

    Map<String, Object> clasificarOfertasMasivo();

    Map<String, Object> purgarOfertasBajoDescuento(double ahorroMenorA);

    Map<String, Object> generarYGuardarReporteBi();

    EstadisticaRadar obtenerEstadisticas();
}

