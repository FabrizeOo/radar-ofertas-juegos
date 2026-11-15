package com.juegos.radar.infraestructura.adaptadores.salida.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "ofertas_juegos")
public class OfertaDocumento {

    @Id
    private String dealId;

    private String gameId;

    @Indexed
    private String titulo;

    private String storeId;

    @Indexed
    private String tiendaNombre;

    private double precioOferta;
    private double precioNormal;

    @Indexed
    private double porcentajeAhorro;

    private int metacriticScore;
    private String steamRatingText;
    private int steamRatingPercent;
    private double dealRating;
    private String urlImagen;
    private Instant fechaActualizacion;
    private long likes;

    public OfertaDocumento() {
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
}

