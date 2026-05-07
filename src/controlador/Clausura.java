package controlador;

import java.util.*;

import modelo.Automata;
import modelo.Transicion;
//INTENGAR JUTNAS RESTOCON EL CONVERITIDOR PORQUE ALFINAL SE TRARTA DE UN PROCESO
public class Clausura {

    public static final String EPSILON = "epsilon"; // Constante para representar la transición epsilon

    private Clausura() {} // Constructor privado para evitar uso en otra clase
    
    public static Set<String> clausuraEpsilon(Automata automata, String estado) { // Calcula la clausura epsilon en un solo estado
        Set<String> clausuraEpsilon = new HashSet<>(); // Aqui guardamos los estados que son alcanzables a través de epsilon

        Queue<String> cola = new LinkedList<>(); // Procesamos los estados en orden de descubrimiento

        clausuraEpsilon.add(estado); // Todo estado pertenece a su propia clausura
        cola.add(estado);

        while (!cola.isEmpty()) {
            String estadoActual = cola.poll(); // Saca el siguiente estado a procesar
            
            for (Transicion t : automata.getTransiciones()) { // Revisamos todas las transiciones del automata
                
                boolean esOrigen = t.getEstadoOrigen().equals(estadoActual); // Transiciones con epsilon desde el estado actual
                boolean esEpsilon = t.getSimbolo().equals(EPSILON);

                if (esOrigen && esEpsilon) {
                    String destino = t.getEstadoDestino();

                    if (!clausuraEpsilon.contains(destino)) { // Se agrega solo si no fue visitado antes para asi evitar errores de ciclos con epsilon
                        clausuraEpsilon.add(destino);
                        cola.add(destino); // Se encola para recorrer tambien sus epsilon
                    }
                }
            }
        }

        return clausuraEpsilon;
    }

    public static Set<String> clausuraEpsilonConjunto(Automata automata, Set<String> estados) { // Calcula la clausura epsilon de un conjunto de estados. 
        Set<String> clausuraEpsilonTotal = new HashSet<>();

        for (String estado : estados) { // Calculamos la clausura de cada estado y unimos los resultados
            clausuraEpsilonTotal.addAll(clausuraEpsilon(automata, estado)); 
        }

        return clausuraEpsilonTotal;
    }

    public static Set<String> mover (Automata automata, Set<String> estados, String simbolo) { // Calcula los estados alcanzables desde un conjunto de estados al leer un simbolo especifico y luego aplica clausura epsilon.
    
        Set<String> movidos = new HashSet<>(); // conjunto de estados alcanzables directamente leyendo un simbolo
    
        for (String estado : estados) {
            for (Transicion t : automata.getTransiciones()) { // Revisamos todas las transiciones del automata
                
                // transiciones desde el estado actual con el simbolo dado
                boolean mismoOrigen = t.getEstadoOrigen().equals(estado); 
                boolean mismoSimbolo = t.getSimbolo().equals(simbolo);

                if (mismoOrigen && mismoSimbolo) { 
                    movidos.add(t.getEstadoDestino()); // Agregamos el estado destino al conjunto de movidos
                }
            }
        }
        // Despues de movernos con el simbolo, aplicamos clausura epsilon, porque el AFND puede seguir avanzando con transiciones vacias 
        return clausuraEpsilonConjunto(automata, movidos);
    }

    public static boolean tieneTransicionesEpsilon(Automata automata) { // Detecta si el automata tiene transicion epsilon

        for (Transicion t : automata.getTransiciones()) {
            if (t.getSimbolo().equals(EPSILON)) {
                return true; // Con encontrar una basta
            }
        }
        return false; // No se encuentra ninguna
    }
}