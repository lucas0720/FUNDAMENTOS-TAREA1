package controlador;

import java.util.*;
import modelo.*;

public class ConvertidorAFNDaAFD {

    private ConvertidorAFNDaAFD() {} // Constructor privado para evitar uso en otra clase

    public static Automata convertir(Automata automataOriginal) {
        
        Map<Set<String>, Map<String, Set<String>>> tablaDeConversion = construirTablaConversion(automataOriginal);
        Map<Set<String>, String> nombresNuevos = new HashMap<>();
        
        for (Set<String> superEstado : tablaDeConversion.keySet()) {
            
            List<String> listaOrdenada = new ArrayList<>(superEstado);
            Collections.sort(listaOrdenada); 
            String nombreFusionado = String.join(",", listaOrdenada);
            
            nombresNuevos.put(superEstado, nombreFusionado);
        }

        // 3. Preparar las listas en blanco
        Set<String> nuevosEstados = new HashSet<>(nombresNuevos.values());
        ArrayList<Transicion> nuevasTransiciones = new ArrayList<>();
        Set<String> nuevosEstadosAceptacion = new HashSet<>();
        
        // 4. Estado Inicial
        Set<String> clausuraInicial = EstadosAlcanzables(automataOriginal, automataOriginal.getEstadoInicial());
        String nuevoEstadoInicial = nombresNuevos.get(clausuraInicial);

        // 5. Finales originales
        Set<String> finalesOriginales = automataOriginal.getEstadosFinales();

        // 6. Rellenar transiciones y buscar finales
        for (Map.Entry<Set<String>, Map<String, Set<String>>> fila : tablaDeConversion.entrySet()) {
            Set<String> superEstadoOrigen = fila.getKey();
            String nombreOrigen = nombresNuevos.get(superEstadoOrigen);

            // A) ¿Es de aceptación?
            for (String estado : superEstadoOrigen) {
                if (finalesOriginales.contains(estado)) {
                    nuevosEstadosAceptacion.add(nombreOrigen);
                    break;
                }
            }

            // B) Crear flechas
            Map<String, Set<String>> saltos = fila.getValue();
            for (Map.Entry<String, Set<String>> salto : saltos.entrySet()) {
                String letra = salto.getKey();
                Set<String> superEstadoDestino = salto.getValue();
                
                if (!superEstadoDestino.isEmpty()) {
                    String nombreDestino = nombresNuevos.get(superEstadoDestino);
                    nuevasTransiciones.add(new Transicion(nombreOrigen, letra, nombreDestino));
                }
            }
        }

        // 7. Armar y entregar
        Automata nuevoAFD = new Automata();
        nuevoAFD.setEstados(nuevosEstados);
        nuevoAFD.setAlfabeto(obtenerAlfabetoSinEpsilon(automataOriginal));
        nuevoAFD.setEstadoInicial(nuevoEstadoInicial);
        nuevoAFD.setEstadosFinales(nuevosEstadosAceptacion);
        nuevoAFD.setTransiciones(nuevasTransiciones);

        return nuevoAFD;
    }

    //CREAR TABLA DE CLAUSURA
    private static Map<String, Set<String>> construirTablaClausura(Automata automata) {
        // Estados  Estados Alcanzables por epsilon
        Map<String, Set<String>> TablaClausura = new HashMap<>();
        Set<String> estados = automata.getEstados();

        for (String e : estados) {

            Set<String> estadosClausura = EstadosAlcanzables(automata, e);
            TablaClausura.put(e, estadosClausura);
        }
        return TablaClausura;
    }

    private static Map<Set<String>, Map<String, Set<String>>> construirTablaConversion(Automata automata) {
        //Obtenemos datos de los metodos privados
        Map<String, Set<String>> tablaClausuras = construirTablaClausura(automata);//CONTRUIMOS LA TABLA CALAUSURA PARA OBTENER LOS E(q) DE CADA ESTADO DEL AFND
        Set<String> alfabeto = obtenerAlfabetoSinEpsilon(automata);                //OBTENEMOS EL ALFABETO SIN EPSILON PARA SABER LAS LETRAS POR LAS QUE DEBEMOS MOVERNOS EN LA CONVERSION

        Map<Set<String>, Map<String, Set<String>>> tablaConversion = new HashMap<>();// LA tabla final , Aqui guaradermos todos los datos de la conversion     
        
        //Herramientas para recoorre la tablas
        Stack<Set<String>> pilaNuevoEstados = new Stack<>();    //Pila donde guardaremos los NuevoEstados que encontremos pero que no se analizan aun
        Set<Set<String>> estadosDescubiertos = new HashSet<>(); //Es para guardar los que hemos descubierto pero que aún no hemos procesado

        String estadoInicial = automata.getEstadoInicial();// buscamos el primer estado pra ocuparlo abajo
        Set<String> primerNewEstado = tablaClausuras.get(estadoInicial);// ya tenemos el primer estado buscamos su E(q) y asi lo ocupamos como el primer estado de la conversion

        pilaNuevoEstados.add(primerNewEstado);  //Es un nuevo estado a procesar por eso lo ponemos en la pila // linea 97
        estadosDescubiertos.add(primerNewEstado);// es un estado que ya vimos o procesamos , y obvio ya esta en nuevoAFD , ENTONCES lo ponemos

        while (!pilaNuevoEstados.isEmpty()) {// recorremos todos los estados que vayamos descubriendo por la pila , hasta que no haya mas estados por descubrir
            Set<String> NuevosEstados  = pilaNuevoEstados.pop();     // AQUI la lisa de los conjuntos de estados que se forman
            Map<String, Set<String>> filaTransiciones = new HashMap<>();

            for (String letra : alfabeto) { // por cada letra del aflabeto buscamos armar nuevos estados , recorremos todas las letras del alfabaeto por cada estado de la pila

                Set<String> destinosDirectos = mover(automata, NuevosEstados, letra);// un super estado seria un estado que puede esar compuesto por mas estados
                Set<String> clausuraEstadosq = new HashSet<>();

                for (String estadoLlegada : destinosDirectos) {
                    clausuraEstadosq.addAll(tablaClausuras.get(estadoLlegada));// como tenemos los estados ahora elegimos en la tabla de clausura , suss E(q) y los añadimos
                }

                filaTransiciones.put(letra, clausuraEstadosq); //Guradamos en el HashMap la letra y su estado de clausura

                if (!clausuraEstadosq.isEmpty() && !estadosDescubiertos.contains(clausuraEstadosq)) {

                    pilaNuevoEstados.add(clausuraEstadosq); 
                    estadosDescubiertos.add(clausuraEstadosq); 
                }
            }
            tablaConversion.put(NuevosEstados, filaTransiciones);
        /*
        Map<Set<String>, Map<String, Set<String>>>

            Set<String> = Estadonuevo del ADF conseguirdo en la linea , en la fila 97 donde fuimos fuardanso los nuevos Estados que se van descubriendo por la pila

            Map<String, Set<String>> 
            
                String = letra del alfabeto por la que se mueve el AFD en el for de la line113 
                Set<String> = el nuevo EL destino, conseguido por las clausuras por cada letra , y en cada estado 

        */
        }
        return tablaConversion; 
    }

    //BUSCAR LOS E(q) de la tabla 
    private static Set<String> EstadosAlcanzables(Automata automata, String estado){
        
        Set<String> estadosAlcanzables = new HashSet<>(); 
        Stack<String> pila = new Stack<>(); 

        estadosAlcanzables.add(estado);//CONJUNTO DE ESTADOS EN E(q) 
        pila.add(estado);//Estados por procesar

        //ESTADOS POR PROCESAR Y AGREGADOS EN estadosAlcanzables
        while (!pila.isEmpty()) {

            String estadoActual = pila.pop();

            for (Transicion t : automata.getTransiciones()) {

                //si fuera (q1,epsilon,q2)
                String origenBuscado = t.getEstadoOrigen();  //q1
                String simboloBuscado = t.getSimbolo();      //epsilon
                String destinoBuscado = t.getEstadoDestino();//q2

                if(estadoActual.equals(origenBuscado) ) { // si el estado actual es el origen de la transicion entonces la revisamos

                    if(esEpsilon(simboloBuscado)){// no sabuamos que caracter epslon podia tener el automata asique pusimos varias posibilidades

                        if (!estadosAlcanzables.contains(destinoBuscado)) { // Si el estado destino no lo hemos visto antes lo agreamo
                            estadosAlcanzables.add(destinoBuscado);

                            pila.add(destinoBuscado);
                        }
                    }
                }
            }
        }

        return estadosAlcanzables; // todos los datos que puede alcanzar con epslon desde el estado dado
    }

    private static Set<String> mover(Automata automata, Set<String> superEstado, String letraBuscada) {
        Set<String> destinosAlcanzados = new HashSet<>();// esta vacia , como no acpeta repetidos si tenemos alto tipo q0 a q1 a q2 a qo No hay problema

        for (String estado : superEstado) {// por cada super estado vemos revisamos cada esado que pueda tener individualemnte, para revisar

            for (Transicion t : automata.getTransiciones()) {

                if (estado.equals(t.getEstadoOrigen()) && letraBuscada.equals(t.getSimbolo())) {
                    destinosAlcanzados.add(t.getEstadoDestino());
                }
            }
        }
        return destinosAlcanzados;
    }

    private static Set<String> obtenerAlfabetoSinEpsilon(Automata automata) {
        Set<String> alfabeto = new HashSet<>();
        for (Transicion t : automata.getTransiciones()) {
            if (!esEpsilon(t.getSimbolo())) {
                alfabeto.add(t.getSimbolo());
            }
        }
        return alfabeto;
    }

    private static boolean esEpsilon(String simbolo) {
        String s = simbolo.toLowerCase();
        boolean esEpsilon = s.equals("epsilon") || s.equals("eps") || s.equals("e") || s.equals("ε");

        return esEpsilon;
    }
}

/**
 * 1. ==== CLASE ESTRUCTURA =====
 * Se creo la clase contructor privadado para no crear objetos de la clase, ya que no esta pensada para mover datos sino para realizar operaciones logicas
 * Usamos una estructura con metodos privados estaticos, ya que no necesitamos mantener estado interno ni acceder a atributos de instancia, solo queremos 
 * realizar operaciones sobre los datos que se le pasan como parametros, entonces podememos ocupar los metodos directamente de la clase con el static , asi 
 * tiene un funcionamiento como el Math.random() , donde se llama desde la clase misma.
 * 
 * 2. ==== FUNCIONALIDADES ====
 * 
 *    ----- METODO ----- clausura(Automata automata, Set<String> estados):
 * 
 *    ----- METODO ----- clausuraEpsilonIndividual(Automata automata, String estado): 
 *
 * clausuraEpsilon: lo ocuparemos para guardar los estados que iran en E(q), no pueden ir repetidos por eso usamos HashSet 
 * en caso de que el camino epsilon apunte nuevamente al estado del que salio , asi nos quitamos un problema de logica.
 * 
 * pila: la usamos para llevar un control de los estados que faltan por procesar, es decir, aquellos a los que llegamos por 
 * epsilon pero no hemos revisado sus transiciones epsilon, asi evitamos problemas de ciclos con epsilon.
 * 
 * while (!pila.isEmpty()): para procesar los estados alcanzables por epsilon, sacamos un estado de la pila.
 * 
 * 
 */