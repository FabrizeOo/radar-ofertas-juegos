package com.juegos.radar.aplicacion.servicio;

import com.juegos.radar.dominio.modelo.ComentarioOferta;
import com.juegos.radar.dominio.modelo.EstadisticaRadar;
import com.juegos.radar.dominio.modelo.OfertaJuego;
import com.juegos.radar.dominio.puertos.salida.ComentarioOfertaRepositorioPort;
import com.juegos.radar.dominio.puertos.salida.FuenteOfertasPort;
import com.juegos.radar.dominio.puertos.salida.NotificadorOfertasPort;
import com.juegos.radar.dominio.puertos.salida.OfertaRepositorioPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RadarOfertasServiceTest {

    @Test
    void sincronizarYComentarEmiteEventosYGuardaEnPuertos() {
        List<OfertaJuego> guardadas = new ArrayList<>();
        List<String> eventos = new ArrayList<>();

        FuenteOfertasPort fuente = (pagina, tamano) -> pagina == 0
                ? List.of(new OfertaJuego("deal-1", "g1", "Witcher 3", "1", "Steam", 9.99, 39.99, 75.0, 93, "Overwhelmingly Positive", 96, 9.8, "", Instant.now(), 0))
                : List.of();

        OfertaRepositorioPort repoOfertas = new OfertaRepositorioPort() {
            @Override public void guardarLote(List<OfertaJuego> ofertas) { guardadas.addAll(ofertas); }
            @Override public List<OfertaJuego> buscarConFiltros(String t, String ti, double a, int p, int ta) { return guardadas; }
            @Override public Optional<OfertaJuego> buscarPorDealId(String id) { return guardadas.stream().filter(o -> o.getDealId().equals(id)).findFirst(); }
            @Override public Optional<OfertaJuego> incrementarLike(String id) { return Optional.empty(); }
            @Override public long contarTotal() { return guardadas.size(); }
            @Override public EstadisticaRadar calcularEstadisticas(long c) { return new EstadisticaRadar(guardadas.size(), c, 75.0, List.of()); }
        };

        ComentarioOfertaRepositorioPort repoComentarios = new ComentarioOfertaRepositorioPort() {
            @Override public ComentarioOferta guardar(ComentarioOferta c) { c.setId("c-1"); return c; }
            @Override public List<ComentarioOferta> listarRecientes(int l) { return List.of(); }
            @Override public List<ComentarioOferta> listarPorDealId(String d) { return List.of(); }
            @Override public long contarTotal() { return 1; }
        };

        NotificadorOfertasPort notificador = (tipo, payload) -> eventos.add(tipo);

        RadarOfertasService service = new RadarOfertasService(fuente, repoOfertas, repoComentarios, notificador);

        int importadas = service.sincronizarMasivo(3);
        assertEquals(1, importadas);
        assertEquals(1, guardadas.size());

        ComentarioOferta comentario = service.publicarComentario("deal-1", "Fabri", "Excelente oferta!", 5);
        assertEquals("Witcher 3", comentario.getTituloJuego());
        assertEquals(List.of("sincronizacion", "comentario"), eventos);
    }
}

