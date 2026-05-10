package controlador;

import modelo.Transicion;
import modelo.Automata;

import java.util.HashMap;
import java.util.Map;

//NOTAS DE LA CLASE AL FINAL DE ELLA
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
            //                      q0       -    a =      q0-a
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
        boolean esEpsilon = s.equals("epsilon") || s.equals("eps")  || s.equals("ε");

        return esEpsilon;
    }
}

/*
    1. ==== CLASE ESTRUCTURA ====
    Aqui cree la clase con el contructor privado porque no vimos que fuerea necesario crear objetos con esta clas
    porque al ser solo algo de analisis , entonces lo pusimos privado y los metodos estaticos asi los podemos llamar
    directo de la clase

    2. === Funcionalidades ====

    -------- METODODo ---------- public static boolean esAFND(Automata automata) {

    Es el metodo que cumpliria el obejtio uno que nos pidieron , donde analizamos si es o no un AFD, y lo usaremos
    para pasar al siguiente objetivo donde debemos de transformarlo de un AFND a un AFD

    registroCaminos: Aqui guardamos los camnos que hemos pasado , donde lo guardamos por pares, para que sea mas facil
    odespues identificar si esque tenia mas de un camino o no solo buscando el primer par , y si vemos que ya existia 
    en un esatado y apunta a otro estado distitno , entonces retornamos que es un afd

    ------- Metodo -------- private static boolean esEpsilon(String simbolo) {

    No sabemos que se aceptara como epsilon , pero pusimos todas las posibilidades para ir probadno y este metodo es para eso
    para simplemente ver si es epsilon o no

    Actualizacion: nos dijeron que sera eps , lo agregamos pero igual mantenemos las demas formas por precaucion
    


*/