package com.juegos.radar.infraestructura.adaptadores.salida;

import com.juegos.radar.dominio.puertos.salida.NotificadorOfertasPort;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseNotificadorOfertasAdapter implements NotificadorOfertasPort {

    private final List<SseEmitter> emisores = new CopyOnWriteArrayList<>();

    public SseEmitter suscribir() {
        SseEmitter emitter = new SseEmitter(0L);
        emisores.add(emitter);
        emitter.onCompletion(() -> emisores.remove(emitter));
        emitter.onTimeout(() -> emisores.remove(emitter));
        emitter.onError(e -> emisores.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("conectado").data("Stream de ofertas en tiempo real activo"));
        } catch (IOException e) {
            emisores.remove(emitter);
        }
        return emitter;
    }

    @Override
    public void publicarEvento(String tipoEvento, Object payload) {
        for (SseEmitter emitter : emisores) {
            try {
                emitter.send(SseEmitter.event().name(tipoEvento).data(payload));
            } catch (Exception e) {
                emitter.complete();
                emisores.remove(emitter);
            }
        }
    }
}

