package com.juegos.radar.infraestructura.adaptadores.salida.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringOfertaRepository extends MongoRepository<OfertaDocumento, String> {
}

