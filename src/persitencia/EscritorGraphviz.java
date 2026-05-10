package persitencia;

import modelo.Automata;
import modelo.Transicion;
import java.io.FileWriter;
import java.io.PrintWriter;


public class EscritorGraphviz {

    /**
     * ----- METODO ----- generarArchivoDot(Automata automata, String nombreArchivo)
     * toma el autómata y escribe las instrucciones de dibujo línea por línea en un archivo.
     */
    public static void generarArchivoDot(Automata automata, String nombreArchivo) {
        // creamos la ruta hacia la carpeta de salidas
        String ruta = "salidas/" + nombreArchivo + ".dot";
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta))) {

            pw.println("digraph G {");
            pw.println("  rankdir=LR;");
            pw.println("  node [shape = circle];"); 

            //Dibujamos los estados de aceptacion 
            if (!automata.getEstadosFinales().isEmpty()) {
                pw.print("  node [shape = doublecircle];");
                for (String f : automata.getEstadosFinales()) {
                    // Usamos comillas por si el nombre tiene comas (como "q0,q1")
                    pw.print(" \"" + f + "\"");
                }
                pw.println(";");
            }

            //dibujamos el estado inicial

            pw.println("  node [shape = circle];");
            pw.println("  secret_node [style=invisible, width=0, height=0, label=\"\"];");
            pw.println("  secret_node -> \"" + automata.getEstadoInicial() + "\";");

            // Escribir las transiciones
            for (Transicion t : automata.getTransiciones()) {
                pw.println("  \"" + t.getEstadoOrigen() + "\" -> \"" + t.getEstadoDestino() + "\" [label=\"" + t.getSimbolo() + "\"];");
            }

            pw.println("}");
            System.out.println("Archivo DOT generado con éxito en: " + ruta);

        } catch (Exception e) {
            System.err.println("Error al crear el archivo DOT: " + e.getMessage());
        }
    }

    /**
     * ----- METODO ----- mostrarGrafico(String nombreArchivo)
     * 
     * Una vez que el archivo .dot de texto existe, este método le da la orden al computador 
     * para que lo transforme en imagen y lo abra en pantalla automáticamente.
     */

    public static void mostrarGrafico(String nombreArchivo) {
        try {
            String dotPath = "salidas/" + nombreArchivo + ".dot";
            String pngPath = "salidas/" + nombreArchivo + ".png";

            //Generar el PNG 
            ProcessBuilder pbDot = new ProcessBuilder("dot", "-Tpng", dotPath, "-o", pngPath);
            pbDot.start().waitFor();

            // 2. Abrir la imagen en Windows
            // Usamos "cmd /c start" que es el equivalente a hacer doble clic
            new ProcessBuilder("cmd", "/c", "start", pngPath).start();

        } catch (Exception e) {
            System.out.println("No se pudo abrir automáticamente. Error: " + e.getMessage());
        }
    }
}

//Aqui nos ayudamos con ia al buscar como generar el archivo . dot y como hacerlo forma correcta porque no teniamos experiencia de como hacerl0
//lo ocupamos como guia

/**
 *  ==== CLASE ESTRUCTURA Y CONCEPTO =====
 * Esta clase actúa como el "Dibujante" de nuestro proyecto y su única responsabilidad 
 * es tomar toda la lógica de nuestros objetos Automata y traducirla a 
 * un archivo de texto que el programa Graphviz pueda entender para dibujar los grafos.
 * 
 * NOTA DE DESARROLLO (Uso de IA como guía técnica):
 * Como equipo no teníamos experiencia previa escribiendo sintaxis de lenguaje DOT 

 * Inteligencia Artificial como documentación interactiva y guía para aprender a:
 * 1. Estructurar correctamente el archivo .dot.
 * 
 * 2. Utilizar la clase ProcessBuilder para comunicarnos con el sistema operativo.
 *
 */
  