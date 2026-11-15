package com.juegos.radar.infraestructura.adaptadores.salida;

import com.juegos.radar.dominio.modelo.OfertaJuego;
import com.juegos.radar.dominio.puertos.salida.FuenteOfertasPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class CheapSharkAdapter implements FuenteOfertasPort {

    private static final Logger log = LoggerFactory.getLogger(CheapSharkAdapter.class);
    private static final String BASE_URL = "https://www.cheapshark.com/api/1.0";
    private static final String USER_AGENT = "RadarOfertasJuegos/1.0 (universidad-nosql@example.com)";

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, String> cacheTiendas = new ConcurrentHashMap<>();

    @Override
    public List<OfertaJuego> descargarOfertas(int pagina, int tamanoPagina) {
        cargarTiendasSiVacio();

        String url = String.format("%s/deals?pageNumber=%d&pageSize=%d", BASE_URL, pagina, tamanoPagina);
        try {
            ResponseEntity<CheapSharkDeal[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    crearHttpEntity(),
                    CheapSharkDeal[].class
            );
            CheapSharkDeal[] body = response.getBody();
            if (body == null || body.length == 0) {
                return Collections.emptyList();
            }
            return Arrays.stream(body)
                    .map(this::mapearADominio)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.warn("Error consultando CheapShark página {}: {}", pagina, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private void cargarTiendasSiVacio() {
        if (!cacheTiendas.isEmpty()) {
            return;
        }
        try {
            ResponseEntity<CheapSharkStore[]> response = restTemplate.exchange(
                    BASE_URL + "/stores",
                    HttpMethod.GET,
                    crearHttpEntity(),
                    CheapSharkStore[].class
            );
            CheapSharkStore[] stores = response.getBody();
            if (stores != null) {
                for (CheapSharkStore s : stores) {
                    if (s.storeID() != null && s.storeName() != null) {
                        cacheTiendas.put(s.storeID(), s.storeName());
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("No se pudo cargar catálogo de tiendas de CheapShark: {}", ex.getMessage());
        }
    }

    private HttpEntity<Void> crearHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        return new HttpEntity<>(headers);
    }

    private OfertaJuego mapearADominio(CheapSharkDeal d) {
        String nombreTienda = cacheTiendas.getOrDefault(d.storeID(), "Tienda #" + d.storeID());
        Instant actualizado = d.lastChange() != null ? Instant.ofEpochSecond(d.lastChange()) : Instant.now();

        return new OfertaJuego(
                d.dealID(),
                d.gameID(),
                d.title(),
                d.storeID(),
                nombreTienda,
                parseDouble(d.salePrice()),
                parseDouble(d.normalPrice()),
                parseDouble(d.savings()),
                parseInt(d.metacriticScore()),
                d.steamRatingText(),
                parseInt(d.steamRatingPercent()),
                parseDouble(d.dealRating()),
                d.thumb(),
                actualizado,
                0L
        );
    }

    private double parseDouble(String valor) {
        if (valor == null || valor.isBlank()) return 0.0;
        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseInt(String valor) {
        if (valor == null || valor.isBlank()) return 0;
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private record CheapSharkDeal(
            String dealID,
            String gameID,
            String title,
            String storeID,
            String salePrice,
            String normalPrice,
            String savings,
            String metacriticScore,
            String steamRatingText,
            String steamRatingPercent,
            String dealRating,
            String thumb,
            Long lastChange
    ) {}

    private record CheapSharkStore(
            String storeID,
            String storeName
    ) {}
}

