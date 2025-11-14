package com.caso5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

/**
 * Punto de entrada de la aplicación: orquesta la simulación completa.
 */
public final class Aplicacion {

    private static final int TOTAL_EVALUACIONES = 5_000;
    private static final Scanner scanner = new Scanner(System.in);

    private Aplicacion() {
        // Evita instanciación
    }

    public static void main(String[] args) {
        EvaluacionRepository repositorio = new EvaluacionRepository();
        CalificadorEvaluaciones calificador = new CalificadorEvaluaciones();
        ColaCorreccion colaCorreccion = new ColaCorreccion();
        SimuladorEvaluaciones simulador = new SimuladorEvaluaciones();
        GeneradorReporte generadorReporte = new GeneradorReporte();

        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            int opcion = leerOpcion();
            
            switch (opcion) {
                case 1 -> agregarEvaluacionManual(repositorio, calificador, colaCorreccion);
                case 2 -> agregarNotaManual(repositorio);
                case 3 -> verCantidadEvaluaciones(repositorio, generadorReporte);
                case 4 -> verTodasEvaluaciones(repositorio, generadorReporte);
                case 5 -> generarEvaluacionesAutomaticas(simulador, repositorio, calificador, colaCorreccion);
                case 6 -> procesarTodasCorrecciones(colaCorreccion, calificador);
                case 7 -> continuar = false;
                default -> System.out.println("Opción no válida. Por favor, seleccione una opción del 1 al 7.");
            }
        }
        
        System.out.println("\n¡Gracias por usar el sistema de evaluaciones!");
        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println("\n=== MENÚ PRINCIPAL ===");
        System.out.println("1. Añadir evaluación manualmente");
        System.out.println("2. Agregar/Editar nota manualmente");
        System.out.println("3. Ver cantidad específica de evaluaciones");
        System.out.println("4. Ver todas las evaluaciones");
        System.out.println("5. Generar evaluaciones automáticamente (5000)");
        System.out.println("6. Procesar correcciones pendientes");
        System.out.println("7. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private static int leerOpcion() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void agregarEvaluacionManual(
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        System.out.println("\n=== AÑADIR EVALUACIÓN MANUAL ===");
        System.out.println("Tipos disponibles:");
        System.out.println("1. OM (Opción Múltiple)");
        System.out.println("2. VF (Verdadero/Falso)");
        System.out.println("3. ABIERTA");
        System.out.print("Seleccione el tipo (1-3): ");
        
        int tipoOpcion = leerOpcion();
        TipoEvaluacion tipo = null;
        
        switch (tipoOpcion) {
            case 1 -> tipo = TipoEvaluacion.OM;
            case 2 -> tipo = TipoEvaluacion.VF;
            case 3 -> tipo = TipoEvaluacion.ABIERTA;
            default -> {
                System.out.println("Tipo no válido.");
                return;
            }
        }
        
        System.out.print("Nombre del estudiante: ");
        String estudiante = scanner.nextLine().trim();
        if (estudiante.isEmpty()) {
            System.out.println("El nombre no puede estar vacío.");
            return;
        }
        
        UUID id = UUID.randomUUID();
        long ordenLlegada = System.currentTimeMillis();
        
        try {
            Evaluacion<?> evaluacionCreada = null;
            switch (tipo) {
                case OM -> evaluacionCreada = agregarOpcionMultiple(id, estudiante, ordenLlegada, repositorio, calificador, colaCorreccion);
                case VF -> evaluacionCreada = agregarVerdaderoFalso(id, estudiante, ordenLlegada, repositorio, calificador, colaCorreccion);
                case ABIERTA -> evaluacionCreada = agregarPreguntaAbierta(id, estudiante, ordenLlegada, repositorio, calificador, colaCorreccion);
            }
            
            if (evaluacionCreada != null) {
                // SIEMPRE preguntar si quiere ingresar el puntaje manualmente
                System.out.println("\n=== ASIGNAR PUNTAJE ===");
                System.out.print("¿Desea ingresar el puntaje manualmente? (s=si, n=no/calcular automático): ");
                String respuesta = scanner.nextLine().trim().toLowerCase();
                
                double puntajeFinal = 0.0;
                
                if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("y") || respuesta.equals("yes") || respuesta.equals("1")) {
                    // Ingresar puntaje manualmente
                    boolean puntajeValido = false;
                    while (!puntajeValido) {
                        System.out.print("Ingrese el puntaje (0-100): ");
                        try {
                            String input = scanner.nextLine().trim();
                            if (input.isEmpty()) {
                                System.out.println("Debe ingresar un valor. Intente de nuevo.");
                                continue;
                            }
                            puntajeFinal = Double.parseDouble(input);
                            if (puntajeFinal < 0 || puntajeFinal > 100) {
                                System.out.println("El puntaje debe estar entre 0 y 100. Intente de nuevo.");
                                continue;
                            }
                            puntajeValido = true;
                        } catch (NumberFormatException e) {
                            System.out.println("Por favor, ingrese un número válido (0-100).");
                        }
                    }
                    evaluacionCreada.actualizarPuntaje(puntajeFinal);
                    System.out.printf("✓ Puntaje manual asignado: %.2f%n", puntajeFinal);
                } else {
                    // Calcular puntaje automáticamente
                    puntajeFinal = calificador.calificar(evaluacionCreada);
                    evaluacionCreada.actualizarPuntaje(puntajeFinal);
                    System.out.printf("✓ Puntaje calculado automáticamente: %.2f%n", puntajeFinal);
                }
                System.out.println("✓ Evaluación agregada exitosamente.");
            }
        } catch (Exception e) {
            System.out.println("✗ Error al agregar la evaluación: " + e.getMessage());
        }
    }

    private static Evaluacion<?> agregarOpcionMultiple(
            UUID id,
            String estudiante,
            long ordenLlegada,
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        System.out.println("\nIngrese 5 respuestas (0-3 para cada pregunta):");
        List<Integer> respuestas = new ArrayList<>();
        List<Integer> clave = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            System.out.print("Pregunta " + i + " - Respuesta del estudiante (0-3): ");
            int respuesta = Integer.parseInt(scanner.nextLine().trim());
            if (respuesta < 0 || respuesta > 3) {
                throw new IllegalArgumentException("La respuesta debe estar entre 0 y 3");
            }
            respuestas.add(respuesta);
            
            System.out.print("Pregunta " + i + " - Respuesta correcta (0-3): ");
            int correcta = Integer.parseInt(scanner.nextLine().trim());
            if (correcta < 0 || correcta > 3) {
                throw new IllegalArgumentException("La respuesta correcta debe estar entre 0 y 3");
            }
            clave.add(correcta);
        }
        
        Evaluacion<List<Integer>> evaluacion = new Evaluacion<>(id, estudiante, TipoEvaluacion.OM, respuestas, ordenLlegada);
        if (repositorio.registrar(evaluacion)) {
            calificador.registrarClave(new ClaveEvaluacion<>(id, clave));
            // No encolar, se procesará inmediatamente
            return evaluacion;
        } else {
            throw new IllegalStateException("El ID de la evaluación ya existe");
        }
    }

    private static Evaluacion<?> agregarVerdaderoFalso(
            UUID id,
            String estudiante,
            long ordenLlegada,
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        System.out.println("\nIngrese 6 respuestas (true/false para cada pregunta):");
        List<Boolean> respuestas = new ArrayList<>();
        List<Boolean> clave = new ArrayList<>();
        
        for (int i = 1; i <= 6; i++) {
            System.out.print("Pregunta " + i + " - Respuesta del estudiante (true/false): ");
            String respuestaStr = scanner.nextLine().trim().toLowerCase();
            boolean respuesta = respuestaStr.equals("true") || respuestaStr.equals("t") || respuestaStr.equals("verdadero") || respuestaStr.equals("v");
            respuestas.add(respuesta);
            
            System.out.print("Pregunta " + i + " - Respuesta correcta (true/false): ");
            String correctaStr = scanner.nextLine().trim().toLowerCase();
            boolean correcta = correctaStr.equals("true") || correctaStr.equals("t") || correctaStr.equals("verdadero") || correctaStr.equals("v");
            clave.add(correcta);
        }
        
        Evaluacion<List<Boolean>> evaluacion = new Evaluacion<>(id, estudiante, TipoEvaluacion.VF, respuestas, ordenLlegada);
        if (repositorio.registrar(evaluacion)) {
            calificador.registrarClave(new ClaveEvaluacion<>(id, clave));
            // No encolar, se procesará inmediatamente
            return evaluacion;
        } else {
            throw new IllegalStateException("El ID de la evaluación ya existe");
        }
    }

    private static Evaluacion<?> agregarPreguntaAbierta(
            UUID id,
            String estudiante,
            long ordenLlegada,
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        System.out.print("Respuesta del estudiante: ");
        String respuesta = scanner.nextLine().trim();
        if (respuesta.isEmpty()) {
            throw new IllegalArgumentException("La respuesta no puede estar vacía");
        }
        
        System.out.print("Respuesta correcta (clave): ");
        String clave = scanner.nextLine().trim();
        if (clave.isEmpty()) {
            throw new IllegalArgumentException("La clave no puede estar vacía");
        }
        
        Evaluacion<String> evaluacion = new Evaluacion<>(id, estudiante, TipoEvaluacion.ABIERTA, respuesta, ordenLlegada);
        if (repositorio.registrar(evaluacion)) {
            calificador.registrarClave(new ClaveEvaluacion<>(id, clave));
            // No encolar, se procesará inmediatamente
            return evaluacion;
        } else {
            throw new IllegalStateException("El ID de la evaluación ya existe");
        }
    }

    private static void agregarNotaManual(EvaluacionRepository repositorio) {
        System.out.println("\n=== AGREGAR/EDITAR NOTA MANUALMENTE ===");
        
        // Mostrar lista de estudiantes disponibles
        Collection<Evaluacion<?>> todas = repositorio.listarTodas();
        if (todas.isEmpty()) {
            System.out.println("No hay evaluaciones registradas. Primero debe crear una evaluación.");
            return;
        }
        
        System.out.println("\nEvaluaciones disponibles:");
        List<Evaluacion<?>> listaEvaluaciones = new ArrayList<>(todas);
        for (int i = 0; i < listaEvaluaciones.size(); i++) {
            Evaluacion<?> eval = listaEvaluaciones.get(i);
            System.out.printf("%d. %s - %s (Nota actual: %.2f)%n", 
                i + 1, eval.getEstudiante(), eval.getTipo(), eval.getPuntaje());
        }
        
        System.out.print("\nSeleccione el número de la evaluación (1-" + listaEvaluaciones.size() + "): ");
        try {
            int indice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (indice < 0 || indice >= listaEvaluaciones.size()) {
                System.out.println("Número inválido.");
                return;
            }
            
            Evaluacion<?> evaluacion = listaEvaluaciones.get(indice);
            System.out.printf("\nEvaluación seleccionada: %s - %s%n", evaluacion.getEstudiante(), evaluacion.getTipo());
            System.out.printf("Nota actual: %.2f%n", evaluacion.getPuntaje());
            
            System.out.print("Ingrese la nueva nota (0-100): ");
            double nuevaNota = Double.parseDouble(scanner.nextLine().trim());
            
            if (nuevaNota < 0 || nuevaNota > 100) {
                System.out.println("La nota debe estar entre 0 y 100.");
                return;
            }
            
            evaluacion.actualizarPuntaje(nuevaNota);
            System.out.printf("✓ Nota actualizada exitosamente: %.2f%n", nuevaNota);
            
        } catch (NumberFormatException e) {
            System.out.println("Por favor, ingrese un número válido.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void verCantidadEvaluaciones(
            EvaluacionRepository repositorio,
            GeneradorReporte generadorReporte
    ) {
        System.out.print("\n¿Cuántas evaluaciones desea ver? ");
        try {
            int cantidad = Integer.parseInt(scanner.nextLine().trim());
            if (cantidad < 1) {
                System.out.println("La cantidad debe ser mayor a 0.");
                return;
            }
            
            List<Evaluacion<?>> todas = generadorReporte.generarListadoOrdenado(repositorio.listarTodas());
            int total = todas.size();
            
            if (total == 0) {
                System.out.println("No hay evaluaciones registradas.");
                return;
            }
            
            int cantidadAMostrar = Math.min(cantidad, total);
            List<Evaluacion<?>> evaluacionesAMostrar = todas.subList(0, cantidadAMostrar);
            
            String reporte = generadorReporte.generarReporteEnTexto(evaluacionesAMostrar);
            System.out.println("\n=== EVALUACIONES (mostrando " + cantidadAMostrar + " de " + total + ") ===");
            System.out.println(reporte);
        } catch (NumberFormatException e) {
            System.out.println("Por favor, ingrese un número válido.");
        }
    }

    private static void verTodasEvaluaciones(
            EvaluacionRepository repositorio,
            GeneradorReporte generadorReporte
    ) {
        List<Evaluacion<?>> todas = generadorReporte.generarListadoOrdenado(repositorio.listarTodas());
        
        if (todas.isEmpty()) {
            System.out.println("\nNo hay evaluaciones registradas.");
            return;
        }
        
        String reporte = generadorReporte.generarReporteEnTexto(todas);
        System.out.println("\n=== TODAS LAS EVALUACIONES (Total: " + todas.size() + ") ===");
        System.out.println(reporte);
        
        if (!todas.isEmpty()) {
            Evaluacion<?> mejor = todas.get(0);
            System.out.printf("Mejor puntaje: %.2f (%s)%n", mejor.getPuntaje(), mejor.getEstudiante());
        }
    }

    private static void generarEvaluacionesAutomaticas(
            SimuladorEvaluaciones simulador,
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        System.out.print("\n¿Cuántas evaluaciones desea generar? (presione Enter para 5000): ");
        String input = scanner.nextLine().trim();
        int cantidad = input.isEmpty() ? TOTAL_EVALUACIONES : Integer.parseInt(input);
        
        if (cantidad < 1) {
            System.out.println("La cantidad debe ser mayor a 0.");
            return;
        }
        
        System.out.println("Generando " + cantidad + " evaluaciones...");
        simulador.generarEvaluaciones(cantidad, repositorio, calificador, colaCorreccion);
        System.out.println("✓ " + cantidad + " evaluaciones generadas exitosamente.");
        
        System.out.println("Procesando correcciones automáticamente...");
        int procesadas = procesarCorreccionesSilencioso(colaCorreccion, calificador);
        System.out.println("✓ " + procesadas + " evaluaciones procesadas y calificadas.");
    }
    
    private static int procesarCorreccionesSilencioso(ColaCorreccion colaCorreccion, CalificadorEvaluaciones calificador) {
        int procesadas = 0;
        while (!colaCorreccion.estaVacia()) {
            Optional<Evaluacion<?>> posibleEvaluacion = colaCorreccion.tomarSiguiente();
            if (posibleEvaluacion.isEmpty()) {
                break;
            }
            Evaluacion<?> evaluacion = posibleEvaluacion.get();
            double puntaje = calificador.calificar(evaluacion);
            evaluacion.actualizarPuntaje(puntaje);
            procesadas++;
        }
        return procesadas;
    }

    private static void procesarTodasCorrecciones(
            ColaCorreccion colaCorreccion,
            CalificadorEvaluaciones calificador
    ) {
        System.out.println("\nProcesando correcciones...");
        int procesadas = 0;
        
        while (!colaCorreccion.estaVacia()) {
            Optional<Evaluacion<?>> posibleEvaluacion = colaCorreccion.tomarSiguiente();
            if (posibleEvaluacion.isEmpty()) {
                break;
            }
            Evaluacion<?> evaluacion = posibleEvaluacion.get();
            double puntaje = calificador.calificar(evaluacion);
            evaluacion.actualizarPuntaje(puntaje);
            procesadas++;
        }
        
        System.out.println("✓ " + procesadas + " evaluaciones procesadas.");
    }
}

