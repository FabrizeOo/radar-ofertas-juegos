package com.juegos.radar.infraestructura.adaptadores.salida.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "comentarios_ofertas")
public class ComentarioOfertaDocumento {

    @Id
    private String id;

    @Indexed
    private String dealId;

    private String tituloJuego;
    private String autor;
    private String contenido;
    private int calificacion;

    @Indexed
    private Instant fechaCreacion;

    public ComentarioOfertaDocumento() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDealId() { return dealId; }
    public void setDealId(String dealId) { this.dealId = dealId; }

    public String getTituloJuego() { return tituloJuego; }
    public void setTituloJuego(String tituloJuego) { this.tituloJuego = tituloJuego; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public int getCalificacion() { return calificacion; }
    public void setCalificacion(int calificacion) { this.calificacion = calificacion; }

    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}

