package com.juegos.radar.dominio.puertos.salida;

import com.juegos.radar.dominio.modelo.EstadisticaRadar;
import com.juegos.radar.dominio.modelo.OfertaJuego;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OfertaRepositorioPort {

    void guardarLote(List<OfertaJuego> ofertas);

    List<OfertaJuego> buscarConFiltros(String texto, String tienda, double ahorroMinimo, int pagina, int tamano);

    Optional<OfertaJuego> buscarPorDealId(String dealId);

    Optional<OfertaJuego> incrementarLike(String dealId);

    default Optional<OfertaJuego> actualizarDestacadoYAlerta(String dealId, Double precioAlerta, Boolean destacada) {
        return Optional.empty();
    }

    default Map<String, Long> clasificarOfertasMasivo() {
        return Map.of();
    }

    default long purgarPorAhorroMenorA(double ahorroMenorA) {
        return 0;
    }

    default Map<String, Object> generarYGuardarReporteBi(long totalComentarios) {
        return Map.of();
    }

    long contarTotal();

    EstadisticaRadar calcularEstadisticas(long totalComentarios);
}

