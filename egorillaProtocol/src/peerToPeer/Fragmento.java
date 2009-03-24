/*
 * Este proyecto esta sujeto a licencia GPL
 * This project and code is uncer GPL license
 */

package peerToPeer;

/**
 * 
 * representa la informaci�n que define un fragmento de un
 * fichero.
 *
 * @author Luis Ayuso
 */
public class Fragmento {

    /**
     * nombre del fichero al que pertence
     */
    public String nombre;
    /**
     * hash que lo identifica
     */
    public String hash;
    
    /**
     * tama�o del fragmento
     */
    public long tama;
    /**
     * byte inical del fragmento
     */
    public long offset;
    
    /**
     * numero de posici�n que ocupa dentro del fichero 
     * (todos los fragmentos son de igual tama�o)
     */
    public int num;
}
