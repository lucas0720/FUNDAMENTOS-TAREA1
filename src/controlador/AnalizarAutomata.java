package controlador;

import modelo.Transicion;
import modelo.Automata;

import java.util.HashMap;
import java.util.Map;

public class AnalizarAutomata {
    
    private AnalizarAutomata() {}

    
    public static boolean esAFND(Automata automata) {

        Map<String, String> registroCaminos = new HashMap<>();

        for(Transicion transicion : automata.getTransiciones()) {

            //EJEMPLO DE TRANSICION: q0 - a -> q1
            String origen = transicion.getEstadoOrigen();  //q0
            String simbolo = transicion.getSimbolo();      //a
            String destino = transicion.getEstadoDestino();//q1

            if(esEpsilon(simbolo) == true) {
                return true;
            }
            //                      q0       -    a = q0-a
            String claveBusqueda = origen + "-" + simbolo;

            if (registroCaminos.containsKey(claveBusqueda)) {
                
                String destinoAnterior = registroCaminos.get(claveBusqueda);
                
                if (!destinoAnterior.equals(destino)) {
                    return true; // Ess AFND porque el camino llevaba a 2 estados distintos 
                }
            } else {
                registroCaminos.put(claveBusqueda, destino);
            }
        }
        
        return false;
    }

    private static boolean esEpsilon(String simbolo) {
        String s = simbolo.toLowerCase();
        boolean esEpsilon = s.equals("epsilon") || s.equals("eps") || s.equals("e") || s.equals("ε");

        return esEpsilon;
    }
}