package com.juegos.radar.aplicacion.servicio;

import com.juegos.radar.dominio.modelo.ComentarioOferta;
import com.juegos.radar.dominio.modelo.EstadisticaRadar;
import com.juegos.radar.dominio.modelo.OfertaJuego;
import com.juegos.radar.dominio.puertos.entrada.ComentarioOfertaUseCase;
import com.juegos.radar.dominio.puertos.entrada.OfertaUseCase;
import com.juegos.radar.dominio.puertos.salida.ComentarioOfertaRepositorioPort;
import com.juegos.radar.dominio.puertos.salida.FuenteOfertasPort;
import com.juegos.radar.dominio.puertos.salida.NotificadorOfertasPort;
import com.juegos.radar.dominio.puertos.salida.OfertaRepositorioPort;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RadarOfertasService implements OfertaUseCase, ComentarioOfertaUseCase {

    private static final int TAMANO_PAGINA_EXTERNA = 60;

    private final FuenteOfertasPort fuenteOfertasPort;
    private final OfertaRepositorioPort ofertaRepositorioPort;
    private final ComentarioOfertaRepositorioPort comentarioRepositorioPort;
    private final NotificadorOfertasPort notificadorPort;

    public RadarOfertasService(
            FuenteOfertasPort fuenteOfertasPort,
            OfertaRepositorioPort ofertaRepositorioPort,
            ComentarioOfertaRepositorioPort comentarioRepositorioPort,
            NotificadorOfertasPort notificadorPort) {
        this.fuenteOfertasPort = fuenteOfertasPort;
        this.ofertaRepositorioPort = ofertaRepositorioPort;
        this.comentarioRepositorioPort = comentarioRepositorioPort;
        this.notificadorPort = notificadorPort;
    }

    @Override
    public List<OfertaJuego> buscarOfertas(String texto, String tienda, double ahorroMinimo, int pagina, int tamano) {
        int paginaSegura = Math.max(0, pagina);
        int tamanoSeguro = Math.min(Math.max(1, tamano), 100);
        return ofertaRepositorioPort.buscarConFiltros(texto, tienda, ahorroMinimo, paginaSegura, tamanoSeguro);
    }

    @Override
    public Optional<OfertaJuego> obtenerPorDealId(String dealId) {
        return ofertaRepositorioPort.buscarPorDealId(dealId);
    }

    @Override
    public int sincronizarMasivo(int paginas) {
        int paginasSeguras = Math.min(Math.max(1, paginas), 30);
        int totalImportado = 0;

        for (int pagina = 0; pagina < paginasSeguras; pagina++) {
            List<OfertaJuego> lote = fuenteOfertasPort.descargarOfertas(pagina, TAMANO_PAGINA_EXTERNA);
            if (lote.isEmpty()) {
                break;
            }
            ofertaRepositorioPort.guardarLote(lote);
            totalImportado += lote.size();
        }

        if (totalImportado > 0) {
            EstadisticaRadar stats = obtenerEstadisticas();
            notificadorPort.publicarEvento("sincronizacion", Map.of(
                    "importadas", totalImportado,
                    "estadisticas", stats
            ));
        }

        return totalImportado;
    }

    @Override
    public Optional<OfertaJuego> darLike(String dealId) {
        Optional<OfertaJuego> actualizada = ofertaRepositorioPort.incrementarLike(dealId);
        actualizada.ifPresent(oferta -> notificadorPort.publicarEvento("like", oferta));
        return actualizada;
    }

    @Override
    public EstadisticaRadar obtenerEstadisticas() {
        long totalComentarios = comentarioRepositorioPort.contarTotal();
        return ofertaRepositorioPort.calcularEstadisticas(totalComentarios);
    }

    @Override
    public List<ComentarioOferta> listarRecientes(int limite) {
        int limiteSeguro = Math.min(Math.max(1, limite), 100);
        return comentarioRepositorioPort.listarRecientes(limiteSeguro);
    }

    @Override
    public List<ComentarioOferta> listarPorOferta(String dealId) {
        return comentarioRepositorioPort.listarPorDealId(dealId);
    }

    @Override
    public ComentarioOferta publicarComentario(String dealId, String autor, String contenido, int calificacion) {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío");
        }

        String tituloJuego = ofertaRepositorioPort.buscarPorDealId(dealId)
                .map(OfertaJuego::getTitulo)
                .orElse("Comunidad General");

        String autorLimpio = (autor == null || autor.isBlank()) ? "Jugador Anónimo" : autor.trim();
        int calificacionSegura = Math.min(Math.max(1, calificacion), 5);

        ComentarioOferta nuevo = new ComentarioOferta(
                null,
                dealId,
                tituloJuego,
                autorLimpio,
                contenido.trim(),
                calificacionSegura,
                Instant.now()
        );

        ComentarioOferta guardado = comentarioRepositorioPort.guardar(nuevo);
        notificadorPort.publicarEvento("comentario", guardado);
        return guardado;
    }
}

