package com.caso5;

import java.util.LinkedList;
import java.util.Optional;

/**
 * Cola FIFO para el proceso de corrección.
 */
public class ColaCorreccion {

    // La LinkedList se usa como cola FIFO
    private final LinkedList<Evaluacion<?>> colaEvaluaciones = new LinkedList<>();

    /**
     * Inserta una evaluación al final de la cola.
     */
    public void encolar(Evaluacion<?> evaluacion) {
        colaEvaluaciones.addLast(evaluacion);
    }

    /**
     * Obtiene la siguiente evaluación por corregir respetando FIFO.
     */
    public Optional<Evaluacion<?>> tomarSiguiente() {
        if (colaEvaluaciones.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(colaEvaluaciones.removeFirst());
    }

    public int pendientes() {
        return colaEvaluaciones.size();
    }

    public boolean estaVacia() {
        return colaEvaluaciones.isEmpty();
    }
}
