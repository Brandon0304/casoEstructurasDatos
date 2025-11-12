package com.caso5;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa una evaluación registrada en el sistema.
 *
 * @param <T> tipo de la respuesta entregada por el estudiante.
 */
public class Evaluacion<T> {

    // Identificador único de la evaluación
    private final UUID id;
    // Nombre del estudiante que presenta la evaluación
    private final String estudiante;
    // Tipo de evaluación (OM, VF o ABIERTA)
    private final TipoEvaluacion tipo;
    // Respuesta entregada, puede ser lista, booleanos o texto
    private final T respuesta;
    // Posición en la que llegó la evaluación, útil para desempatar
    private final long ordenLlegada;
    // Puntaje calculado tras la corrección
    private double puntaje;

    public Evaluacion(UUID id, String estudiante, TipoEvaluacion tipo, T respuesta, long ordenLlegada) {
        this.id = Objects.requireNonNull(id);
        this.estudiante = Objects.requireNonNull(estudiante);
        this.tipo = Objects.requireNonNull(tipo);
        this.respuesta = Objects.requireNonNull(respuesta);
        this.ordenLlegada = ordenLlegada;
        this.puntaje = 0.0;
    }

    public UUID getId() {
        return id;
    }

    public String getEstudiante() {
        return estudiante;
    }

    public TipoEvaluacion getTipo() {
        return tipo;
    }

    public T getRespuesta() {
        return respuesta;
    }

    public double getPuntaje() {
        return puntaje;
    }

    public long getOrdenLlegada() {
        return ordenLlegada;
    }

    /**
     * Registra el puntaje obtenido tras la corrección.
     *
     * @param puntaje valor entre 0 y 100.
     */
    public void actualizarPuntaje(double puntaje) {
        this.puntaje = puntaje;
    }
}