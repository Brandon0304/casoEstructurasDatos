package com.caso5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Repositorio en memoria para almacenar y consultar evaluaciones.
 */
public class EvaluacionRepository {

    // Almacena las evaluaciones con acceso O(1) conservando orden de inserción
    private final Map<UUID, Evaluacion<?>> mapaEvaluaciones = new LinkedHashMap<>();
    // Registra los identificadores para impedir duplicados
    private final Set<UUID> registroIdentificadores = new HashSet<>();

    /**
     * Registra una nueva evaluación. Ignora duplicados.
     *
     * @param evaluacion evaluación a registrar.
     * @return {@code true} si la evaluación fue agregada; {@code false} si el identificador ya existía.
     */
    public boolean registrar(Evaluacion<?> evaluacion) {
        UUID identificador = evaluacion.getId();
        if (registroIdentificadores.contains(identificador)) {
            return false;
        }
        registroIdentificadores.add(identificador);
        mapaEvaluaciones.put(identificador, evaluacion);
        return true;
    }

    /**
     * Busca una evaluación por su identificador.
     *
     * @param identificador identificador único.
     * @return evaluación encontrada o {@code null} si no existe.
     */
    public Evaluacion<?> buscar(UUID identificador) {
        return mapaEvaluaciones.get(identificador);
    }

    /**
     * Devuelve una copia inmutable de todas las evaluaciones registradas.
     *
     * @return colección inmutable de evaluaciones.
     */
    public Collection<Evaluacion<?>> listarTodas() {
        return Collections.unmodifiableCollection(mapaEvaluaciones.values());
    }

    /**
     * Crea una lista mutable con todas las evaluaciones registradas.
     *
     * @return lista con las evaluaciones almacenadas.
     */
    public List<Evaluacion<?>> copiarComoLista() {
        return new ArrayList<>(mapaEvaluaciones.values());
    }
}

