package com.caso5;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Genera reportes ordenados por puntaje de manera descendente.
 */
public class GeneradorReporte {

    // Ordena por puntaje descendente y desempata por orden de llegada
    private static final Comparator<Evaluacion<?>> COMPARADOR_PUNTAJE = (a, b) -> {
        int comparacionPuntaje = Double.compare(b.getPuntaje(), a.getPuntaje());
        if (comparacionPuntaje != 0) {
            return comparacionPuntaje;
        }
        return Long.compare(a.getOrdenLlegada(), b.getOrdenLlegada());
    };

    /**
     * Crea un listado sin duplicados y ordenado por puntaje.
     *
     * @param evaluaciones colección de evaluaciones procesadas.
     * @return lista ordenada de mayor a menor puntaje.
     */
    public List<Evaluacion<?>> generarListadoOrdenado(Iterable<Evaluacion<?>> evaluaciones) {
        // TreeSet mantiene ordenado y evita duplicados automáticamente
        Set<Evaluacion<?>> conjuntoOrdenado = new TreeSet<>(COMPARADOR_PUNTAJE);
        for (Evaluacion<?> evaluacion : evaluaciones) {
            conjuntoOrdenado.add(evaluacion);
        }
        return new ArrayList<>(conjuntoOrdenado);
    }

    /**
     * Genera un reporte textual amigable para lectura humana.
     *
     * @param evaluaciones evaluaciones ya ordenadas.
     * @return texto tabular que resume los puntajes.
     */
    public String generarReporteEnTexto(List<Evaluacion<?>> evaluaciones) {
        StringBuilder reporte = new StringBuilder();
        reporte.append(String.format("%-36s | %-20s | %-8s | %-7s%n",
                "ID", "Estudiante", "Tipo", "Puntaje"));
        reporte.append("------------------------------------------------------------------------------------------\n");
        for (Evaluacion<?> evaluacion : evaluaciones) {
            reporte.append(String.format(
                    "%-36s | %-20s | %-8s | %-7.2f%n",
                    evaluacion.getId(),
                    recortar(evaluacion.getEstudiante(), 20),
                    evaluacion.getTipo(),
                    evaluacion.getPuntaje()
            ));
        }
        return reporte.toString();
    }

    private String recortar(String texto, int limite) {
        if (texto.length() <= limite) {
            return texto;
        }
        return texto.substring(0, limite - 3) + "...";
    }
}

