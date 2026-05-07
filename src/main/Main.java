package main;

import modelo.Automata;
import modelo.Transicion;
import persitencia.LectorTXT;

public class Main {

    public static void main(String[] args) {
        LectorTXT lector = LectorTXT.getInstancia();

        // 2. Definimos todos los archivos que están en la carpeta 'entradas'
        String[] archivosPrueba = {
            "automata_normal.txt",
            "automata_sucio.txt",
            "automata_complejo.txt",
            "automata_minimo.txt",
            "AFD1.txt",
            "AFD2.txt",
            "AFND1.txt",
            "AFND2.txt"
        };

        System.out.println("=================================================");
        System.out.println("   INICIANDO BATERÍA DE PRUEBAS DE LECTURA");
        System.out.println("=================================================\n");
        for (String nombreArchivo : archivosPrueba) {
            String ruta = "entradas/" + nombreArchivo;
            
            System.out.println("-------------------------------------------------");
            System.out.println(" Leyendo archivo: " + ruta);
            System.out.println("-------------------------------------------------");
            
            try {
                Automata automataEnMemoria = lector.leerAutomataDesdeArchivo(ruta);
                imprimirDetalles(automataEnMemoria);
                
            } catch (Exception e) {
                System.out.println("ERROR :No se pudo procesar el archivo.");
                System.out.println("Verifica que '" + nombreArchivo + "' exista en la carpeta 'entradas'.");
            }
            System.out.println("\n");
        }
    }

    /**
     * Método auxiliar para imprimir los datos del Autómata de forma ordenada.
     */
    private static void imprimirDetalles(Automata a) {
        
        if (a == null || a.getEstados() == null || a.getEstados().isEmpty()) {
            System.out.println(" ALERTA: El autómata se creó, pero está vacío. Revisa el formato del TXT.");
            return;
        }

        System.out.println("Objeto Autómata construido exitosamente:");
        System.out.println("  -> K (Estados):         " + a.getEstados());
        System.out.println("  -> Sigma (Alfabeto):    " + a.getAlfabeto());
        System.out.println("  -> s (Estado Inicial):  " + a.getEstadoInicial());
        System.out.println("  -> F (Estados Finales): " + a.getEstadosFinales());
        
        System.out.println("  -> Transiciones (Total: " + a.getTransiciones().size() + "):");
        
        // Recorremos la lista de transiciones para imprimirlas como fórmulas (origen, símbolo) -> destino
        for (Transicion t : a.getTransiciones()) {
            System.out.println("       (" + t.getEstadoOrigen() + ", " + t.getSimbolo() + ") ---> " + t.getEstadoDestino());
        }
    }
}