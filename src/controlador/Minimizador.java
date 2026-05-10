package controlador;

import modelo.*;
import java.util.*;
//Hay apuntes extra alfinal de la clse
public class Minimizador {

    public static Automata minimizar(Automata AutomataAFD) {
        //COMLETAMOS EL AUOMATA , si no lo esta
        Automata AutomataAFDCompleto = completarAutomata(AutomataAFD); // en caso de que no este completo , porque cuando pasabamos un afnd a afd solo veirifcamos si tenia epsilon pero no si tenia transiciones faltantes :c
        
        List<String> estados = new ArrayList<>(AutomataAFDCompleto.getEstados());
        Set<String> finales = AutomataAFDCompleto.getEstadosFinales();
        Set<Set<String>> paresMarcados = new HashSet<>();

        for (int i = 0; i < estados.size(); i++) {

            for (int j = i + 1; j < estados.size(); j++) {

                if (finales.contains(estados.get(i)) != finales.contains(estados.get(j))) {// si son distitnos se marca , igual que en la tabla
                    paresMarcados.add(crearPar(estados.get(i), estados.get(j)));// esto seria el acto de cuando ponemo x , en abal 
                }
            }
        }

        boolean cambio;
        do {// AQUI cada vez que el la tabla se tacha revisamos todo denuevo , en caso de que haya cambiado algo
            cambio = false;

            for (int i = 0; i < estados.size(); i++) {

                for (int j = i + 1; j < estados.size(); j++) {

                    String p = estados.get(i);
                    String q = estados.get(j);

                    if (!paresMarcados.contains(crearPar(p, q))) { // aqui si ya estan tachado los ignora

                        for (String letra : AutomataAFDCompleto.getAlfabeto()) {

                            String destinop = obtenerDestino(AutomataAFDCompleto, p, letra);
                            String destinoq = obtenerDestino(AutomataAFDCompleto, q, letra);

                            // Si los destinos son diferentes y ya están marcados como distintos
                            if (!destinop.equals(destinoq) && paresMarcados.contains(crearPar(destinop, destinoq))) {

                                paresMarcados.add(crearPar(p, q));
                                cambio = true;
                                break;
                            }
                        }
                    }
                }
            }
        } while (cambio);

        return construirMinimo(AutomataAFDCompleto, estados, paresMarcados);
    }

    private static Automata completarAutomata(Automata original) { // Ocupamos  este metodo para los casos dodonde un estado le falta un camino de la letra del alfabeto
        Set<String> estadosCopia = new HashSet<>(original.getEstados());
        Set<String> alfabetoCopia = new HashSet<>(original.getAlfabeto());
        String inicialCopia = original.getEstadoInicial();
        Set<String> finalesCopia = new HashSet<>(original.getEstadosFinales());
        List<Transicion> transicionesCopia = new ArrayList<>(original.getTransiciones());

        Automata copia = new Automata(estadosCopia, alfabetoCopia, inicialCopia, finalesCopia, transicionesCopia);
        
        String Sumidero = "sumidero";
        boolean necesitaSumidero = false;

        Set<String> recorridoDeEstados = copia.getEstados();// todos los estados que vamos a revisar
        for (String e : recorridoDeEstados) {

            Set<String>  recorridoDeAlfabeto = copia.getAlfabeto(); // todas las letras del alfabeto que vamos a revisar

            for (String letra : recorridoDeAlfabeto) {

                String destino = obtenerDestino(copia, e, letra);
                if (destino.equals("NULL")) {
                    copia.getTransiciones().add(new Transicion(e, letra, Sumidero));
                    necesitaSumidero = true;
                }
            }
        }

        //si necesita sumidero , entonce a el estado le agregamos la nueva transicion
        if (necesitaSumidero) {

            Set<String> estados = copia.getEstados();
            estados.add(Sumidero);
            Set<String> recorridoDeAlfabeto = copia.getAlfabeto();

            for (String letra : recorridoDeAlfabeto) {

                copia.getTransiciones().add(new Transicion(Sumidero, letra, Sumidero));//agregamos a un esado su camino sumidero
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
    
    //METODO auxiliar  , algo chiquito
    private static Set<String> crearPar(String estado1, String estado2) {
        Set<String> pareja = new HashSet<>();

        pareja.add(estado1);
        pareja.add(estado2);
        
        return pareja;

    }

    // logica de unión de bloques, en el siguiente metodo nos ayudamos de inteligencia artificial , porque fue dificil imaginar el algoritom
    //o pensar es partes , claro que si influenciamos mucho en su funcionamiento y como lo queriamos , pero lo ocupamos para corregir nuestros erores
    
    private static Automata construirMinimo(Automata original, List<String> todosLosEstados, Set<Set<String>> paresTachados) {
        
        List<Set<String>> bolsasDeClones = new ArrayList<>();// aqui cada set , es una bolsa , donde tendra los nombres de los estaods de automata viejo y el list , sera para guardar ordenadaemnte estos set
        
        // Tomamos los estados sueltos del autómata viejo uno por uno
        for (String estadoActual : todosLosEstados) {
            boolean encontroSuBolsa = false;
            
            for (Set<String> bolsa : bolsasDeClones) {
                String representante = bolsa.iterator().next();
                
                // Le preguntamos al cuaderno: de si hay una "x" entre el estado actual y este representante
                // Si NO hay una X (!contains), significa que son idénticos

                if (!paresTachados.contains(crearPar(estadoActual, representante))) {
                    bolsa.add(estadoActual);
                    encontroSuBolsa = true;
                    break;
                }
            }

            // Si lo comparamos con todas las bolsas y no encajó en ninguna, 
            // significa que hace algo único. Le creamos su propia bolsa solitaria.
            if (!encontroSuBolsa) {
                bolsasDeClones.add(new HashSet<>(Collections.singletonList(estadoActual)));
            }
        }

        Map<Set<String>, String> etiquetas = new HashMap<>();
        int contador = 1; 

        for (Set<String> bolsa : bolsasDeClones) {
            // La bolsa que tenga al estado inicial original, siempre será el nuevo "q0"
            if (bolsa.contains(original.getEstadoInicial())) {
                etiquetas.put(bolsa, "q0"); 
                
                //rescatamos nuestro estado trampa para que mantenga su nombre en el dibujo
            } else if (bolsa.contains("Sumidero")) {
                etiquetas.put(bolsa, "Sumidero"); 
                
                //a todas las demás bolsas las enumeramos en orden (q1, q2, q3...)
            } else {
                etiquetas.put(bolsa, "q" + contador);
                contador++;
            }
        }

        Automata automataMinimizado = new Automata();
        automataMinimizado.setAlfabeto(original.getAlfabeto());
        
        //ahora convertimos cada "bolsa" en un Círculo Oficial del nuevo autómata
        for (Set<String> bolsa : bolsasDeClones) {
            
            String nombreNuevo = etiquetas.get(bolsa);
            automataMinimizado.getEstados().add(nombreNuevo);
    
            if (bolsa.contains(original.getEstadoInicial())) {
                automataMinimizado.setEstadoInicial(nombreNuevo);
            }
            

            for (String estadoAntiguo : bolsa) {
                if (original.getEstadosFinales().contains(estadoAntiguo)) {
                    automataMinimizado.getEstadosFinales().add(nombreNuevo);
                    break; 
                }
            }
            
            for (String letra : automataMinimizado.getAlfabeto()) {
                
                String representante = bolsa.iterator().next();
                String destinoAntiguo = obtenerDestino(original, representante, letra);
                
                
                for (Set<String> bolsaDestino : bolsasDeClones) {
                    if (bolsaDestino.contains(destinoAntiguo)) {
                        
                        String nombreDestinoFinal = etiquetas.get(bolsaDestino);
                        
                       
                        Transicion nuevaFlecha = new Transicion(nombreNuevo, letra, nombreDestinoFinal);
                        automataMinimizado.getTransiciones().add(nuevaFlecha);
                        break;
                    }
                }
            }
        }
        
        return automataMinimizado;
    }
}
/**
 * 1. ==== CLASE ESTRUCTURA =====
 * Se creó la clase con un constructor privado para no permitir la creación de objetos (instancias) de esta clase, 
 * ya que no está pensada para almacenar datos, sino para realizar operaciones lógicas y matemáticas.
 * Usamos una estructura con métodos privados y públicos estáticos (static), ya que no necesitamos mantener 
 * un estado interno. Al igual que la clase Math de Java, podemos llamar a su funcionalidad directamente 
 * usando Minimizador.minimizar(...) desde cualquier parte del programa.
 * 
 *       2. ==== FUNCIONALIDADES ====
 *  ----- METODO PRINCIPAL ----- public static Automata minimizar(Automata automataAFD):
 * 
 *  Recibe un AFD y aplica el Algoritmo de Llenado de Tabla para reducirlo.
 * 
 * - completarAutomata: Lo primero que hace es asegurar que el autómata no tenga huecos para evitar errores matemáticos.
 * 
 * - paresMarcados (La Tabla): Funciona como nuestro cuaderno. Si dos estados demuestran ser diferentes, 
 * los agrupamos con crearPar() y los metemos aquí (equivalente a poner una "X" en la tabla).
 * 
 * - Caso Base (for anidados): Compara todos los estados de a dos. Si uno es de aceptación (Final) y el otro no, 
 * los marca inmediatamente como distintos.
 * 
 * - Paso Inductivo (do-while): El motor del algoritmo. toma pares de estados que aún no están tachados y los hace 
 * viajar con cada letra del alfabeto. Si sus destinos terminan en un par que YA estaba tachado en nuestra tabla, 
 * entonces estos estados también son distintos y se tachan. Se repite hasta que no haya ningún cambio nuevo.
 * 
 * 
 *  ----- METODO ----- completarAutomata(Automata original)
 * Actúa como inspector de calidad. Recorre cada estado y prueba cada letra del alfabeto. 
 * Si detecta que a un estado le falta una flecha (camino "NULL"), crea automáticamente un estado trampa 
 * llamado "Sumidero" y dirige todas las flechas faltantes hacia allí, creando un bucle infinito en él.
 * 
 *  ----- METODO ----- construirMinimo(Automata original, List<String> todosLosEstados, Set<Set<String>> paresTachados)
 * Ensambla el autómata final usando los resultados de la tabla matemática.
 * * - bolsasDeClones: Agrupa en una misma bolsa (Set) a los estados que nunca fueron tachados en la tabla 
 * (es decir, resultaron ser equivalentes o clones).
 * 
 * 
 *  - etiquetas (Magia Humana): Recorre las bolsas y les asigna nombres limpios para el dibujo final. 
 * La bolsa con el inicio será "q0", el "Sumidero" mantiene su nombre, y el resto se numera secuencialmente (q1, q2..,)
 * 
 *  - Reconstrucción: convierte cada bolsa en un nuevo súper-estado del AFD minimizado. Para reconectar las flechas, 
 * toma un representante de la bolsa, mira a dónde viajaba en el autómata viejo, y dirige la nueva flecha 
 * hacia la bolsa que contiene ese destino antiguo.
 */