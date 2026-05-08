package main;

import modelo.Automata;
import controlador.*;
import persitencia.EscritorGraphviz;
import persitencia.LectorTXT;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("   SISTEMA DE ANÁLISIS DE AUTÓMATAS - UBB 2026");
        System.out.println("====================================================\n");

        try {
            // 1. RUTAS DE ARCHIVOS
            String ruta1 = "entradas/automata1.txt";
            String ruta2 = "entradas/automata2.txt";

            validarEntornos();
            // 2. LECTURA (Usando tu Singleton)
            System.out.println("[Step 1] Cargando archivos desde 'entradas/'...");
            LectorTXT lector = LectorTXT.getInstancia();
            Automata a1 = lector.leerAutomataDesdeArchivo(ruta1);
            Automata a2 = lector.leerAutomataDesdeArchivo(ruta2);

            // 3. PROCESAMIENTO DEL PRIMER AUTÓMATA
            System.out.println("\n[Step 2] Procesando Autómata 1...");
            Automata afd1 = procesarFlujoCompleto(a1, "Autómata_1");

            // 4. PROCESAMIENTO DEL SEGUNDO AUTÓMATA
            System.out.println("\n[Step 3] Procesando Autómata 2...");
            Automata afd2 = procesarFlujoCompleto(a2, "Autómata_2");

            // 5. VERIFICACIÓN DE EQUIVALENCIA
            System.out.println("\n[Step 4] Comprobando equivalencia de lenguajes...");
            boolean equivalentes = VerificadorEquivalencia.sonEquivalentes(afd1, afd2);
            
            System.out.println("----------------------------------------------------");
            if (equivalentes) {
                System.out.println(">>> RESULTADO: Los autómatas SON EQUIVALENTES.");
            } else {
                System.out.println(">>> RESULTADO: Los autómatas NO SON equivalentes.");
            }
            System.out.println("----------------------------------------------------");

            System.out.println("\n[INFO] Revisa la carpeta 'salidas/' para ver los archivos .dot y .png");

        } catch (Exception e) {
            System.err.println("\n[ERROR CRÍTICO]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Automata procesarFlujoCompleto(Automata a, String etiqueta) {
        Automata resultado = a;

        // ¿Es AFND?
        if (AnalizarAutomata.esAFND(a)) {

            System.out.println("  > Detectado como AFND. Convirtiendo a AFD...");
            // Aquí llamarías a tu clase de conversión (Subconjuntos)
            resultado = ConvertidorAFNDaAFD.convertir(a);
        } else {
            System.out.println("  > Detectado como AFD. Saltando conversión.");
        }

        // Minimización obligatoria por pauta
        System.out.println("  > Aplicando Algoritmo de Minimización...");
        resultado = Minimizador.minimizar(resultado);

        // Exportar a Graphviz
        EscritorGraphviz.generarArchivoDot(resultado, "resultado_" + etiqueta);
        EscritorGraphviz.mostrarGrafico("resultado_" + etiqueta);

        return resultado;
    }

    private static void validarEntornos() throws Exception {
        File folderSalida = new File("salidas");
        if (!folderSalida.exists()) folderSalida.mkdir();
        
        File folderEntrada = new File("entradas");
        if (!folderEntrada.exists()) {
            throw new Exception("La carpeta 'entradas' no existe. Créala y añade los .txt");
        }
    }
}