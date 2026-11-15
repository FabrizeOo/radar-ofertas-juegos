package com.juegos.radar.dominio.modelo;

import java.time.Instant;

/**
 * Comentario u opinión de un usuario sobre una oferta de videojuego.
 */
public class ComentarioOferta {

    private String id;
    private String dealId;
    private String tituloJuego;
    private String autor;
    private String contenido;
    private int calificacion;
    private Instant fechaCreacion;

    public ComentarioOferta() {
    }

    public ComentarioOferta(
            String id,
            String dealId,
            String tituloJuego,
            String autor,
            String contenido,
            int calificacion,
            Instant fechaCreacion) {
        this.id = id;
        this.dealId = dealId;
        this.tituloJuego = tituloJuego;
        this.autor = autor;
        this.contenido = contenido;
        this.calificacion = calificacion;
        this.fechaCreacion = fechaCreacion;
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

