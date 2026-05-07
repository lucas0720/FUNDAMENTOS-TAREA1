package modelo;
public class Transicion {
    
    //ATRIBUTOS
    private String estadoOrigen;
    private String simbolo;
    private String estadoDestino;

    //CONSTRUCTOR
    public Transicion(String estadoOrigen, String simbolo, String estadoDestino) {
        this.estadoOrigen = estadoOrigen;
        this.simbolo = simbolo;
        this.estadoDestino = estadoDestino;
    }

    //GETTERS Y SETTERS
    public String getEstadoOrigen() {
        return estadoOrigen;
    }

    public void setEstadoOrigen(String estadoOrigen) {
        this.estadoOrigen = estadoOrigen;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getEstadoDestino() {
        return estadoDestino;
    }

    public void setEstadoDestino(String estadoDestino) {
        this.estadoDestino = estadoDestino;
    }

    @Override
    public boolean equals(Object objeto) {

        if(this  == objeto){
            return true;
        }
        if(objeto == null || !(objeto instanceof Transicion)){
            return false;
        }

        Transicion transicion = (Transicion) objeto;

        boolean MismoOrigen = this.estadoOrigen.equals(transicion.getEstadoOrigen());
        boolean MismoSimbolo = this.simbolo.equals(transicion.getSimbolo());
        boolean MismoDestino = this.estadoDestino.equals(transicion.getEstadoDestino());

        return MismoOrigen && MismoSimbolo && MismoDestino;
    }


    @Override
    public String toString() {
        return "(" + estadoOrigen + ", " + simbolo + ", " + estadoDestino + ")";
    }
}
