package gestorDeProtocolos;

import java.io.Serializable;

/**
 * Clase que implementa los métodos necesarios para las respuestas a consultas
 * de descarga de archivos.
 * 
 * @author Luis Ayuso, Ivan Munsuri, Javier Salcedo
 */
public class RespuestaPeticionDescarga implements Serializable{
    
    // ATRIBUTOS
    private DatosCliente[] _lista;

    /**
     * Constructor de la clase DownloadOrderAnswer.
     * 
     * @param lista Lista de clientes asociados.
     */
    public RespuestaPeticionDescarga(DatosCliente[] lista) {
       _lista = lista;
    }

    /**
     * Devuelve la lista de clientes asociados a la consulta de descarga de un
     * archivo.
     * 
     * @return La lista de clientes asociados a la consulta de descarga de un
     * archivo.
     */
    public DatosCliente[] getLista() {
        return _lista;
    }

    /**
     * Establece un nuevo valor para la lista de clientes asociados a la consulta.
     * 
     * @param lista Nuevo valor de la lista de archivos asociada a establecer.
     */
    public void setLista(DatosCliente[] lista) {
        _lista = lista;
    }    
}
