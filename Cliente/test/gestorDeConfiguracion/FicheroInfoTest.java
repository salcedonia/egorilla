
package gestorDeConfiguracion;

import java.util.ArrayList;
import java.util.Iterator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author F. Javier Sanchez Pardo
 */
public class FicheroInfoTest {
    private static FicheroInfo <InfoServidor> _oFicheroInfoServidores;
    private static FicheroInfo <InfoDescarga> _oFicheroInfoDescargas;

    public FicheroInfoTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws ControlConfiguracionClienteException {
        _oFicheroInfoServidores = new FicheroInfo <InfoServidor> ("servidores.info");
        _oFicheroInfoServidores.cargarFicheroInfo();
        _oFicheroInfoDescargas = new FicheroInfo <InfoDescarga> ("descargas.info");    
        _oFicheroInfoDescargas.cargarFicheroInfo();
    }

    @Test
    public void testCarga() throws ControlConfiguracionClienteException {    
        _oFicheroInfoServidores.cargarFicheroInfo();
        ArrayList <InfoServidor> alInfoServidores = _oFicheroInfoServidores.obtenerConjuntoInfo();
        System.out.println("Fichero de servidores");
        for(Iterator <InfoServidor> iterador = alInfoServidores.iterator(); iterador.hasNext();){
            System.out.println(iterador.next());
        }
        
        _oFicheroInfoDescargas.cargarFicheroInfo();
        ArrayList <InfoDescarga> alInfoDescargas = _oFicheroInfoDescargas.obtenerConjuntoInfo();
        System.out.println("Fichero de descargas");
        for(Iterator <InfoDescarga> iterador = alInfoDescargas.iterator(); iterador.hasNext();){
            System.out.println(iterador.next());
        }
    }
    
    @Test
    public void testModificacion() throws ControlConfiguracionClienteException {    
        InfoServidor infoServidor;
        InfoDescarga infoDescarga;

        _oFicheroInfoServidores.cargarFicheroInfo();
        ArrayList <InfoServidor> alInfoServidores = _oFicheroInfoServidores.obtenerConjuntoInfo();
        //Añado 2 servidores mas
        infoServidor = new InfoServidor ("Servidor Otro", "128.23.21.1", "9091", "prueba 1");
        alInfoServidores.add(infoServidor);
        infoServidor = new InfoServidor ("Servidor Otro mas", "128.23.21.1", "9091", "prueba 1");
        alInfoServidores.add(infoServidor);
        //Modifico y grabo
        _oFicheroInfoServidores.establecerConjuntoInfo(alInfoServidores);
        //Imprimo para ver
        System.out.println("Fichero de servidores");
        for(Iterator <InfoServidor> iterador = alInfoServidores.iterator(); iterador.hasNext();){
            System.out.println(iterador.next());
        }
        
        _oFicheroInfoDescargas.cargarFicheroInfo();
        ArrayList <InfoDescarga> alInfoDescargas = _oFicheroInfoDescargas.obtenerConjuntoInfo();
        //Añado 2 descargas mas
        infoDescarga = new InfoDescarga ("Descarga+", "valor11", "valor12", "valor13");
        alInfoDescargas.add(infoDescarga);
        infoDescarga = new InfoDescarga ("Descarga++", "valor11", "valor12", "valor13");
        alInfoDescargas.add(infoDescarga);
        //Modifico y grabo
        _oFicheroInfoDescargas.establecerConjuntoInfo(alInfoDescargas);
        //Imprimo para ver
        System.out.println("Fichero de descargas");
        for(Iterator <InfoDescarga> iterador = alInfoDescargas.iterator(); iterador.hasNext();){
            System.out.println(iterador.next());
        }
    }    
}