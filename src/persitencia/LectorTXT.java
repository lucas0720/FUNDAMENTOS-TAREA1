package persitencia;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

import modelo.Automata;
import modelo.Transicion;

public class LectorTXT {
    
    private static LectorTXT instancia;

    public static LectorTXT getInstancia() {
        if (instancia == null) {
            instancia = new LectorTXT();
        }
        return instancia;
    }

    public Automata leerAutomataDesdeArchivo(String rutaArchivo) {

        //DATOS DE UN AUTOMATA
        HashSet<String> estados = new HashSet<>();
        HashSet<String> alfabeto = new HashSet<>();
        HashSet<String> estadosFinales = new HashSet<>();
        String estadoInicial = "";
        ArrayList<Transicion> transiciones = new ArrayList<>();
       


        try(

            FileReader archivo = new FileReader(rutaArchivo);
            BufferedReader lector = new BufferedReader(archivo);

        ){
            String linea;

            while ((linea = lector.readLine()) != null) {
                //Limpieza de linea
                linea = linea.trim();
                if (linea.isEmpty() || linea.equals("delta:")) continue;
                linea = linea.replace(" ", "");
                
                //BUSCAR DATOS EN EL ARCHIVO

                //ESTADOS
                if (linea.startsWith("k=")) {
                    estados = procesarEstados(linea, estados);
                }

                //ALFABETO
                else if (linea.startsWith("sigma=")) {
                    alfabeto = procesarAlfabeto(linea, alfabeto);
                }

                //"delta:"
                else if (linea.startsWith("(")) {
                    transiciones = buscarTransiciones(linea, transiciones);
                }

                //ESTADO INICIAL
                else if (linea.startsWith("s=")) {
                    estadoInicial = linea.replace("s=", "");
                }

                //ESTADOS FINALES
                else if (linea.startsWith("f=")) {
                    estadosFinales = procesarEstadosFinales(linea, estadosFinales);
                    
                }

            }

        } catch (Exception e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        
        }

        Automata automata = new Automata(estados, alfabeto, estadoInicial, estadosFinales, transiciones);
        return automata;
    }


    //METOOD PRIVADOS

    //BUSCAR LOS ESTADOS
    private HashSet<String> procesarEstados(String linea, HashSet<String> estados) {
        String contenido = linea;

        contenido = contenido.replace("k={", "");
        contenido = contenido.replace("}", "");
        String[] estadosarr = contenido.split(",");

        for (String e : estadosarr) {
            estados.add(e);
        }
        return estados; // aunque no sea necesario lo hago por claridad de codigo ahora mismo
    }

    //BUSCAR LOS SIMBOLOS DEL ALFABETO
    private HashSet<String> procesarAlfabeto(String linea, HashSet<String> alfabeto) {
        String contenido = linea;

        contenido = contenido.replace("sigma={", "");
        contenido = contenido.replace("}", "");
        String[] simbolosarr = contenido.split(",");

        for (String simbolo : simbolosarr) {
            alfabeto.add(simbolo);
        }
        return alfabeto; 
    }

    //BUSCAR LAS TRANSICIONES
    private ArrayList<Transicion> buscarTransiciones(String linea, ArrayList<Transicion> transiciones) {
        String contenido = linea;
        contenido = contenido.replace("(", "");
        contenido = contenido.replace(")", "");

        String[] partes = contenido.split(",");

        if (partes.length == 3) {

            String origen = partes[0];
            String simbolo = partes[1];
            String destino = partes[2];

            Transicion transicion = new Transicion(origen, simbolo, destino);
            transiciones.add(transicion);
        }
        return transiciones;
    }

    //ESTADOS FINALES
    private HashSet<String> procesarEstadosFinales(String linea, HashSet<String> estadosFinales) {

        String contenido = linea.replace("f={", "").replace("}", "");
        String[] estadosF = contenido.split(",");
        
        for (String estado : estadosF) {
            estadosFinales.add(estado);
        }
        return estadosFinales;
    }
}
