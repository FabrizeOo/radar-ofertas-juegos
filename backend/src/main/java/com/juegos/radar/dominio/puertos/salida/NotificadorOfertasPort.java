package com.juegos.radar.dominio.puertos.salida;

public interface NotificadorOfertasPort {

    void publicarEvento(String tipoEvento, Object payload);
}

