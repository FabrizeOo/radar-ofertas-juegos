package com.juegos.radar.infraestructura.adaptadores.salida.mongo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SpringComentarioOfertaRepository extends MongoRepository<ComentarioOfertaDocumento, String> {

    List<ComentarioOfertaDocumento> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    List<ComentarioOfertaDocumento> findByDealIdOrderByFechaCreacionDesc(String dealId);
}

