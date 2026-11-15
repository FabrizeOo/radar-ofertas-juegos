package com.juegos.radar.infraestructura.configuracion;

import com.juegos.radar.dominio.puertos.entrada.OfertaUseCase;
import com.juegos.radar.dominio.puertos.salida.OfertaRepositorioPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InicializadorOfertas implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializadorOfertas.class);

    private final OfertaUseCase ofertaUseCase;
    private final OfertaRepositorioPort repositorioPort;

    @Value("${app.seed-on-startup:true}")
    private boolean seedOnStartup;

    @Value("${app.seed-pages:10}")
    private int seedPages;

    public InicializadorOfertas(OfertaUseCase ofertaUseCase, OfertaRepositorioPort repositorioPort) {
        this.ofertaUseCase = ofertaUseCase;
        this.repositorioPort = repositorioPort;
    }

    @Override
    public void run(String... args) {
        if (!seedOnStartup) {
            return;
        }
        try {
            long existentes = repositorioPort.contarTotal();
            if (existentes == 0) {
                log.info("MongoDB vacío. Importando {} páginas JSON desde CheapShark...", seedPages);
                int importadas = ofertaUseCase.sincronizarMasivo(seedPages);
                log.info("Carga inicial completada: {} ofertas guardadas en MongoDB.", importadas);
            } else {
                log.info("MongoDB ya contiene {} ofertas de videojuegos.", existentes);
            }
        } catch (Exception ex) {
            log.warn("No se pudo ejecutar la carga inicial automática (verifica tu MONGODB_URI): {}", ex.getMessage());
        }
    }
}

