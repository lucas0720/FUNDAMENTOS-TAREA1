package controlador;

import java.util.*;
import modelo.*;

public class ConvertidorAFND {
    private ConvertidorAFND() {} // Constructor privado para evitar uso en otra clase

    public static Automata convertirAFND(Automata afnd) {
        Set<String> estadoInicialAFD = Clausura.clausuraEpsilon(afnd, afnd.getEstadoInicial()); // El estado inicial del AFD es la clausura epsilon del estado inicial del AFND
    
        List<Set<String>> estadosAFD = new ArrayList<>(); // Lista de conjuntos de estados del AFD
        List<Transicion> transicionesAFD = new ArrayList<>(); // Lista de transiciones del AFD
        Queue<Set<String>> porProcesar = new LinkedList<>(); // Cola para procesar los estados del AFD uno a uno
        
        estadosAFD.add(estadoInicialAFD); // Agregamos el estado inicial a la lista de estados del AFD y a la cola
        porProcesar.add(estadoInicialAFD);
        
        // Procesar cada estado del AFD nuevo
        while (!porProcesar.isEmpty()) { // Mientras haya estados sin procesar

            Set<String> estadoActual = porProcesar.poll(); // Sacamos el siguiente conjunto de estados a procesar
            
            for (String simbolo : afnd.getAlfabeto()) { // Por cada simbolo del alfabeto (excepto epsilon)
                
                // Calculamos el conjunto de estados alcanzables desde el estado actual al leer el simbolo 
                // Clausura.mover() ya incluye la clausura epsilon del resultado
                Set<String> estadosDestino = Clausura.mover(afnd, estadoActual, simbolo);

                // En caso de que el conjunto destino este vacio, no hay transicion
                if (estadosDestino.isEmpty()) {
                    continue;
                }

                // Si el estado destino no lo hemos visto antes, lo agregamos como estado nuevo
                if (!contiene(estadosAFD, estadosDestino)) {
                    estadosAFD.add(estadosDestino);
                    porProcesar.add(estadosDestino);
                    // se enconla para procesarlo despues
                }

                Transicion t = new Transicion(
                    conjuntoANombre(estadoActual), // Convertimos el conjunto de estados a un nombre unico para el AFD
                    simbolo,
                    conjuntoANombre(estadosDestino)    
                );
                transicionesAFD.add(t); // Agregamos la transicion al AFD
            
            }

        }
        

        // Un estado del AFD es final si alguno de los estados que contiene es final en el AFND original
        // Al menos un estado final!!
        Set<String> estadosFinalesAFD = new HashSet<>(); // Conjunto de estados finales del AFD
        
        for (Set<String> estadoAFD : estadosAFD) {
            for (String estadoFinal : afnd.getEstadosFinales()) {
                if (estadoAFD.contains(estadoFinal)) {
                    estadosFinalesAFD.add(conjuntoANombre(estadoAFD)); 
                    break; // No es necesario seguir revisando los estados finales del AFND para este conjunto
                }
            }
        }
        
        // Construye el conjunto de estados del AFD (como nombres)
        Set<String> nombresEstadosAFD = new HashSet<>();
        for (Set<String> estadoAFD : estadosAFD) {
            nombresEstadosAFD.add(conjuntoANombre(estadoAFD)); // Convertimos cada conjunto de estados a un nombre unico para el AFD
        }

        // Armar y retornar el AFD resultante
        // El alfabeto es el mismo que en el AFND solo que sin epsilon
        Set<String> alfabetoSinEpsilon = new HashSet<>(afnd.getAlfabeto());
        alfabetoSinEpsilon.remove(Clausura.EPSILON); // Eliminamos epsilon del alfabeto para el AFD

        
        return new Automata(
            nombresEstadosAFD,
            alfabetoSinEpsilon,
            conjuntoANombre(estadoInicialAFD),
            estadosFinalesAFD,
            transicionesAFD
        );
    }

    // Metodos Auxiliares

    // Convierte un conjunto de estandos en un nombre de tipo String.
    // Ordenamos los estados alfabeticamente para que el mismo conjunto siempre genere un mismo nombre,
    // sin importar el orden interno del conjunto.
    private static String conjuntoANombre(Set<String> conjunto) {
        
        List<String> ordenados = new ArrayList<>(conjunto); // Convertimos el conjunto a una lista para ordenar los estados
        Collections.sort(ordenados); // Ordenamos para consistencia
        return "{" + String.join(",", ordenados) + "}"; // Unimos los estados con comas y los encerramos entre llaves para formar el nombre del estado del AFD
    
    }

    // Comprueba si es que un conjunto ya existe en la lista de estados del AFD
    private static boolean contiene(List<Set<String>> lista, Set<String> conjunto) { 
        
        for (Set<String> existente : lista) {

            if (existente.equals(conjunto)) {
                return true;
            }

        }
        
        return false;

    }
}
