package com.juegos.radar.infraestructura.adaptadores.entrada;

import com.juegos.radar.dominio.modelo.EstadisticaRadar;
import com.juegos.radar.dominio.modelo.OfertaJuego;
import com.juegos.radar.dominio.puertos.entrada.OfertaUseCase;
import com.juegos.radar.infraestructura.adaptadores.salida.SseNotificadorOfertasAdapter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ofertas")
@CrossOrigin(origins = "*")
public class OfertaController {

    private final OfertaUseCase ofertaUseCase;
    private final SseNotificadorOfertasAdapter sseNotificador;

    public OfertaController(OfertaUseCase ofertaUseCase, SseNotificadorOfertasAdapter sseNotificador) {
        this.ofertaUseCase = ofertaUseCase;
        this.sseNotificador = sseNotificador;
    }

    @GetMapping
    public List<OfertaJuego> buscarOfertas(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "TODAS") String tienda,
            @RequestParam(defaultValue = "0") double ahorroMin,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "24") int tamano) {
        return ofertaUseCase.buscarOfertas(q, tienda, ahorroMin, pagina, tamano);
    }

    @GetMapping("/detalle")
    public ResponseEntity<OfertaJuego> obtenerDetalle(@RequestParam String dealId) {
        return ofertaUseCase.obtenerPorDealId(dealId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sincronizar")
    public Map<String, Object> sincronizarMasivo(@RequestParam(defaultValue = "10") int paginas) {
        int importadas = ofertaUseCase.sincronizarMasivo(paginas);
        EstadisticaRadar estadisticas = ofertaUseCase.obtenerEstadisticas();
        return Map.of(
                "importadas", importadas,
                "estadisticas", estadisticas
        );
    }

    @PostMapping("/like")
    public ResponseEntity<OfertaJuego> darLike(@RequestParam String dealId) {
        return ofertaUseCase.darLike(dealId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/destacar")
    public ResponseEntity<OfertaJuego> destacarOferta(
            @RequestParam String dealId,
            @RequestBody AlertaOfertaRequest request) {
        return ofertaUseCase.actualizarDestacadoYAlerta(dealId, request.precioAlerta(), request.destacada())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/clasificar-lote")
    public Map<String, Object> clasificarMasivo() {
        return ofertaUseCase.clasificarOfertasMasivo();
    }

    @DeleteMapping("/purgar")
    public Map<String, Object> purgarBajoDescuento(@RequestParam(defaultValue = "25") double ahorroMenorA) {
        return ofertaUseCase.purgarOfertasBajoDescuento(ahorroMenorA);
    }

    @PostMapping("/reporte-bi")
    public Map<String, Object> generarReporteBi() {
        return ofertaUseCase.generarYGuardarReporteBi();
    }

    public record AlertaOfertaRequest(Double precioAlerta, Boolean destacada) {}

    @GetMapping("/estadisticas")
    public EstadisticaRadar obtenerEstadisticas() {
        return ofertaUseCase.obtenerEstadisticas();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTiempoReal() {
        return sseNotificador.suscribir();
    }
}

