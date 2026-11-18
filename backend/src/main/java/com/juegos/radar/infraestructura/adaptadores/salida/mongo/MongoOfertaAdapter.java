package com.juegos.radar.infraestructura.adaptadores.salida.mongo;

import com.juegos.radar.dominio.modelo.EstadisticaRadar;
import com.juegos.radar.dominio.modelo.OfertaJuego;
import com.juegos.radar.dominio.puertos.salida.OfertaRepositorioPort;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bson.Document;

@Component
public class MongoOfertaAdapter implements OfertaRepositorioPort {

    private final MongoTemplate mongoTemplate;
    private final SpringOfertaRepository repository;

    public MongoOfertaAdapter(MongoTemplate mongoTemplate, SpringOfertaRepository repository) {
        this.mongoTemplate = mongoTemplate;
        this.repository = repository;
    }

    @Override
    public void guardarLote(List<OfertaJuego> ofertas) {
        if (ofertas == null || ofertas.isEmpty()) {
            return;
        }
        BulkOperations bulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, OfertaDocumento.class);
        for (OfertaJuego o : ofertas) {
            Query query = Query.query(Criteria.where("_id").is(o.getDealId()));
            Update update = new Update()
                    .set("gameId", o.getGameId())
                    .set("titulo", o.getTitulo())
                    .set("storeId", o.getStoreId())
                    .set("tiendaNombre", o.getTiendaNombre())
                    .set("precioOferta", o.getPrecioOferta())
                    .set("precioNormal", o.getPrecioNormal())
                    .set("porcentajeAhorro", o.getPorcentajeAhorro())
                    .set("metacriticScore", o.getMetacriticScore())
                    .set("steamRatingText", o.getSteamRatingText())
                    .set("steamRatingPercent", o.getSteamRatingPercent())
                    .set("dealRating", o.getDealRating())
                    .set("urlImagen", o.getUrlImagen())
                    .set("fechaActualizacion", o.getFechaActualizacion())
                    .setOnInsert("likes", o.getLikes());
            bulk.upsert(query, update);
        }
        bulk.execute();
    }

    @Override
    public List<OfertaJuego> buscarConFiltros(String texto, String tienda, double ahorroMinimo, int pagina, int tamano) {
        List<Criteria> criterios = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            String escaped = Pattern.quote(texto.trim());
            criterios.add(Criteria.where("titulo").regex(escaped, "i"));
        }
        if (tienda != null && !tienda.isBlank() && !"TODAS".equalsIgnoreCase(tienda.trim())) {
            if ("DESTACADAS".equalsIgnoreCase(tienda.trim())) {
                criterios.add(Criteria.where("destacada").is(true));
            } else {
                String escapedStore = Pattern.quote(tienda.trim());
                criterios.add(Criteria.where("tiendaNombre").regex("^" + escapedStore + "$", "i"));
            }
        }
        if (ahorroMinimo > 0) {
            criterios.add(Criteria.where("porcentajeAhorro").gte(ahorroMinimo));
        }

        Query query = new Query();
        if (!criterios.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criterios.toArray(new Criteria[0])));
        }
        query.with(Sort.by(Sort.Order.desc("porcentajeAhorro"), Sort.Order.desc("dealRating")));
        query.skip((long) pagina * tamano);
        query.limit(tamano);

        return mongoTemplate.find(query, OfertaDocumento.class)
                .stream()
                .map(this::mapearADominio)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<OfertaJuego> buscarPorDealId(String dealId) {
        return repository.findById(dealId).map(this::mapearADominio);
    }

    @Override
    public Optional<OfertaJuego> incrementarLike(String dealId) {
        Query query = Query.query(Criteria.where("_id").is(dealId));
        Update update = new Update().inc("likes", 1);
        OfertaDocumento modificado = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                OfertaDocumento.class
        );
        return Optional.ofNullable(modificado).map(this::mapearADominio);
    }

    @Override
    public Optional<OfertaJuego> actualizarDestacadoYAlerta(String dealId, Double precioAlerta, Boolean destacada) {
        Query query = Query.query(Criteria.where("_id").is(dealId));
        Update update = new Update();
        if (precioAlerta != null) {
            if (precioAlerta <= 0) {
                update.unset("precioAlerta");
            } else {
                update.set("precioAlerta", precioAlerta);
            }
        }
        if (destacada != null) {
            update.set("destacada", destacada);
        }
        OfertaDocumento modificado = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                OfertaDocumento.class
        );
        return Optional.ofNullable(modificado).map(this::mapearADominio);
    }

    @Override
    public Map<String, Long> clasificarOfertasMasivo() {
        mongoTemplate.updateMulti(new Query(), new Update().set("nivelOferta", "REBAJA_MEDIA"), OfertaDocumento.class);

        mongoTemplate.updateMulti(
                Query.query(Criteria.where("porcentajeAhorro").gte(60).lt(80)),
                new Update().set("nivelOferta", "GRAN_DESCUENTO"),
                OfertaDocumento.class
        );
        mongoTemplate.updateMulti(
                Query.query(Criteria.where("porcentajeAhorro").gte(80)),
                new Update().set("nivelOferta", "IMPERDIBLE"),
                OfertaDocumento.class
        );

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.group("nivelOferta").count().as("total")
        );
        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "ofertas_juegos", Document.class);
        Map<String, Long> conteos = new LinkedHashMap<>();
        for (Document d : results.getMappedResults()) {
            String k = d.getString("_id") != null ? d.getString("_id") : "REBAJA_MEDIA";
            Number n = d.get("total", Number.class);
            conteos.put(k, n != null ? n.longValue() : 0L);
        }
        return conteos;
    }

    @Override
    public long purgarPorAhorroMenorA(double ahorroMenorA) {
        Query query = Query.query(
                Criteria.where("porcentajeAhorro").lt(ahorroMenorA)
                        .and("destacada").ne(true)
        );
        return mongoTemplate.remove(query, OfertaDocumento.class).getDeletedCount();
    }

    @Override
    public Map<String, Object> generarYGuardarReporteBi(long totalComentarios) {
        Map<String, Long> porNivel = clasificarOfertasMasivo();
        long destacadas = mongoTemplate.count(Query.query(Criteria.where("destacada").is(true)), OfertaDocumento.class);
        long conAlerta = mongoTemplate.count(Query.query(Criteria.where("precioAlerta").gt(0)), OfertaDocumento.class);

        EstadisticaRadar stats = calcularEstadisticas(totalComentarios);

        Aggregation aggRatings = Aggregation.newAggregation(
                Aggregation.group("calificacion").count().as("total")
        );
        AggregationResults<Document> resRatings = mongoTemplate.aggregate(aggRatings, "comentarios_ofertas", Document.class);
        Map<String, Long> distribucionCalificaciones = new LinkedHashMap<>();
        for (Document d : resRatings.getMappedResults()) {
            Object idObj = d.get("_id");
            String key = idObj != null ? idObj + "_estrellas" : "5_estrellas";
            Number n = d.get("total", Number.class);
            distribucionCalificaciones.put(key, n != null ? n.longValue() : 0L);
        }

        List<Map<String, Object>> tiendasMap = stats.porTienda().stream()
                .map(t -> Map.<String, Object>of(
                        "tiendaNombre", t.tiendaNombre(),
                        "cantidadOfertas", t.cantidadOfertas(),
                        "ahorroPromedio", t.ahorroPromedio(),
                        "precioMinimo", t.precioMinimo()
                ))
                .collect(Collectors.toList());

        Document reporteDoc = new Document("_id", "ultimo_reporte_ofertas")
                .append("fechaGeneracion", Instant.now().toString())
                .append("totalOfertas", stats.totalOfertas())
                .append("totalComentarios", totalComentarios)
                .append("ahorroPromedioGlobal", stats.ahorroPromedio())
                .append("ofertasDestacadas", destacadas)
                .append("alertasPrecioActivas", conAlerta)
                .append("distribucionNiveles", porNivel)
                .append("distribucionCalificaciones", distribucionCalificaciones)
                .append("resumenTiendas", tiendasMap);

        mongoTemplate.save(reporteDoc, "reportes_bi");

        Map<String, Object> out = new LinkedHashMap<>(reporteDoc);
        out.put("coleccionGuardada", "reportes_bi");
        return out;
    }

    @Override
    public long contarTotal() {
        return repository.count();
    }

    @Override
    public EstadisticaRadar calcularEstadisticas(long totalComentarios) {
        long totalOfertas = repository.count();

        Aggregation aggTiendas = Aggregation.newAggregation(
                Aggregation.group("tiendaNombre")
                        .count().as("cantidadOfertas")
                        .avg("porcentajeAhorro").as("ahorroPromedio")
                        .min("precioOferta").as("precioMinimo"),
                Aggregation.sort(Sort.Direction.DESC, "cantidadOfertas"),
                Aggregation.limit(8)
        );

        AggregationResults<TiendaAggResult> resultados = mongoTemplate.aggregate(
                aggTiendas, "ofertas_juegos", TiendaAggResult.class
        );

        List<EstadisticaRadar.ResumenTienda> porTienda = resultados.getMappedResults().stream()
                .map(r -> new EstadisticaRadar.ResumenTienda(
                        r.get_id() != null ? r.get_id() : "Otra",
                        r.getCantidadOfertas(),
                        Math.round(r.getAhorroPromedio() * 10.0) / 10.0,
                        Math.round(r.getPrecioMinimo() * 100.0) / 100.0
                ))
                .collect(Collectors.toList());

        Aggregation aggGlobal = Aggregation.newAggregation(
                Aggregation.group().avg("porcentajeAhorro").as("ahorroPromedio")
        );
        AggregationResults<GlobalAggResult> globalRes = mongoTemplate.aggregate(
                aggGlobal, "ofertas_juegos", GlobalAggResult.class
        );
        GlobalAggResult unique = globalRes.getUniqueMappedResult();
        double ahorroGlobal = unique != null ? Math.round(unique.getAhorroPromedio() * 10.0) / 10.0 : 0.0;

        return new EstadisticaRadar(totalOfertas, totalComentarios, ahorroGlobal, porTienda);
    }

    private OfertaJuego mapearADominio(OfertaDocumento doc) {
        OfertaJuego o = new OfertaJuego(
                doc.getDealId(),
                doc.getGameId(),
                doc.getTitulo(),
                doc.getStoreId(),
                doc.getTiendaNombre(),
                doc.getPrecioOferta(),
                doc.getPrecioNormal(),
                doc.getPorcentajeAhorro(),
                doc.getMetacriticScore(),
                doc.getSteamRatingText(),
                doc.getSteamRatingPercent(),
                doc.getDealRating(),
                doc.getUrlImagen(),
                doc.getFechaActualizacion(),
                doc.getLikes()
        );
        o.setPrecioAlerta(doc.getPrecioAlerta());
        o.setDestacada(doc.isDestacada());
        o.setNivelOferta(doc.getNivelOferta());
        return o;
    }

    private static class TiendaAggResult {
        private String _id;
        private long cantidadOfertas;
        private double ahorroPromedio;
        private double precioMinimo;

        public String get_id() { return _id; }
        public long getCantidadOfertas() { return cantidadOfertas; }
        public double getAhorroPromedio() { return ahorroPromedio; }
        public double getPrecioMinimo() { return precioMinimo; }
    }

    private static class GlobalAggResult {
        private double ahorroPromedio;

        public double getAhorroPromedio() { return ahorroPromedio; }
    }
}

