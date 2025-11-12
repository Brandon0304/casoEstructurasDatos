package com.caso5;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Genera evaluaciones simuladas y sus claves de corrección.
 */
public class SimuladorEvaluaciones {

    // Catálogo reducido de estudiantes ficticios
    private static final List<String> NOMBRES_ESTUDIANTES = List.of(
            "Ana Torres", "Luis García", "Sofía Hernández", "Carlos Díaz", "María López",
            "Julián Pérez", "Camila Rojas", "Pedro Castillo", "Valentina Arce", "Juan Suárez"
    );

    // Respuestas modelo para preguntas abiertas
    private static final String[] RESPUESTAS_ABIERTAS = new String[]{
            "La fotosintesis transforma energia solar en quimica en los cloroplastos",
            "Inglaterra tenia carbon puertos y capital para iniciar la revolucion industrial",
            "La tabla periodica organiza elementos por numero atomico y propiedades",
            "Un algoritmo es una serie ordenada de pasos para resolver un problema",
            "La biodiversidad mantiene el equilibrio de los ecosistemas"
    };

    // Generador aleatorio utilizado en la simulación
    private final Random generador = new Random();
    // Contador que representa el orden de llegada de cada evaluación
    private long contadorRegistro = 0L;

    public void generarEvaluaciones(
            int cantidad,
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        // Se generan evaluaciones hasta alcanzar la cantidad solicitada
        for (int i = 0; i < cantidad; i++) {
            TipoEvaluacion tipo = seleccionarTipo();
            UUID identificador = UUID.randomUUID();
            String estudiante = seleccionarNombreAleatorio();

            switch (tipo) {
                case OM -> crearOpcionMultiple(identificador, estudiante, repositorio, calificador, colaCorreccion);
                case VF -> crearVerdaderoFalso(identificador, estudiante, repositorio, calificador, colaCorreccion);
                case ABIERTA -> crearPreguntaAbierta(identificador, estudiante, repositorio, calificador, colaCorreccion);
            }
        }
    }

    private TipoEvaluacion seleccionarTipo() {
        int indice = generador.nextInt(TipoEvaluacion.values().length);
        return TipoEvaluacion.values()[indice];
    }

    private String seleccionarNombreAleatorio() {
        return NOMBRES_ESTUDIANTES.get(generador.nextInt(NOMBRES_ESTUDIANTES.size()));
    }

    // Crea una evaluación de opción múltiple con cinco preguntas
    private void crearOpcionMultiple(
            UUID identificador,
            String estudiante,
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        List<Integer> clave = new ArrayList<>();
        List<Integer> respuestas = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int correcta = generador.nextInt(4);
            int elegida = generador.nextInt(4);
            clave.add(correcta);
            respuestas.add(elegida);
        }
        registrarEvaluacion(identificador, estudiante, TipoEvaluacion.OM, respuestas, clave, repositorio, calificador, colaCorreccion);
    }

    private void crearVerdaderoFalso(
            UUID identificador,
            String estudiante,
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        List<Boolean> clave = new ArrayList<>();
        List<Boolean> respuestas = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            boolean correcta = generador.nextBoolean();
            boolean elegida = generador.nextBoolean();
            clave.add(correcta);
            respuestas.add(elegida);
        }
        registrarEvaluacion(identificador, estudiante, TipoEvaluacion.VF, respuestas, clave, repositorio, calificador, colaCorreccion);
    }

    // Crea una evaluación abierta reutilizando respuestas modelo
    private void crearPreguntaAbierta(
            UUID identificador,
            String estudiante,
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        int indice = generador.nextInt(RESPUESTAS_ABIERTAS.length);
        String clave = RESPUESTAS_ABIERTAS[indice];
        String respuestaEstudiante = generarRespuestaLibre(clave);
        registrarEvaluacion(identificador, estudiante, TipoEvaluacion.ABIERTA, respuestaEstudiante, clave, repositorio, calificador, colaCorreccion);
    }

    private String generarRespuestaLibre(String base) {
        if (generador.nextBoolean()) {
            return base;
        }
        return base + " con ejemplos simples";
    }

    /**
     * Registra la evaluación en todas las estructuras si el ID no está repetido.
     */
    private <T> void registrarEvaluacion(
            UUID identificador,
            String estudiante,
            TipoEvaluacion tipo,
            T respuestaEstudiante,
            T respuestaEsperada,
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        Evaluacion<T> evaluacion = new Evaluacion<>(
                identificador,
                estudiante,
                tipo,
                respuestaEstudiante,
                contadorRegistro++
        );
        if (repositorio.registrar(evaluacion)) {
            calificador.registrarClave(new ClaveEvaluacion<>(identificador, respuestaEsperada));
            colaCorreccion.encolar(evaluacion);
        }
    }
}
