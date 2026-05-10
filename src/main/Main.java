package main;

import modelo.Automata;
import persitencia.LectorTXT;
import persitencia.EscritorGraphviz;
import controlador.ConvertidorAFNDaAFD; 
import controlador.Minimizador;
import controlador.VerificadorEquivalencia;

import java.io.File;
import java.util.Scanner;

/**
 * 1. ==== CLASE ESTRUCTURA Y CONCEPTO =====
 * Clase principal que gestiona la interfaz de consola del usuario.
 * Ejecuta el flujo secuencial de lectura, conversión, minimización y 
 * verificación de autómatas, mostrando los resultados en un formato 
 * estructurado y profesional.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        //DIBUJITOS PARA QUE SE VEA BONITO EN CONSOLA
        System.out.println("┌────────────────────────────────────────────────────────┐");
        System.out.println("│           SISTEMA DE ANÁLISIS DE AUTÓMATAS 1           │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        System.out.println(" INSTRUCCIONES:");
        System.out.println(" Coloque sus archivos de texto (.txt) con las definiciones");
        System.out.println(" matemáticas dentro del directorio: /Entradas");
        System.out.println("──────────────────────────────────────────────────────────\n");

        while (!salir) {
            System.out.println("┌─────────────────────── MENÚ ───────────────────────────┐");
            System.out.println("│ [1] Procesar y comparar dos autómatas                  │");
            System.out.println("│ [2] Salir del sistema                                  │");
            System.out.println("└────────────────────────────────────────────────────────┘");
            System.out.print(" Seleccione una opción: ");
            
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    ejecutarFlujoCompleto(scanner);
                    break;
                case "2":
                    System.out.println("\nfinalizando ejecución del sistema");
                    salir = true;
                    break;
                default:
                    System.out.println("\n[Opción no válida :c Intente nuevamente.\n");
            }
        }
        scanner.close();
    }

    private static void ejecutarFlujoCompleto(Scanner scanner) {
        System.out.println("\n───────────────── INGRESO DE ARCHIVOS ────────────────────");
        System.out.print(" Ingrese nombre del PRIMER archivo  (ej: automata1): ");
        String nombreArch1 = scanner.nextLine();
        
        System.out.print(" Ingrese nombre del SEGUNDO archivo (ej: automata2): ");
        String nombreArch2 = scanner.nextLine();

        String ruta1 = "Entradas/" + nombreArch1 + ".txt";
        String ruta2 = "Entradas/" + nombreArch2 + ".txt";

        if (!new File(ruta1).exists() || !new File(ruta2).exists()) {
            System.out.println("\n[ERROR] No se encontraron los archivos en la ruta especificada.");
            System.out.println("Verifique el directorio /Entradas e intente nuevamente.\n");
            return; 
        }

        System.out.println("\nINICIANDO SECUENCIA DE ANÁLISIS");

        // PROCESANDO EL PRIMER AUTO-MATA
        Automata min1 = procesarUnAutomata(ruta1, nombreArch1, 1);
        // PROCESANDO EL SEGUNDO AUTO-MATA
        Automata min2 = procesarUnAutomata(ruta2, nombreArch2, 2);

        // VERIFICACIÓN 
        System.out.println("\n==========================================================");
        System.out.println("                     FASE DE VERIFICACIÓN                   ");
        System.out.println("===============================================================");
        
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│ PASO 5: ANÁLISIS DE ISOMORFISMO ENTRE AUTÓMATAS        │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        System.out.println(" >Evaluando igualdad");
        
        boolean sonIguales = VerificadorEquivalencia.sonEquivalentes(min1, min2);

        System.out.println("\n┌──────────────────── VEREDICTO FINAL ───────────────────┐");
        if (sonIguales) {
            System.out.println("│ RESULTADO: EQUIVALENTES                                │");
            System.out.println("│ Detalle  : Ambos autómatas procesan exactamente el     │");
            System.out.println("│            mismo lenguaje regular.                     │");
        } else {
            System.out.println("│ RESULTADO: NO EQUIVALENTES                             │");
            System.out.println("│ Detalle  : Los autómatas difieren en el lenguaje que   │");
            System.out.println("│            aceptan o en su comportamiento lógico.      │");
        }
        System.out.println("└────────────────────────────────────────────────────────┘\n");
    }

    /**
     * ----- METODO AYUDANTE -----
     * Procesa un autómata individual aplicando las fases de lectura,
     */
    private static Automata procesarUnAutomata(String rutaArchivo, String nombreArchivo, int numeroAutomata) {
        
        System.out.println("\n==========================================================");
        System.out.println("           ANALIZANDO AUTÓMATA " + numeroAutomata + " (" + nombreArchivo + ".txt)");
        System.out.println("==========================================================");

        // PASO 1
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│ PASO 1: LECTURA DE ARCHIVO Y EXTRACCIÓN DE DATOS       │");
        System.out.println("└────────────────────────────────────────────────────────┘");

        Automata automataOriginal = LectorTXT.getInstancia().leerAutomataDesdeArchivo(rutaArchivo);

        System.out.println(" > Archivo procesado correctamente.");
        System.out.println(" > Total de estados originales definidos: " + automataOriginal.getEstados().size());

        // PASO 2
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│ PASO 2: CONVERSIÓN Y BLINDAJE (AFND -> AFD)            │");
        System.out.println("└────────────────────────────────────────────────────────┘");

        Automata afd = ConvertidorAFNDaAFD.convertir(automataOriginal);

        System.out.println(" > Transformación a Autómata Finito Determinista completada.");
        System.out.println(" > Estados resultantes tras aplicar subconjuntos: " + afd.getEstados().size());

        // PASO 3
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│ PASO 3: MINIMIZACIÓN DE ESTADOS (MYHILL-NERODE)        │");
        System.out.println("└────────────────────────────────────────────────────────┘");

        Automata minimizado = Minimizador.minimizar(afd);

        System.out.println(" > Algoritmo de partición de equivalencia aplicado.");
        System.out.println(" > El autómata ha sido optimizado a: " + minimizado.getEstados().size() + " estados.");

        // PASO 4
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│ PASO 4: GENERACIÓN DE MODELO VISUAL (GRAPHVIZ)         │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        String nombreSalida = nombreArchivo + "minimizado";

        EscritorGraphviz.generarArchivoDot(minimizado, nombreSalida);
        EscritorGraphviz.mostrarGrafico(nombreSalida);

        System.out.println(" > Archivo .dot generado y renderizado a .png.");
        System.out.println(" > Imagen abierta en el visor del sistema.");
        System.out.println("\n----------------------------------------------------------");

        return minimizado;
    }
}