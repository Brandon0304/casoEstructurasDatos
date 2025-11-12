package com.caso5;

import java.util.List;
import java.util.Optional;

/**
 * Punto de entrada de la aplicación: orquesta la simulación completa.
 */
public final class Aplicacion {

    private static final int TOTAL_EVALUACIONES = 5_000;

    private Aplicacion() {
        // Evita instanciación
    }

    public static void main(String[] args) {
        EvaluacionRepository repositorio = new EvaluacionRepository();
        CalificadorEvaluaciones calificador = new CalificadorEvaluaciones();
        ColaCorreccion colaCorreccion = new ColaCorreccion();
        SimuladorEvaluaciones simulador = new SimuladorEvaluaciones();
        GeneradorReporte generadorReporte = new GeneradorReporte();

        // 1. Generar todas las evaluaciones con sus claves
        simulador.generarEvaluaciones(TOTAL_EVALUACIONES, repositorio, calificador, colaCorreccion);

        // 2. Corregir respetando el orden FIFO
        procesarCorrecciones(colaCorreccion, calificador);

        // 3. Obtener el reporte ordenado por puntaje
        List<Evaluacion<?>> evaluacionesOrdenadas = generadorReporte.generarListadoOrdenado(repositorio.listarTodas());

        String reporte = generadorReporte.generarReporteEnTexto(evaluacionesOrdenadas);
        System.out.println("\n=== REPORTE DE EVALUACIONES ===");
        System.out.println(reporte);

        System.out.println("Total de evaluaciones generadas: " + evaluacionesOrdenadas.size());
        if (!evaluacionesOrdenadas.isEmpty()) {
            Evaluacion<?> mejor = evaluacionesOrdenadas.get(0);
            System.out.printf("Mejor puntaje: %.2f (%s)%n", mejor.getPuntaje(), mejor.getEstudiante());
        }
    }

    private static void procesarCorrecciones(ColaCorreccion colaCorreccion, CalificadorEvaluaciones calificador) {
        while (!colaCorreccion.estaVacia()) {
            Optional<Evaluacion<?>> posibleEvaluacion = colaCorreccion.tomarSiguiente();
            if (posibleEvaluacion.isEmpty()) {
                break;
            }
            Evaluacion<?> evaluacion = posibleEvaluacion.get();
            // El calificador detecta automáticamente el tipo y calcula el puntaje
            double puntaje = calificador.calificar(evaluacion);
            evaluacion.actualizarPuntaje(puntaje);
        }
    }
}

