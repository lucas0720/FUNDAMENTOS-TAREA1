package controlador;

import java.util.*;
import modelo.*;
//MAS comentarios al final del codigo , donde apuntamos otras cosas

public class ConvertidorAFNDaAFD {

    private ConvertidorAFNDaAFD() {} // Constructor privado para evitar uso en otra clase

    public static Automata convertir(Automata automataOriginal) {
        
        Map<Set<String>, Map<String, Set<String>>> tablaDeConversion = construirTablaConversion(automataOriginal);// tabla de conversion como la que vimos en clae , donde ponemos los estados en la primera columna , los nuevos estados y las trnasiciones depsues
        Map<Set<String>, String> nombresNuevos = new HashMap<>();
        
        for (Set<String> superEstado : tablaDeConversion.keySet()) { // aqui con keySet , toma super estados , q seria como los estados que quedaron el la tabla de convesion entonces hay estados que quedan juntos tipo {q2, q0, q1}
            
            List<String> listaOrdenada = new ArrayList<>(superEstado); // convertimos el conjunto en una lista asi la ordenamos
            Collections.sort(listaOrdenada); // con esto ordenamos de forma ordenada , para no tener problemas con conjuntos que sean q0,q1 y q1,q0 , que son lo mismo ,
            String nombreFusionado = String.join(",", listaOrdenada);
            
            nombresNuevos.put(superEstado, nombreFusionado);
        }

        // preparar las listas en blanco
        Set<String> nuevosEstados = new HashSet<>(nombresNuevos.values()); // guardamos los nombre de los nombres nuevos aqui
        ArrayList<Transicion> nuevasTransiciones = new ArrayList<>();
        Set<String> nuevosEstadosAceptacion = new HashSet<>();
        
        // estado Inicial
        Set<String> clausuraInicial = EstadosAlcanzables(automataOriginal, automataOriginal.getEstadoInicial());
        String nuevoEstadoInicial = nombresNuevos.get(clausuraInicial);

        // Finales originales
        Set<String> finalesOriginales = automataOriginal.getEstadosFinales();

        //Rellenar transiciones y buscar finales
        //tipo Map.Entry<Set<String>, Map<String, Set<String>>> :
        /* El Map.Entry <clave , valor> 
           la clave es un set String porque representa los estados de automata que aveces pueden estar juntos,
           el valor quedo como un Map, porque el String es la letra del alfabeto de la tabla, y el Set<String> seria el estado hacia donde llega
           Map.Entry , porque es la forma de tomar como ambos valores la llave y su valor entonces vamor recorrendo todo con esos metodos
         */

        for (Map.Entry<Set<String>, Map<String, Set<String>>> fila : tablaDeConversion.entrySet()) {
            Set<String> superEstadoOrigen = fila.getKey();
            String nombreOrigen = nombresNuevos.get(superEstadoOrigen);

            
            for (String estado : superEstadoOrigen) {
                if (finalesOriginales.contains(estado)) {
                    nuevosEstadosAceptacion.add(nombreOrigen);
                    break;
                }
            }

            // crear flechas
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

        //Armar y entregar
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

            Set<String> = Estadonuevo del ADF conseguirdo en la linea 

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
        Set<String> alfabetoOriginal = new HashSet<>(automata.getAlfabeto());
        alfabetoOriginal.removeIf(simbolo -> esEpsilon(simbolo));
        
        return alfabetoOriginal;
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
 *  
 * ----- METODO ----- public static Automata convertir(Automata automataOriginal) {
 * 
 * Es el metodo principal de la clase donde buscmaos replicar el paso a paso del procedimiento que vimos en clase donde buscamos retornar un automata osea
 * recibe un automada AFND y lo transforma a un AFD , funicona en base a muchos metodos privados para no volverse tan extenso
 * 
 * tablaDeConverison: aqui recuperamos la tabla de conversion , la tabla donde tomamos los estados en una columna y depsues en las transicicones vamos pasando
 * a otros estados , asi descubirendo los nuevos estados
 * 
 * nombre nuevos: aqui guardamos los nuevos nombres de los estados, pra el nuvo AFD
 * 
 * for (Set<String> superEstado : tablaDeConversion.keySet()) {:rcuperamos el conjunto de estados ahora qe estan en la primera columna ,
 * y los ordenamos en la nueva liesta en el formato que mas nos acomodaba , no fucionando los estaods en uno nuevos sino que , separandlos con una ,  
 * 
 *  
 * List<String> listaOrdenada = new ArrayList<>(superEstado); eso es para convertirlo en un tipo lista para ordenarlo
 * 
 * Collections.sort(listaOrdenada); con esto ordenamos de forma ordenada , para no tener problemas con conjuntos que sean q0,q1 y q1,q0 , que son lo mismo ,
 * habiamos tenido el problema por esto asique buscamos como arreglarlo y con essto lo logramos
 * 
 * String nombreFusionado = String.join(",", listaOrdenada);
 * aqui solo convertimos la lista en un String , y separamos cada elemento de la lista con una , 
 * asi logrmaos el formato que queriamos separando por q0 , q1 ,q2
 * 
 * nombresNuevos.put(superEstado, nombreFusionado);
 * aqui usamos la estructura HashMap para poder guardar de forma organizada, donde un super estado queda al inicio yle damos el nombre que uraemos
 * 
 * 
 *  
 *    
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
 */