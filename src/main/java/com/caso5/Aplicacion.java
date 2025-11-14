package com.caso5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

/**
 * Punto de entrada de la aplicación: orquesta la simulación completa.
 * 
 * Esta clase maneja un sistema interactivo para gestionar evaluaciones de estudiantes.
 * Permite crear evaluaciones manualmente, generar evaluaciones automáticas,
 * calificar respuestas y generar reportes ordenados por puntaje.
 */
public final class Aplicacion {

    // Cantidad por defecto de evaluaciones a generar automáticamente
    private static final int TOTAL_EVALUACIONES = 5_000;
    
    // Scanner para leer entrada del usuario desde la consola
    private static final Scanner scanner = new Scanner(System.in);

    private Aplicacion() {
        // Evita instanciación - esta clase solo tiene métodos estáticos
    }

    /**
     * Método principal que inicia la aplicación.
     * Crea todas las estructuras necesarias y muestra el menú interactivo.
     * 
     * @param args argumentos de línea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        // Inicializar todas las estructuras de datos necesarias:
        // - Repositorio: almacena todas las evaluaciones
        // - Calificador: calcula los puntajes comparando respuestas con claves
        // - ColaCorreccion: cola FIFO para procesar evaluaciones pendientes
        // - Simulador: genera evaluaciones automáticas con datos aleatorios
        // - GeneradorReporte: crea reportes ordenados por puntaje
        EvaluacionRepository repositorio = new EvaluacionRepository();
        CalificadorEvaluaciones calificador = new CalificadorEvaluaciones();
        ColaCorreccion colaCorreccion = new ColaCorreccion();
        SimuladorEvaluaciones simulador = new SimuladorEvaluaciones();
        GeneradorReporte generadorReporte = new GeneradorReporte();

        // Bucle principal del menú interactivo
        // Se ejecuta hasta que el usuario elija salir (opción 7)
        boolean continuar = true;
        while (continuar) {
            mostrarMenu(); // Muestra las opciones disponibles
            int opcion = leerOpcion(); // Lee la opción seleccionada por el usuario
            
            // Ejecutar la acción correspondiente a la opción seleccionada
            switch (opcion) {
                case 1 -> agregarEvaluacionManual(repositorio, calificador, colaCorreccion);
                case 2 -> agregarNotaManual(repositorio);
                case 3 -> verCantidadEvaluaciones(repositorio, generadorReporte);
                case 4 -> verTodasEvaluaciones(repositorio, generadorReporte);
                case 5 -> generarEvaluacionesAutomaticas(simulador, repositorio, calificador, colaCorreccion);
                case 6 -> procesarTodasCorrecciones(colaCorreccion, calificador);
                case 7 -> continuar = false; // Salir del programa
                default -> System.out.println("Opción no válida. Por favor, seleccione una opción del 1 al 7.");
            }
        }
        
        System.out.println("\n¡Gracias por usar el sistema de evaluaciones!");
        scanner.close(); // Cerrar el scanner para liberar recursos
    }

    /**
     * Muestra el menú principal con todas las opciones disponibles.
     * Este método se llama en cada iteración del bucle principal.
     */
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

    /**
     * Lee la opción seleccionada por el usuario desde la consola.
     * 
     * @return el número de opción seleccionado, o -1 si la entrada no es válida
     */
    private static int leerOpcion() {
        try {
            // Leer la línea, eliminar espacios en blanco y convertir a entero
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            // Si el usuario ingresa algo que no es un número, retornar -1
            // Esto activará el caso "default" en el switch
            return -1;
        }
    }

    /**
     * Permite al usuario agregar una evaluación manualmente.
     * El usuario puede elegir el tipo de evaluación (OM, VF o ABIERTA),
     * ingresar los datos del estudiante y las respuestas.
     * Al final, puede ingresar el puntaje manualmente o calcularlo automáticamente.
     * 
     * @param repositorio almacén de todas las evaluaciones
     * @param calificador calcula puntajes comparando respuestas con claves
     * @param colaCorreccion cola FIFO para evaluaciones pendientes (no se usa en modo manual)
     */
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
        
        // Leer el tipo de evaluación seleccionado
        int tipoOpcion = leerOpcion();
        TipoEvaluacion tipo = null;
        
        // Convertir la opción numérica al enum correspondiente
        switch (tipoOpcion) {
            case 1 -> tipo = TipoEvaluacion.OM;
            case 2 -> tipo = TipoEvaluacion.VF;
            case 3 -> tipo = TipoEvaluacion.ABIERTA;
            default -> {
                System.out.println("Tipo no válido.");
                return; // Salir si el tipo no es válido
            }
        }
        
        // Solicitar el nombre del estudiante
        System.out.print("Nombre del estudiante: ");
        String estudiante = scanner.nextLine().trim();
        if (estudiante.isEmpty()) {
            System.out.println("El nombre no puede estar vacío.");
            return;
        }
        
        // Generar un identificador único para la evaluación
        UUID id = UUID.randomUUID();
        // Usar el tiempo actual como orden de llegada (para desempates)
        long ordenLlegada = System.currentTimeMillis();
        
        try {
            // Crear la evaluación según el tipo seleccionado
            Evaluacion<?> evaluacionCreada = null;
            switch (tipo) {
                case OM -> evaluacionCreada = agregarOpcionMultiple(id, estudiante, ordenLlegada, repositorio, calificador, colaCorreccion);
                case VF -> evaluacionCreada = agregarVerdaderoFalso(id, estudiante, ordenLlegada, repositorio, calificador, colaCorreccion);
                case ABIERTA -> evaluacionCreada = agregarPreguntaAbierta(id, estudiante, ordenLlegada, repositorio, calificador, colaCorreccion);
            }
            
            // Si la evaluación se creó correctamente, asignar el puntaje
            if (evaluacionCreada != null) {
                // Preguntar al usuario si quiere ingresar el puntaje manualmente
                System.out.println("\n=== ASIGNAR PUNTAJE ===");
                System.out.print("¿Desea ingresar el puntaje manualmente? (s=si, n=no/calcular automático): ");
                String respuesta = scanner.nextLine().trim().toLowerCase();
                
                double puntajeFinal = 0.0;
                
                // Si el usuario quiere ingresar el puntaje manualmente
                if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("y") || respuesta.equals("yes") || respuesta.equals("1")) {
                    // Bucle para validar que el puntaje ingresado sea válido (0-100)
                    boolean puntajeValido = false;
                    while (!puntajeValido) {
                        System.out.print("Ingrese el puntaje (0-100): ");
                        try {
                            String input = scanner.nextLine().trim();
                            if (input.isEmpty()) {
                                System.out.println("Debe ingresar un valor. Intente de nuevo.");
                                continue; // Volver a pedir el puntaje
                            }
                            puntajeFinal = Double.parseDouble(input);
                            // Validar que esté en el rango permitido
                            if (puntajeFinal < 0 || puntajeFinal > 100) {
                                System.out.println("El puntaje debe estar entre 0 y 100. Intente de nuevo.");
                                continue; // Volver a pedir el puntaje
                            }
                            puntajeValido = true; // Puntaje válido, salir del bucle
                        } catch (NumberFormatException e) {
                            System.out.println("Por favor, ingrese un número válido (0-100).");
                        }
                    }
                    // Asignar el puntaje manual ingresado
                    evaluacionCreada.actualizarPuntaje(puntajeFinal);
                    System.out.printf("✓ Puntaje manual asignado: %.2f%n", puntajeFinal);
                } else {
                    // Calcular el puntaje automáticamente comparando respuestas con la clave
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

    /**
     * Crea una evaluación de tipo Opción Múltiple (OM).
     * Solicita 5 respuestas del estudiante y sus respuestas correctas correspondientes.
     * Cada respuesta es un número del 0 al 3 (representando las opciones A, B, C, D).
     * 
     * @param id identificador único de la evaluación
     * @param estudiante nombre del estudiante
     * @param ordenLlegada orden de llegada (para desempates)
     * @param repositorio donde se guardará la evaluación
     * @param calificador donde se registrará la clave de respuestas correctas
     * @param colaCorreccion no se usa en modo manual
     * @return la evaluación creada
     */
    private static Evaluacion<?> agregarOpcionMultiple(
            UUID id,
            String estudiante,
            long ordenLlegada,
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        System.out.println("\nIngrese 5 respuestas (0-3 para cada pregunta):");
        List<Integer> respuestas = new ArrayList<>(); // Respuestas del estudiante
        List<Integer> clave = new ArrayList<>(); // Respuestas correctas (clave de corrección)
        
        // Solicitar 5 preguntas de opción múltiple
        for (int i = 1; i <= 5; i++) {
            // Leer la respuesta del estudiante (0=A, 1=B, 2=C, 3=D)
            System.out.print("Pregunta " + i + " - Respuesta del estudiante (0-3): ");
            int respuesta = Integer.parseInt(scanner.nextLine().trim());
            if (respuesta < 0 || respuesta > 3) {
                throw new IllegalArgumentException("La respuesta debe estar entre 0 y 3");
            }
            respuestas.add(respuesta);
            
            // Leer la respuesta correcta para esta pregunta
            System.out.print("Pregunta " + i + " - Respuesta correcta (0-3): ");
            int correcta = Integer.parseInt(scanner.nextLine().trim());
            if (correcta < 0 || correcta > 3) {
                throw new IllegalArgumentException("La respuesta correcta debe estar entre 0 y 3");
            }
            clave.add(correcta);
        }
        
        // Crear la evaluación con las respuestas del estudiante
        Evaluacion<List<Integer>> evaluacion = new Evaluacion<>(id, estudiante, TipoEvaluacion.OM, respuestas, ordenLlegada);
        if (repositorio.registrar(evaluacion)) {
            // Registrar la clave de respuestas correctas para poder calificar después
            calificador.registrarClave(new ClaveEvaluacion<>(id, clave));
            // No se encola porque se procesará inmediatamente en agregarEvaluacionManual
            return evaluacion;
        } else {
            throw new IllegalStateException("El ID de la evaluación ya existe");
        }
    }

    /**
     * Crea una evaluación de tipo Verdadero/Falso (VF).
     * Solicita 6 respuestas del estudiante y sus respuestas correctas correspondientes.
     * Acepta varias formas de ingresar true/false (true, t, verdadero, v, etc.).
     * 
     * @param id identificador único de la evaluación
     * @param estudiante nombre del estudiante
     * @param ordenLlegada orden de llegada (para desempates)
     * @param repositorio donde se guardará la evaluación
     * @param calificador donde se registrará la clave de respuestas correctas
     * @param colaCorreccion no se usa en modo manual
     * @return la evaluación creada
     */
    private static Evaluacion<?> agregarVerdaderoFalso(
            UUID id,
            String estudiante,
            long ordenLlegada,
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        System.out.println("\nIngrese 6 respuestas (true/false para cada pregunta):");
        List<Boolean> respuestas = new ArrayList<>(); // Respuestas del estudiante
        List<Boolean> clave = new ArrayList<>(); // Respuestas correctas (clave de corrección)
        
        // Solicitar 6 preguntas de verdadero/falso
        for (int i = 1; i <= 6; i++) {
            // Leer la respuesta del estudiante (acepta: true, t, verdadero, v)
            System.out.print("Pregunta " + i + " - Respuesta del estudiante (true/false): ");
            String respuestaStr = scanner.nextLine().trim().toLowerCase();
            // Convertir texto a booleano (acepta múltiples formatos)
            boolean respuesta = respuestaStr.equals("true") || respuestaStr.equals("t") || respuestaStr.equals("verdadero") || respuestaStr.equals("v");
            respuestas.add(respuesta);
            
            // Leer la respuesta correcta para esta pregunta
            System.out.print("Pregunta " + i + " - Respuesta correcta (true/false): ");
            String correctaStr = scanner.nextLine().trim().toLowerCase();
            // Convertir texto a booleano
            boolean correcta = correctaStr.equals("true") || correctaStr.equals("t") || correctaStr.equals("verdadero") || correctaStr.equals("v");
            clave.add(correcta);
        }
        
        // Crear la evaluación con las respuestas del estudiante
        Evaluacion<List<Boolean>> evaluacion = new Evaluacion<>(id, estudiante, TipoEvaluacion.VF, respuestas, ordenLlegada);
        if (repositorio.registrar(evaluacion)) {
            // Registrar la clave de respuestas correctas para poder calificar después
            calificador.registrarClave(new ClaveEvaluacion<>(id, clave));
            // No se encola porque se procesará inmediatamente en agregarEvaluacionManual
            return evaluacion;
        } else {
            throw new IllegalStateException("El ID de la evaluación ya existe");
        }
    }

    /**
     * Crea una evaluación de tipo Pregunta Abierta (ABIERTA).
     * Solicita la respuesta del estudiante (texto libre) y la respuesta correcta (clave).
     * El calificador compara el texto para asignar el puntaje.
     * 
     * @param id identificador único de la evaluación
     * @param estudiante nombre del estudiante
     * @param ordenLlegada orden de llegada (para desempates)
     * @param repositorio donde se guardará la evaluación
     * @param calificador donde se registrará la clave de respuesta correcta
     * @param colaCorreccion no se usa en modo manual
     * @return la evaluación creada
     */
    private static Evaluacion<?> agregarPreguntaAbierta(
            UUID id,
            String estudiante,
            long ordenLlegada,
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        // Leer la respuesta del estudiante (texto libre)
        System.out.print("Respuesta del estudiante: ");
        String respuesta = scanner.nextLine().trim();
        if (respuesta.isEmpty()) {
            throw new IllegalArgumentException("La respuesta no puede estar vacía");
        }
        
        // Leer la respuesta correcta (clave de corrección)
        System.out.print("Respuesta correcta (clave): ");
        String clave = scanner.nextLine().trim();
        if (clave.isEmpty()) {
            throw new IllegalArgumentException("La clave no puede estar vacía");
        }
        
        // Crear la evaluación con la respuesta del estudiante
        Evaluacion<String> evaluacion = new Evaluacion<>(id, estudiante, TipoEvaluacion.ABIERTA, respuesta, ordenLlegada);
        if (repositorio.registrar(evaluacion)) {
            // Registrar la clave de respuesta correcta para poder calificar después
            calificador.registrarClave(new ClaveEvaluacion<>(id, clave));
            // No se encola porque se procesará inmediatamente en agregarEvaluacionManual
            return evaluacion;
        } else {
            throw new IllegalStateException("El ID de la evaluación ya existe");
        }
    }

    /**
     * Permite editar manualmente el puntaje de una evaluación existente.
     * Muestra todas las evaluaciones disponibles y permite seleccionar una para cambiar su nota.
     * 
     * @param repositorio almacén de todas las evaluaciones
     */
    private static void agregarNotaManual(EvaluacionRepository repositorio) {
        System.out.println("\n=== AGREGAR/EDITAR NOTA MANUALMENTE ===");
        
        // Obtener todas las evaluaciones registradas
        Collection<Evaluacion<?>> todas = repositorio.listarTodas();
        if (todas.isEmpty()) {
            System.out.println("No hay evaluaciones registradas. Primero debe crear una evaluación.");
            return;
        }
        
        // Mostrar lista numerada de todas las evaluaciones disponibles
        System.out.println("\nEvaluaciones disponibles:");
        List<Evaluacion<?>> listaEvaluaciones = new ArrayList<>(todas);
        for (int i = 0; i < listaEvaluaciones.size(); i++) {
            Evaluacion<?> eval = listaEvaluaciones.get(i);
            // Mostrar: número, estudiante, tipo y nota actual
            System.out.printf("%d. %s - %s (Nota actual: %.2f)%n", 
                i + 1, eval.getEstudiante(), eval.getTipo(), eval.getPuntaje());
        }
        
        // Solicitar al usuario que seleccione una evaluación
        System.out.print("\nSeleccione el número de la evaluación (1-" + listaEvaluaciones.size() + "): ");
        try {
            // Leer el número seleccionado y convertir a índice (restar 1 porque la lista empieza en 0)
            int indice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (indice < 0 || indice >= listaEvaluaciones.size()) {
                System.out.println("Número inválido.");
                return;
            }
            
            // Obtener la evaluación seleccionada
            Evaluacion<?> evaluacion = listaEvaluaciones.get(indice);
            System.out.printf("\nEvaluación seleccionada: %s - %s%n", evaluacion.getEstudiante(), evaluacion.getTipo());
            System.out.printf("Nota actual: %.2f%n", evaluacion.getPuntaje());
            
            // Solicitar la nueva nota
            System.out.print("Ingrese la nueva nota (0-100): ");
            double nuevaNota = Double.parseDouble(scanner.nextLine().trim());
            
            // Validar que la nota esté en el rango permitido
            if (nuevaNota < 0 || nuevaNota > 100) {
                System.out.println("La nota debe estar entre 0 y 100.");
                return;
            }
            
            // Actualizar el puntaje de la evaluación
            evaluacion.actualizarPuntaje(nuevaNota);
            System.out.printf("✓ Nota actualizada exitosamente: %.2f%n", nuevaNota);
            
        } catch (NumberFormatException e) {
            System.out.println("Por favor, ingrese un número válido.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Muestra una cantidad específica de evaluaciones ordenadas por puntaje.
     * El usuario puede elegir cuántas evaluaciones desea ver.
     * Las evaluaciones se muestran ordenadas de mayor a menor puntaje.
     * 
     * @param repositorio almacén de todas las evaluaciones
     * @param generadorReporte genera reportes ordenados por puntaje
     */
    private static void verCantidadEvaluaciones(
            EvaluacionRepository repositorio,
            GeneradorReporte generadorReporte
    ) {
        System.out.print("\n¿Cuántas evaluaciones desea ver? ");
        try {
            // Leer la cantidad deseada
            int cantidad = Integer.parseInt(scanner.nextLine().trim());
            if (cantidad < 1) {
                System.out.println("La cantidad debe ser mayor a 0.");
                return;
            }
            
            // Obtener todas las evaluaciones ordenadas por puntaje (de mayor a menor)
            List<Evaluacion<?>> todas = generadorReporte.generarListadoOrdenado(repositorio.listarTodas());
            int total = todas.size();
            
            if (total == 0) {
                System.out.println("No hay evaluaciones registradas.");
                return;
            }
            
            // Calcular cuántas evaluaciones mostrar (no más de las que existen)
            int cantidadAMostrar = Math.min(cantidad, total);
            // Obtener solo las primeras N evaluaciones de la lista ordenada
            List<Evaluacion<?>> evaluacionesAMostrar = todas.subList(0, cantidadAMostrar);
            
            // Generar y mostrar el reporte en formato de tabla
            String reporte = generadorReporte.generarReporteEnTexto(evaluacionesAMostrar);
            System.out.println("\n=== EVALUACIONES (mostrando " + cantidadAMostrar + " de " + total + ") ===");
            System.out.println(reporte);
        } catch (NumberFormatException e) {
            System.out.println("Por favor, ingrese un número válido.");
        }
    }

    /**
     * Muestra todas las evaluaciones registradas ordenadas por puntaje.
     * También muestra información del mejor puntaje obtenido.
     * 
     * @param repositorio almacén de todas las evaluaciones
     * @param generadorReporte genera reportes ordenados por puntaje
     */
    private static void verTodasEvaluaciones(
            EvaluacionRepository repositorio,
            GeneradorReporte generadorReporte
    ) {
        // Obtener todas las evaluaciones ordenadas por puntaje (de mayor a menor)
        List<Evaluacion<?>> todas = generadorReporte.generarListadoOrdenado(repositorio.listarTodas());
        
        if (todas.isEmpty()) {
            System.out.println("\nNo hay evaluaciones registradas.");
            return;
        }
        
        // Generar y mostrar el reporte completo en formato de tabla
        String reporte = generadorReporte.generarReporteEnTexto(todas);
        System.out.println("\n=== TODAS LAS EVALUACIONES (Total: " + todas.size() + ") ===");
        System.out.println(reporte);
        
        // Mostrar información del mejor puntaje (primera evaluación de la lista ordenada)
        if (!todas.isEmpty()) {
            Evaluacion<?> mejor = todas.get(0);
            System.out.printf("Mejor puntaje: %.2f (%s)%n", mejor.getPuntaje(), mejor.getEstudiante());
        }
    }

    /**
     * Genera evaluaciones automáticamente usando el simulador.
     * Las evaluaciones se crean con datos aleatorios y se procesan inmediatamente
     * para calcular sus puntajes.
     * 
     * @param simulador genera evaluaciones con datos aleatorios
     * @param repositorio donde se guardarán las evaluaciones
     * @param calificador calcula los puntajes
     * @param colaCorreccion cola FIFO para procesar evaluaciones pendientes
     */
    private static void generarEvaluacionesAutomaticas(
            SimuladorEvaluaciones simulador,
            EvaluacionRepository repositorio,
            CalificadorEvaluaciones calificador,
            ColaCorreccion colaCorreccion
    ) {
        // Solicitar la cantidad de evaluaciones a generar (por defecto 5000)
        System.out.print("\n¿Cuántas evaluaciones desea generar? (presione Enter para 5000): ");
        String input = scanner.nextLine().trim();
        int cantidad = input.isEmpty() ? TOTAL_EVALUACIONES : Integer.parseInt(input);
        
        if (cantidad < 1) {
            System.out.println("La cantidad debe ser mayor a 0.");
            return;
        }
        
        // Generar las evaluaciones con datos aleatorios
        System.out.println("Generando " + cantidad + " evaluaciones...");
        simulador.generarEvaluaciones(cantidad, repositorio, calificador, colaCorreccion);
        System.out.println("✓ " + cantidad + " evaluaciones generadas exitosamente.");
        
        // Procesar todas las correcciones automáticamente para calcular los puntajes
        System.out.println("Procesando correcciones automáticamente...");
        int procesadas = procesarCorreccionesSilencioso(colaCorreccion, calificador);
        System.out.println("✓ " + procesadas + " evaluaciones procesadas y calificadas.");
    }
    
    /**
     * Procesa todas las evaluaciones pendientes en la cola de corrección.
     * Calcula el puntaje de cada evaluación comparando respuestas con claves.
     * Este método no muestra mensajes de progreso (versión silenciosa).
     * 
     * @param colaCorreccion cola FIFO con evaluaciones pendientes
     * @param calificador calcula los puntajes
     * @return número de evaluaciones procesadas
     */
    private static int procesarCorreccionesSilencioso(ColaCorreccion colaCorreccion, CalificadorEvaluaciones calificador) {
        int procesadas = 0;
        // Procesar todas las evaluaciones en la cola (orden FIFO)
        while (!colaCorreccion.estaVacia()) {
            // Tomar la siguiente evaluación de la cola
            Optional<Evaluacion<?>> posibleEvaluacion = colaCorreccion.tomarSiguiente();
            if (posibleEvaluacion.isEmpty()) {
                break; // No hay más evaluaciones
            }
            Evaluacion<?> evaluacion = posibleEvaluacion.get();
            // Calcular el puntaje comparando respuestas con la clave
            double puntaje = calificador.calificar(evaluacion);
            // Actualizar el puntaje en la evaluación
            evaluacion.actualizarPuntaje(puntaje);
            procesadas++;
        }
        return procesadas;
    }

    /**
     * Procesa todas las evaluaciones pendientes en la cola de corrección.
     * Calcula el puntaje de cada evaluación comparando respuestas con claves.
     * Este método muestra mensajes de progreso al usuario.
     * 
     * @param colaCorreccion cola FIFO con evaluaciones pendientes
     * @param calificador calcula los puntajes
     */
    private static void procesarTodasCorrecciones(
            ColaCorreccion colaCorreccion,
            CalificadorEvaluaciones calificador
    ) {
        System.out.println("\nProcesando correcciones...");
        int procesadas = 0;
        
        // Procesar todas las evaluaciones en la cola (orden FIFO)
        while (!colaCorreccion.estaVacia()) {
            // Tomar la siguiente evaluación de la cola
            Optional<Evaluacion<?>> posibleEvaluacion = colaCorreccion.tomarSiguiente();
            if (posibleEvaluacion.isEmpty()) {
                break; // No hay más evaluaciones
            }
            Evaluacion<?> evaluacion = posibleEvaluacion.get();
            // Calcular el puntaje comparando respuestas con la clave
            double puntaje = calificador.calificar(evaluacion);
            // Actualizar el puntaje en la evaluación
            evaluacion.actualizarPuntaje(puntaje);
            procesadas++;
        }
        
        System.out.println("✓ " + procesadas + " evaluaciones procesadas.");
    }
}

