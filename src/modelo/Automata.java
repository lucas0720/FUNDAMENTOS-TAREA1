package modelo;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Automata {

    //ATRIBUTOS
    private Set<String> estados;
    private Set<String> alfabeto;
    private Set<String> estadosFinales;

    private String estadoInicial;

    //RELACION
    private List<Transicion> transiciones;

    //CONSTRUCTORES
    public Automata() {
        this.estados = new HashSet<>();
        this.alfabeto = new HashSet<>();
        this.estadosFinales = new HashSet<>();
        this.transiciones = new ArrayList<>();
        this.estadoInicial = "";
    }

    public Automata(Set<String> estados, Set<String> alfabeto, String estadoInicial, Set<String> estadosFinales, List<Transicion> transiciones) {
        this.estados = estados;
        this.alfabeto = alfabeto;
        this.estadoInicial = estadoInicial;
        this.estadosFinales = estadosFinales;
        this.transiciones = transiciones;
    }

    //GETTERS Y SETTERS
    public Set<String> getEstados() {
        return estados;
    }

    public void setEstados(Set<String> estados) {
        this.estados = estados;
    }

    public Set<String> getAlfabeto() {
        return alfabeto;
    }

    public void setAlfabeto(Set<String> alfabeto) {
        this.alfabeto = alfabeto;
    }

    public String getEstadoInicial() {
        return estadoInicial;
    }

    public void setEstadoInicial(String estadoInicial) {
        this.estadoInicial = estadoInicial;
    }

    public Set<String> getEstadosFinales() {
        return estadosFinales;
    }

    public void setEstadosFinales(Set<String> estadosFinales) {
        this.estadosFinales = estadosFinales;
    }

    public List<Transicion> getTransiciones() {
        return transiciones;
    }

    public void setTransiciones(List<Transicion> transiciones) {
        this.transiciones = transiciones;
    }

   @Override
    public boolean equals(Object objeto) {

        if(this  == objeto){
            return true;
        }   

        if(objeto == null || !(objeto instanceof Automata)){
            return false;
        }

        Automata automata = (Automata) objeto;
        boolean mismosEstados = this.estados.equals(automata.getEstados());
        boolean mismoAlfabeto = this.alfabeto.equals(automata.getAlfabeto());
        boolean mismoEstadoInicial = this.estadoInicial.equals(automata.getEstadoInicial());
        boolean mismosEstadosFinales = this.estadosFinales.equals(automata.getEstadosFinales());
       
        boolean mismasTransiciones = false;
        if (this.transiciones.size() == automata.getTransiciones().size()) {
            mismasTransiciones = this.transiciones.containsAll(automata.getTransiciones());
        }
        return mismosEstados && mismoAlfabeto && mismoEstadoInicial && mismosEstadosFinales && mismasTransiciones;
    }

    @Override
    public String toString() {
        return "Automata {\n" + "  Estados (K) = " + estados + "\n" + "  Alfabeto (Sigma) = " + alfabeto + "\n" + "  Estado Inicial (s) = '" + estadoInicial + "'\n" + "  Estados Finales (F) = " + estadosFinales + "\n" + "  Transiciones = " + transiciones.size() + " registradas\n" +"}";
    }
}
//HOLA PANCHO
// ola luk 