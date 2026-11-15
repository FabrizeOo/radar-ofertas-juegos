package com.juegos.radar.infraestructura.configuracion;

import com.juegos.radar.aplicacion.servicio.RadarOfertasService;
import com.juegos.radar.dominio.puertos.salida.ComentarioOfertaRepositorioPort;
import com.juegos.radar.dominio.puertos.salida.FuenteOfertasPort;
import com.juegos.radar.dominio.puertos.salida.NotificadorOfertasPort;
import com.juegos.radar.dominio.puertos.salida.OfertaRepositorioPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HexagonalBeanConfiguration {

    @Bean
    public RadarOfertasService radarOfertasService(
            FuenteOfertasPort fuenteOfertasPort,
            OfertaRepositorioPort ofertaRepositorioPort,
            ComentarioOfertaRepositorioPort comentarioRepositorioPort,
            NotificadorOfertasPort notificadorPort) {
        return new RadarOfertasService(
                fuenteOfertasPort,
                ofertaRepositorioPort,
                comentarioRepositorioPort,
                notificadorPort
        );
    }
}

