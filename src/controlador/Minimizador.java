package controlador;

import java.util.*;
import modelo.*;
public class Minimizador {

    public static Automata minimizar(Automata afd) {
        
        // PASO 0: Obtener los estados y prepararlos en una lista para poder iterar ordenadamente
        List<String> estados = new ArrayList<>(afd.getEstados());
        Set<String> finales = afd.getEstadosFinales();
        
        // Nuestra "Tabla" será un Conjunto que guarda las parejas que Tienen una 'X'
        // Si la pareja {q1, q2} está aquí adentro, significa que son DISTINGUIBLES (tienen una X)
        Set<Set<String>> paresMarcados = new HashSet<>();

        // ========================================================================
        // PASO 1: CASO BASE (Marcar final vs no-final) - Diapositiva 4 del PDF
        // ========================================================================
        for (int i = 0; i < estados.size(); i++) {
            for (int j = i + 1; j < estados.size(); j++) {
                String p = estados.get(i);
                String q = estados.get(j);
                
                boolean pEsFinal = finales.contains(p);
                boolean qEsFinal = finales.contains(q);
                
                // Si uno es final y el otro no (XOR), le ponemos una 'X' en la tabla
                if (pEsFinal != qEsFinal) {
                    paresMarcados.add(new HashSet<>(Arrays.asList(p, q)));
                }
            }
        }

        // ========================================================================
        // PASO 2: ITERACIONES (Revisar los saltos) - Diapositiva 6 y 8 del PDF
        // ========================================================================
        boolean huboCambios;
        do {
            huboCambios = false; // Asumimos que no habrá nuevas 'X' en esta vuelta
            
            for (int i = 0; i < estados.size(); i++) {
                for (int j = i + 1; j < estados.size(); j++) {
                    String p = estados.get(i);
                    String q = estados.get(j);
                    Set<String> parActual = new HashSet<>(Arrays.asList(p, q));

                    // Si el par AÚN NO tiene una 'X' (está en blanco en la tabla)
                    if (!paresMarcados.contains(parActual)) {
                        
                        // Probamos con cada letra del alfabeto
                        for (String letra : afd.getAlfabeto()) {
                            String destinoP = obtenerDestinoUnico(afd, p, letra);
                            String destinoQ = obtenerDestinoUnico(afd, q, letra);

                            // Si ambos tienen destino y apuntan a estados diferentes
                            if (destinoP != null && destinoQ != null && !destinoP.equals(destinoQ)) {
                                Set<String> parDestino = new HashSet<>(Arrays.asList(destinoP, destinoQ));
                                
                                // Si el lugar al que saltaron YA TIENE una 'X'
                                if (paresMarcados.contains(parDestino)) {
                                    paresMarcados.add(parActual); // Le ponemos una 'X' a nuestro par actual
                                    huboCambios = true;           // Avisamos que la tabla cambió
                                    break; // Ya lo marcamos, no necesitamos probar más letras para este par
                                }
                            }
                        }
                    }
                }
            }
        } while (huboCambios); // Repetir hasta que ninguna 'X' nueva sea agregada

        // ========================================================================
        // PASO 3: CREAR LAS CLASES DE EQUIVALENCIA (Los Bloques) - Diapositiva 11
        // ========================================================================
        List<Set<String>> bloques = new ArrayList<>();
        
        // Inicialmente, cada estado es su propio bloque aislado
        for (String estado : estados) {
            bloques.add(new HashSet<>(Arrays.asList(estado)));
        }
        
        // Juntamos los bloques de los estados que quedaron SIN MARCAR (son equivalentes)
        for (int i = 0; i < estados.size(); i++) {
            for (int j = i + 1; j < estados.size(); j++) {
                String p = estados.get(i);
                String q = estados.get(j);
                Set<String> parActual = new HashSet<>(Arrays.asList(p, q));
                
                if (!paresMarcados.contains(parActual)) {
                    // Buscamos en qué bloque está 'p' y en cuál está 'q'
                    Set<String> bloqueP = null, bloqueQ = null;
                    for (Set<String> b : bloques) {
                        if (b.contains(p)) bloqueP = b;
                        if (b.contains(q)) bloqueQ = b;
                    }
                    
                    // Si están en bloques separados, los fusionamos
                    if (bloqueP != null && bloqueQ != null && bloqueP != bloqueQ) {
                        bloqueP.addAll(bloqueQ);
                        bloques.remove(bloqueQ);
                    }
                }
            }
        }

        // ========================================================================
        // PASO 4: CONSTRUIR EL NUEVO AUTÓMATA MÍNIMO
        // ========================================================================
        return ensamblarAutomataMinimo(afd, bloques);
    }

    // ========================================================================
    // HERRAMIENTAS DE APOYO (MÉTODOS PRIVADOS)
    // ========================================================================

    // Busca hacia dónde salta un estado con una letra específica (En un AFD solo hay 1 camino)
    private static String obtenerDestinoUnico(Automata afd, String estadoOrigen, String letra) {
        for (Transicion t : afd.getTransiciones()) {
            if (t.getEstadoOrigen().equals(estadoOrigen) && t.getSimbolo().equals(letra)) {
                return t.getEstadoDestino();
            }
        }
        return null; // Si no hay camino (callejón sin salida)
    }

    // Traduce la lista de "Bloques" a un nuevo objeto Automata
    private static Automata ensamblarAutomataMinimo(Automata afdOriginal, List<Set<String>> bloques) {
        Automata minimo = new Automata();
        minimo.setAlfabeto(afdOriginal.getAlfabeto());
        
        // Diccionario para saber cómo se llama cada bloque (ej: "q0,q1")
        Map<Set<String>, String> nombresBloques = new HashMap<>();
        Set<String> nuevosEstados = new HashSet<>();
        Set<String> nuevosFinales = new HashSet<>();
        
        for (Set<String> bloque : bloques) {
            // Ordenamos y unimos los nombres (Ej: {3, 1} -> "1,3")
            List<String> listaOrdenada = new ArrayList<>(bloque);
            Collections.sort(listaOrdenada);
            String nombreFusionado = String.join(",", listaOrdenada);
            
            nombresBloques.put(bloque, nombreFusionado);
            nuevosEstados.add(nombreFusionado);
            
            // Si algún estado de este bloque era Final en el original, todo el bloque es Final
            for (String estado : bloque) {
                if (afdOriginal.getEstadosFinales().contains(estado)) {
                    nuevosFinales.add(nombreFusionado);
                    break;
                }
            }
            
            // Si el bloque contiene al estado inicial antiguo, este bloque es el nuevo Inicial
            if (bloque.contains(afdOriginal.getEstadoInicial())) {
                minimo.setEstadoInicial(nombreFusionado);
            }
        }
        
        // Reconstruir transiciones leyendo solo 1 representante de cada bloque
        List<Transicion> nuevasTransiciones = new ArrayList<>();
        for (Set<String> bloque : bloques) {
            String representante = bloque.iterator().next(); // Tomamos a cualquiera, todos se comportan igual
            String nombreOrigen = nombresBloques.get(bloque);
            
            for (String letra : minimo.getAlfabeto()) {
                String destinoOriginal = obtenerDestinoUnico(afdOriginal, representante, letra);
                
                if (destinoOriginal != null) {
                    // Buscamos en qué bloque quedó el destino
                    for (Set<String> bDestino : bloques) {
                        if (bDestino.contains(destinoOriginal)) {
                            String nombreDestino = nombresBloques.get(bDestino);
                            nuevasTransiciones.add(new Transicion(nombreOrigen, letra, nombreDestino));
                            break;
                        }
                    }
                }
            }
        }
        
        minimo.setEstados(nuevosEstados);
        minimo.setEstadosFinales(nuevosFinales);
        minimo.setTransiciones(nuevasTransiciones);
        
        return minimo;
    }
}