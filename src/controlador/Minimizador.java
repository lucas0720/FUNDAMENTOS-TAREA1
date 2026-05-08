package controlador;

import modelo.*;
import java.util.*;

public class Minimizador {

    public static Automata minimizar(Automata afd) {
        // 1. COMPLETAR EL AUTOMATA (Estado Trampa) - Paso 1 del PDF
        Automata afdCompleto = completarAutomata(afd);
        
        List<String> estados = new ArrayList<>(afdCompleto.getEstados());
        Set<String> finales = afdCompleto.getEstadosFinales();
        Set<Set<String>> paresMarcados = new HashSet<>();

        // CASO BASE: Final vs No-Final (Pág 4 del PDF)
        for (int i = 0; i < estados.size(); i++) {
            for (int j = i + 1; j < estados.size(); j++) {
                if (finales.contains(estados.get(i)) != finales.contains(estados.get(j))) {
                    paresMarcados.add(crearPar(estados.get(i), estados.get(j)));
                }
            }
        }

        // PASO INDUCTIVO: Llenado de tabla (Pág 6 del PDF)
        boolean cambio;
        do {
            cambio = false;
            for (int i = 0; i < estados.size(); i++) {
                for (int j = i + 1; j < estados.size(); j++) {
                    String p = estados.get(i);
                    String q = estados.get(j);
                    if (!paresMarcados.contains(crearPar(p, q))) {
                        for (String letra : afdCompleto.getAlfabeto()) {
                            String dp = obtenerDestino(afdCompleto, p, letra);
                            String dq = obtenerDestino(afdCompleto, q, letra);
                            
                            // Si los destinos son diferentes y ya están marcados como distintos
                            if (!dp.equals(dq) && paresMarcados.contains(crearPar(dp, dq))) {
                                paresMarcados.add(crearPar(p, q));
                                cambio = true;
                                break;
                            }
                        }
                    }
                }
            }
        } while (cambio);

        // AGRUPAR Y ARMAR (Pág 11 del PDF)
        return construirMinimo(afdCompleto, estados, paresMarcados);
    }

    private static Automata completarAutomata(Automata original) {
        Automata copia = new Automata(new HashSet<>(original.getEstados()), new HashSet<>(original.getAlfabeto()), 
                         original.getEstadoInicial(), new HashSet<>(original.getEstadosFinales()), 
                         new ArrayList<>(original.getTransiciones()));
        
        String TRAMPA = "T";
        boolean necesitaTrampa = false;

        for (String e : copia.getEstados()) {
            for (String letra : copia.getAlfabeto()) {
                if (obtenerDestino(copia, e, letra).equals("NULL")) {
                    copia.getTransiciones().add(new Transicion(e, letra, TRAMPA));
                    necesitaTrampa = true;
                }
            }
        }

        if (necesitaTrampa) {
            copia.getEstados().add(TRAMPA);
            for (String letra : copia.getAlfabeto()) {
                copia.getTransiciones().add(new Transicion(TRAMPA, letra, TRAMPA));
            }
        }
        return copia;
    }

    private static String obtenerDestino(Automata a, String origen, String letra) {
        for (Transicion t : a.getTransiciones()) {
            if (t.getEstadoOrigen().equals(origen) && t.getSimbolo().equals(letra)) return t.getEstadoDestino();
        }
        return "NULL";
    }

    private static Set<String> crearPar(String a, String b) {
        return new HashSet<>(Arrays.asList(a, b));
    }

    private static Automata construirMinimo(Automata afd, List<String> estados, Set<Set<String>> marcados) {
        // Lógica de unión de bloques (Clases de equivalencia)
        List<Set<String>> bloques = new ArrayList<>();
        for (String e : estados) {
            boolean agregado = false;
            for (Set<String> b : bloques) {
                String representante = b.iterator().next();
                if (!marcados.contains(crearPar(e, representante))) {
                    b.add(e);
                    agregado = true;
                    break;
                }
            }
            if (!agregado) bloques.add(new HashSet<>(Collections.singletonList(e)));
        }

        // =========================================================
        // AQUÍ ESTÁ LA MAGIA "HUMANA" PARA LOS NOMBRES
        // =========================================================
        Map<Set<String>, String> nombres = new HashMap<>();
        int contador = 1; // Empezamos en 1 porque el 0 está reservado para el inicial

        for (Set<String> b : bloques) {
            if (b.contains(afd.getEstadoInicial())) {
                nombres.put(b, "q0"); // El estado inicial SIEMPRE se llamará q0
            } else {
                nombres.put(b, "q" + contador); // Los demás serán q1, q2, q3...
                contador++;
            }
        }

        // Reconstruir transiciones finales
        Automata min = new Automata();
        min.setAlfabeto(afd.getAlfabeto());
        
        for (Set<String> b : bloques) {
            String nom = nombres.get(b);
            min.getEstados().add(nom);
            
            if (b.contains(afd.getEstadoInicial())) min.setEstadoInicial(nom);
            
            for (String e : b) {
                if (afd.getEstadosFinales().contains(e)) {
                    min.getEstadosFinales().add(nom);
                    break;
                }
            }
            for (String letra : min.getAlfabeto()) {
                String destOrig = obtenerDestino(afd, b.iterator().next(), letra);
                for (Set<String> bDest : bloques) {
                    if (bDest.contains(destOrig)) {
                        min.getTransiciones().add(new Transicion(nom, letra, nombres.get(bDest)));
                        break;
                    }
                }
            }
        }
        return min;
    }
}