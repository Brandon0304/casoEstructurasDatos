package com.caso5;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Calcula el puntaje de cada evaluación utilizando su clave de respuestas.
 */
public class CalificadorEvaluaciones {

    // Relaciona el ID de la evaluación con su clave correcta
    private final Map<UUID, ClaveEvaluacion<?>> clavesPorEvaluacion = new HashMap<>();

    /**
     * Guarda la clave correspondiente a una evaluación.
     */
    public void registrarClave(ClaveEvaluacion<?> clave) {
        clavesPorEvaluacion.put(clave.getIdentificadorEvaluacion(), clave);
    }

    @SuppressWarnings("unchecked")
    /**
     * Calcula el puntaje usando la clave almacenada y el tipo de evaluación.
     */
    public double calificar(Evaluacion<?> evaluacion) {
        ClaveEvaluacion<?> clave = clavesPorEvaluacion.get(evaluacion.getId());
        if (clave == null) {
            throw new IllegalStateException("No se encontró la clave para " + evaluacion.getId());
        }

        return switch (evaluacion.getTipo()) {
            case OM -> calificarOpcionMultiple(
                    (List<Integer>) evaluacion.getRespuesta(),
                    (List<Integer>) clave.getRespuestaEsperada()
            );
            case VF -> calificarVerdaderoFalso(
                    (List<Boolean>) evaluacion.getRespuesta(),
                    (List<Boolean>) clave.getRespuestaEsperada()
            );
            case ABIERTA -> calificarPreguntaAbierta(
                    (String) evaluacion.getRespuesta(),
                    (String) clave.getRespuestaEsperada()
            );
        };
    }

    private double calificarOpcionMultiple(List<Integer> respuestas, List<Integer> clave) {
        int total = Math.min(respuestas.size(), clave.size());
        int aciertos = 0;
        for (int i = 0; i < total; i++) {
            if (respuestas.get(i).equals(clave.get(i))) {
                aciertos++;
            }
        }
        return total == 0 ? 0.0 : (aciertos * 100.0) / total;
    }

    private double calificarVerdaderoFalso(List<Boolean> respuestas, List<Boolean> clave) {
        int total = Math.min(respuestas.size(), clave.size());
        int aciertos = 0;
        for (int i = 0; i < total; i++) {
            if (respuestas.get(i).equals(clave.get(i))) {
                aciertos++;
            }
        }
        return total == 0 ? 0.0 : (aciertos * 100.0) / total;
    }

    // Para preguntas abiertas se hace una comparación sencilla del contenido
    private double calificarPreguntaAbierta(String respuesta, String clave) {
        if (respuesta == null || respuesta.isBlank()) {
            return 0.0;
        }
        if (respuesta.equalsIgnoreCase(clave)) {
            return 100.0;
        }
        return respuesta.toLowerCase().contains(clave.toLowerCase()) ? 70.0 : 40.0;
    }
}
