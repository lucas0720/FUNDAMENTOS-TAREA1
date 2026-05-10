package controlador;

import modelo.*;
import java.util.*;

public class VerificadorEquivalencia {

    /**
     * Este es el método principal que llamarás desde tu Main.
     */
    public static boolean sonEquivalentes(Automata af1, Automata af2) {
        // minimizamos ambos autómatas para dejarlos en su versión más compacta
        Automata min1 = Minimizador.minimizar(af1);
        Automata min2 = Minimizador.minimizar(af2);

        // estandarizamos los nombres de sus estados
        // esto asegura que si ambos tienen la misma estructura, ambos se llamarán "q0, q1 " porque son iguale
        Automata norm1 = normalizarNombres(min1);
        Automata norm2 = normalizarNombres(min2);

        //Usamos el método equals() que tú ya programaste en la clase Automata
        return norm1.equals(norm2);
    }

    /*+
     * Recorre el autómata de forma ordenada  y le cambia los nombres a los 
     * estados por "q0", "q1", "q2"... siguiendo el rastro de las letras.
     */

    private static Automata normalizarNombres(Automata afdMinimo) {
        Map<String, String> diccionarioNombres = new HashMap<>();
        Queue<String> colaBusqueda = new LinkedList<>(); // Bandeja de pendientes
        
        int contador = 0;
        String inicialOriginal = afdMinimo.getEstadoInicial();
        
        // El estado inicial será bautizado como "q0"
        diccionarioNombres.put(inicialOriginal, "q" + contador);
        colaBusqueda.add(inicialOriginal);
        contador++;

        // Ordenamos el alfabeto alfabéticamente (ej: primero 'a', luego 'b')
        // para que la exploración siempre siga el mismo camino
        List<String> alfabetoOrdenado = new ArrayList<>(afdMinimo.getAlfabeto());
        Collections.sort(alfabetoOrdenado);

        // Exploramos el autómata camino por camino
        while (!colaBusqueda.isEmpty()) {
            String estadoActual = colaBusqueda.poll();

            for (String letra : alfabetoOrdenado) {
                String destino = obtenerDestinoUnico(afdMinimo, estadoActual, letra);
                
                // Si llegamos a un estado que aún no tiene nombre nuevo, lo bautizamos
                if (destino != null && !diccionarioNombres.containsKey(destino)) {
                    diccionarioNombres.put(destino, "q" + contador);
                    colaBusqueda.add(destino);
                    contador++; // El próximo se llamará q1, luego q2, etc.
                }
            }
        }

        Automata normalizado = new Automata();
        normalizado.setAlfabeto(afdMinimo.getAlfabeto());
        normalizado.setEstadoInicial(diccionarioNombres.get(inicialOriginal));

        Set<String> nuevosEstados = new HashSet<>();
        Set<String> nuevosFinales = new HashSet<>();
        List<Transicion> nuevasTransiciones = new ArrayList<>();

        for (String estadoAntiguo : afdMinimo.getEstados()) {
            String nombreNuevo = diccionarioNombres.get(estadoAntiguo);
            
            if (nombreNuevo != null) {
                nuevosEstados.add(nombreNuevo);
                // Si el original era de aceptación, el nuevo también lo es
                if (afdMinimo.getEstadosFinales().contains(estadoAntiguo)) {
                    nuevosFinales.add(nombreNuevo);
                }
            }
        }

        for (Transicion t : afdMinimo.getTransiciones()) {
            String origenNuevo = diccionarioNombres.get(t.getEstadoOrigen());
            String destinoNuevo = diccionarioNombres.get(t.getEstadoDestino());
            
            if (origenNuevo != null && destinoNuevo != null) {
                nuevasTransiciones.add(new Transicion(origenNuevo, t.getSimbolo(), destinoNuevo));
            }
        }

        normalizado.setEstados(nuevosEstados);
        normalizado.setEstadosFinales(nuevosFinales);
        normalizado.setTransiciones(nuevasTransiciones);

        return normalizado;
    }

    private static String obtenerDestinoUnico(Automata afd, String origen, String letra) {
        for (Transicion t : afd.getTransiciones()) {
            if (t.getEstadoOrigen().equals(origen) && t.getSimbolo().equals(letra)) {
                return t.getEstadoDestino();
            }
        }
        return null;
    }
}/**
 *  ==== CLASE ESTRUCTURA Y CONCEPTO =====
 * Esta clase se encarga de resolver el problema del ismorfimo entre dos autómatas.
 *  osea cuando Dos autómatas pueden estar dibujados de forma totalmente distinta 
 * y tener nombres de estados diferentes, pero si aceptan exactamente el mismo lenguaje, 
 * son equivalentes. 
 * 
 * Nuestra estrategia de solución es el de los caminos gemelos:
 * 1. Minimizamos ambos autómatas a su máxima expresión.
 * 
 * 2. Les borramos los nombres originales y los "normalizamos" .
 * 
 * 3. Si realmente hacían lo mismo, al normalizarlos deberían ser gemelos idénticos.
 */