package com.juegos.radar.infraestructura.adaptadores.salida.mongo;

import com.juegos.radar.dominio.modelo.ComentarioOferta;
import com.juegos.radar.dominio.puertos.salida.ComentarioOfertaRepositorioPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MongoComentarioOfertaAdapter implements ComentarioOfertaRepositorioPort {

    private final SpringComentarioOfertaRepository repository;

    public MongoComentarioOfertaAdapter(SpringComentarioOfertaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ComentarioOferta guardar(ComentarioOferta comentario) {
        ComentarioOfertaDocumento doc = new ComentarioOfertaDocumento();
        doc.setDealId(comentario.getDealId());
        doc.setTituloJuego(comentario.getTituloJuego());
        doc.setAutor(comentario.getAutor());
        doc.setContenido(comentario.getContenido());
        doc.setCalificacion(comentario.getCalificacion());
        doc.setFechaCreacion(comentario.getFechaCreacion());

        ComentarioOfertaDocumento guardado = repository.save(doc);
        return mapearADominio(guardado);
    }

    @Override
    public List<ComentarioOferta> listarRecientes(int limite) {
        return repository.findAllByOrderByFechaCreacionDesc(PageRequest.of(0, limite))
                .stream()
                .map(this::mapearADominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComentarioOferta> listarPorDealId(String dealId) {
        return repository.findByDealIdOrderByFechaCreacionDesc(dealId)
                .stream()
                .map(this::mapearADominio)
                .collect(Collectors.toList());
    }

    @Override
    public long contarTotal() {
        return repository.count();
    }

    private ComentarioOferta mapearADominio(ComentarioOfertaDocumento doc) {
        return new ComentarioOferta(
                doc.getId(),
                doc.getDealId(),
                doc.getTituloJuego(),
                doc.getAutor(),
                doc.getContenido(),
                doc.getCalificacion(),
                doc.getFechaCreacion()
        );
    }
}

