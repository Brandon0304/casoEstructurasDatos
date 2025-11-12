# Caso 5 - Estructuras de Datos Simples

Aplicación de consola en Java que simula el caso solicitado: registra evaluaciones, evita duplicados, corrige en orden FIFO y genera un reporte ordenado por puntaje. Solo se emplean las estructuras requeridas (`HashMap`, `HashSet`, `LinkedList`, `ArrayList`, `TreeSet`) y clases genéricas para manejar diferentes tipos de respuestas.

## Requisitos

- Java 17 o superior.

## Ejecución rápida

```bash
javac -d out src/main/java/com/caso5/*.java
java -cp out com.caso5.Aplicacion
```

## Componentes principales

- `Evaluacion<T>`: modelo genérico con ID, estudiante, tipo, respuesta y puntaje.
- `EvaluacionRepository`: registro principal con `HashMap` y control de duplicados por `HashSet`.
- `ColaCorreccion`: cola FIFO basada en `LinkedList`.
- `CalificadorEvaluaciones`: calcula puntajes según tipo (OM, VF, Abierta).
- `SimuladorEvaluaciones`: genera hasta 5 000 evaluaciones y claves.
- `GeneradorReporte`: usa `TreeSet` para ordenar por puntaje y `ArrayList` para preparar el reporte.
- `Aplicacion`: coordina la simulación completa.
