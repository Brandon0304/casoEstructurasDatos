package com.caso5;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa la respuesta esperada para una evaluación concreta.
 *
 * @param <T> tipo coincidente con la respuesta del estudiante.
 */
public class ClaveEvaluacion<T> {

    // Identificador de la evaluación a la que pertenece la clave
    private final UUID identificadorEvaluacion;
    // Respuesta correcta que se utilizará para calificar
    private final T respuestaEsperada;

    public ClaveEvaluacion(UUID identificadorEvaluacion, T respuestaEsperada) {
        this.identificadorEvaluacion = Objects.requireNonNull(identificadorEvaluacion, "El identificador no puede ser nulo");
        this.respuestaEsperada = Objects.requireNonNull(respuestaEsperada, "La respuesta esperada no puede ser nula");
    }

    public UUID getIdentificadorEvaluacion() {
        return identificadorEvaluacion;
    }

    public T getRespuestaEsperada() {
        return respuestaEsperada;
    }
}

