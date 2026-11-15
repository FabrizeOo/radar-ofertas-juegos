package com.juegos.radar.dominio.modelo;

import java.util.List;

/**
 * Resumen estadístico calculado sobre las colecciones en MongoDB.
 */
public record EstadisticaRadar(
        long totalOfertas,
        long totalComentarios,
        double ahorroPromedio,
        List<ResumenTienda> porTienda
) {
    public record ResumenTienda(
            String tiendaNombre,
            long cantidadOfertas,
            double ahorroPromedio,
            double precioMinimo
    ) {}
}

