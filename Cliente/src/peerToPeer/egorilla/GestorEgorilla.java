/*
 * Este proyecto esta sujeto a licencia GPL
 * This project and code is uncer GPL license
 */

package peerToPeer.egorilla;

import datos.Archivo;
import datos.Fragmento;
import gestorDeRed.GestorDeRed;
import gestorDeRed.NetError;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import mensajes.Mensaje;
import mensajes.p2p.HolaQuiero;
import mensajes.serverclient.DatosCliente;
import mensajes.serverclient.PeticionConsulta;
import mensajes.serverclient.PeticionDescarga;
import peerToPeer.GestorClientes;
import peerToPeer.descargas.GestorDescargas;

/**
 *
 * @author Luis Ayuso
 */
public class GestorEgorilla extends Thread{
    
    private Queue<Mensaje> _colaSalida;
   
    private GestorDescargas _gestorDescargas;
    private GestorDeRed<Mensaje> _gestorDeRed;
    
    private GestorSubidas _gestorSubidas;
    
    private GestorClientes _gestorClientes;
    
    private boolean _conectado;
    private String  _serverIP;
    private int  _serverPort;
    
    public GestorEgorilla(GestorDescargas gd, GestorDeRed<Mensaje> gr) {
        _colaSalida = new LinkedList<Mensaje>();
        _gestorDescargas = gd;
        _gestorSubidas = new GestorSubidas(this);
        _gestorDeRed = gr; 
        _gestorClientes = new GestorClientes();
    }
    
    /**
     * realiza la concexion con el servidor egorilla. 
     * este metodo es sincrono, por lo que estaremos dentro de el hasta que 
     * se realice toda la comunicaci�n, ya sea correcta o erronea.
     * 
     * @return si la conexion ha sido satisfactoria
     */
    public boolean conectaServidor(){
        // realiza la conexion por pasos.
        // para ello envia los datos al servidor y espera respuesta
        
        
        _conectado = true;
        return _conectado;
    }
    
    /**
     * pone el p2p en modo escucha, de forma que se atiende a otros
     * clientes
     *
     */
    public void comienzaP2P(){
        // TODO: 6969 fijado a capon
     //  _gestorDeRed = new GestorDeRedTCPimpl<Mensaje>(6969);
        _gestorDeRed.registraReceptor(new ServidorP2PEgorilla(this, _gestorDescargas));
       
        _gestorDeRed.comienzaEscucha();
    //   this.start();
    }

//    /**
//     * se da la orden de comenzar una descarga.
//     * esto dispara la colsulta de peers al servidor y 
//     * despues comenzar�n las negociaciones con estos
//     * @param a archivo a descargar
//     */
//    public void nuevaDescarga(Archivo a){
//        // se realiza la peticion de descarga al servidor
//        
//        Mensaje  msj = new PeticionDescarga(a._hash);
//        msj.setDestino(_serverIP, _serverPort);
//        
//        // si hemos conectado antes...
//        // lo envia al servidor.
//        if (!_conectado){
//            // TODO: notificar error
//        }
//        else
//            this.addMensajeParaEnviar(msj);      
//    } 
    
    /**
     * se realiza una consulta, el m�todo es asincrono, as� que no espera respuesta
     * la ejecuci�n continua. 
     * La respuesta llegara a la parte serividora, la cual lo comunicar� a sus listeners
     * 
     * @param query la consulta a hacer.
     */
    public void nuevaConsulta (String query){
        // crea el paquete consulta,
        Mensaje msj = new PeticionConsulta(query);
        msj.setDestino(_serverIP, _serverPort);
        
        // si hemos conectado antes...
        // lo envia al servidor.
        if (!_conectado){
            // TODO: notificar error
        }
        else
            this.addMensajeParaEnviar(msj);
        
    }
   
    /**
     * Se indica que este fichero se transmitira a este peer. 
     * 
     * 
     * @param a
     * @param ip
     * @param puerto
     * @param fragmentos
     */
    public void nuevaSubida(Archivo a, String ip, int puerto,ArrayList<Fragmento> fragmentos){
        //TODO:
    }
            
    public void nuevaDescarga(Archivo a, DatosCliente[] peers){
        // tomo nota de estos clientes. 
        
        // se les envia HolaQuiero a cada uno de ellos
        for (DatosCliente cliente : peers) {
            HolaQuiero hola = new HolaQuiero(a);
            hola.setDestino(cliente.getIP(), cliente.getPuertoEscucha());
            addMensajeParaEnviar(hola);
            //_colaSalida.add(hola);
        }
    }
    
    /**
     * se encola un mensaje para darle salida.
     * los mensajes se encolan primero para poder controlar el flujo.
     * TODO mensaje de salida DEBE pasar por aqui
     * @param msj
     */
    public synchronized void addMensajeParaEnviar(Mensaje msj){
        _colaSalida.add(msj);
        if (!this.isAlive())
            this.start();
    }
    
    public void run(){
        
            try {
                // por cada mensaje al que se le deba dar salida, se le da.
                while (!_colaSalida.isEmpty()){
                    Mensaje msj =_colaSalida.poll();       
                    _gestorDeRed.envia(msj, msj.destino(), msj.puerto());
                }
                //this.wait();
            }   catch (NetError ex) {
                // TODO: gestor de errores. hay no se ha podido hablar con quien dices
                    Logger.getLogger(GestorEgorilla.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    /**
     * tras la respuesta del servidor, se comienza la descarga de un fichero desde 
     * los clientes indicados
     * @param a fichero a descargar
     * @param lista lista de clientes que lo contienen
     */
    void DescargaFichero(Archivo a, DatosCliente[] lista) {
        
        for (DatosCliente cliente : lista) {
            _gestorClientes.addClienteDescarga(cliente.getIP(), a);
        }
        _gestorDescargas.altaFichero(a);
        
        // antes de nada, debo mirar si estoy entre los propietarios del
        // fichero, no quiero hablar conmigo mismo
        Vector<DatosCliente> sinMi = new Vector<DatosCliente>();
        for (DatosCliente cliente : lista) {
            // si no soy yo lo agrego 
            sinMi.add(cliente);
        }
        
        // ahora se envia el HolaQuiero a todos los clientes que lo tienen
        for (Iterator<DatosCliente> it = sinMi.iterator(); it.hasNext();) {
            DatosCliente cliente = it.next();
         
            HolaQuiero q = new HolaQuiero(a);
            q.setDestino(cliente.getIP(),cliente.getPuertoEscucha());
            this.addMensajeParaEnviar(q);
        }
    }
}
