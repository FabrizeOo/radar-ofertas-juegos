package com.juegos.radar.dominio.modelo;

import java.time.Instant;

/**
 * Entidad de dominio que representa una oferta de videojuego almacenada en el sistema.
 */
public class OfertaJuego {

    private String dealId;
    private String gameId;
    private String titulo;
    private String storeId;
    private String tiendaNombre;
    private double precioOferta;
    private double precioNormal;
    private double porcentajeAhorro;
    private int metacriticScore;
    private String steamRatingText;
    private int steamRatingPercent;
    private double dealRating;
    private String urlImagen;
    private Instant fechaActualizacion;
    private long likes;
    private Double precioAlerta;
    private boolean destacada;
    private String nivelOferta;

    public OfertaJuego() {
    }

    public OfertaJuego(
            String dealId,
            String gameId,
            String titulo,
            String storeId,
            String tiendaNombre,
            double precioOferta,
            double precioNormal,
            double porcentajeAhorro,
            int metacriticScore,
            String steamRatingText,
            int steamRatingPercent,
            double dealRating,
            String urlImagen,
            Instant fechaActualizacion,
            long likes) {
        this.dealId = dealId;
        this.gameId = gameId;
        this.titulo = titulo;
        this.storeId = storeId;
        this.tiendaNombre = tiendaNombre;
        this.precioOferta = precioOferta;
        this.precioNormal = precioNormal;
        this.porcentajeAhorro = porcentajeAhorro;
        this.metacriticScore = metacriticScore;
        this.steamRatingText = steamRatingText;
        this.steamRatingPercent = steamRatingPercent;
        this.dealRating = dealRating;
        this.urlImagen = urlImagen;
        this.fechaActualizacion = fechaActualizacion;
        this.likes = likes;
    }

    public String getDealId() { return dealId; }
    public void setDealId(String dealId) { this.dealId = dealId; }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getTiendaNombre() { return tiendaNombre; }
    public void setTiendaNombre(String tiendaNombre) { this.tiendaNombre = tiendaNombre; }

    public double getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(double precioOferta) { this.precioOferta = precioOferta; }

    public double getPrecioNormal() { return precioNormal; }
    public void setPrecioNormal(double precioNormal) { this.precioNormal = precioNormal; }

    public double getPorcentajeAhorro() { return porcentajeAhorro; }
    public void setPorcentajeAhorro(double porcentajeAhorro) { this.porcentajeAhorro = porcentajeAhorro; }

    public int getMetacriticScore() { return metacriticScore; }
    public void setMetacriticScore(int metacriticScore) { this.metacriticScore = metacriticScore; }

    public String getSteamRatingText() { return steamRatingText; }
    public void setSteamRatingText(String steamRatingText) { this.steamRatingText = steamRatingText; }

    public int getSteamRatingPercent() { return steamRatingPercent; }
    public void setSteamRatingPercent(int steamRatingPercent) { this.steamRatingPercent = steamRatingPercent; }

    public double getDealRating() { return dealRating; }
    public void setDealRating(double dealRating) { this.dealRating = dealRating; }

    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }

    public Instant getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Instant fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public long getLikes() { return likes; }
    public void setLikes(long likes) { this.likes = likes; }

    public Double getPrecioAlerta() { return precioAlerta; }
    public void setPrecioAlerta(Double precioAlerta) { this.precioAlerta = precioAlerta; }

    public boolean isDestacada() { return destacada; }
    public void setDestacada(boolean destacada) { this.destacada = destacada; }

    public String getNivelOferta() { return nivelOferta; }
    public void setNivelOferta(String nivelOferta) { this.nivelOferta = nivelOferta; }
}

