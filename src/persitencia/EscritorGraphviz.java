package persitencia;

import modelo.Automata;
import modelo.Transicion;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;

public class EscritorGraphviz {

    public static void generarArchivoDot(Automata automata, String nombreArchivo) {
        // Creamos la ruta hacia la carpeta de salidas
        String ruta = "salidas/" + nombreArchivo + ".dot";
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(ruta))) {

            pw.println("digraph G {");
            pw.println("  rankdir=LR;");
            pw.println("  node [shape = circle];"); 
            
  
            if (!automata.getEstadosFinales().isEmpty()) {
                pw.print("  node [shape = doublecircle];");
                for (String f : automata.getEstadosFinales()) {
                    // Usamos comillas por si el nombre tiene comas (como "q0,q1")
                    pw.print(" \"" + f + "\"");
                }
                pw.println(";");
            }

            // 2. Volver a círculo normal para el resto y crear la flecha inicial
            pw.println("  node [shape = circle];");
            pw.println("  secret_node [style=invisible, width=0, height=0, label=\"\"];");
            pw.println("  secret_node -> \"" + automata.getEstadoInicial() + "\";");

            // 3. Escribir las transiciones
            for (Transicion t : automata.getTransiciones()) {
                pw.println("  \"" + t.getEstadoOrigen() + "\" -> \"" + t.getEstadoDestino() + "\" [label=\"" + t.getSimbolo() + "\"];");
            }

            pw.println("}");
            System.out.println("Archivo DOT generado con éxito en: " + ruta);

        } catch (Exception e) {
            System.err.println("Error al crear el archivo DOT: " + e.getMessage());
        }
    }

    public static void mostrarGrafico(String nombreArchivo) {
        try {
            String dotPath = "salidas/" + nombreArchivo + ".dot";
            String pngPath = "salidas/" + nombreArchivo + ".png";

            // 1. Generar el PNG (Esto es igual en todos lados si el PATH está bien)
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