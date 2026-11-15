package com.juegos.radar.infraestructura.adaptadores.entrada;

import com.juegos.radar.dominio.modelo.ComentarioOferta;
import com.juegos.radar.dominio.puertos.entrada.ComentarioOfertaUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
@CrossOrigin(origins = "*")
public class ComentarioOfertaController {

    private final ComentarioOfertaUseCase comentarioUseCase;

    public ComentarioOfertaController(ComentarioOfertaUseCase comentarioUseCase) {
        this.comentarioUseCase = comentarioUseCase;
    }

    @GetMapping
    public List<ComentarioOferta> listarRecientes(@RequestParam(defaultValue = "30") int limite) {
        return comentarioUseCase.listarRecientes(limite);
    }

    @GetMapping("/oferta")
    public List<ComentarioOferta> listarPorOferta(@RequestParam String dealId) {
        return comentarioUseCase.listarPorOferta(dealId);
    }

    @PostMapping
    public ResponseEntity<ComentarioOferta> publicarComentario(@RequestBody NuevoComentarioRequest request) {
        ComentarioOferta creado = comentarioUseCase.publicarComentario(
                request.dealId(),
                request.autor(),
                request.contenido(),
                request.calificacion()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    public record NuevoComentarioRequest(
            String dealId,
            String autor,
            String contenido,
            int calificacion
    ) {}
}

