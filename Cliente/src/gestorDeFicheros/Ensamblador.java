package gestorDeFicheros;

import mensajes.serverclient.*;
import datos.*;
import java.io.*;
import java.util.*;

/**
 * Guarda los fragmentos en el lugar correspondiente, ademas crea y mantiene actualizados los
 * indices de los temporales. Cuando se completo un archivo, lo manda al direc de completos
 *
 */
public class Ensamblador{

  //Este podria volver a leer los datos de las properties o que le pasa los valores el 
  //gestor de disco.
  
  //Hacer properties
  private String extesionIndices = ".part.met";

  private String extesionFicheroTemporal = ".tmp";

  private int tamanioBytesFragmento = 512;

  private String _directorioTemporales;

  private String _directorioCompletos;

  //Lista de todos (temporales+completos)
  private ListaArchivos _listaTodos;

  //Lista de los temporales
  private ListaArchivos _listaTemporales;

  //Lista de los completos
  private ListaArchivos _listaCompletos;

  private GestorDisco _gestorDisco;

  private ManejarIndices _manejarIndices;

  private ManejarListaArchivos _manejarListaArchivos;



  public Ensamblador( GestorDisco gestorDisco, String directorioCompletos, String directorioTemporales){
    _directorioCompletos = directorioCompletos;
    _directorioTemporales = directorioTemporales;
    _gestorDisco = gestorDisco;

    //Obtengo sus referencias, para poder tener las listas actualizarlas
    _listaCompletos = gestorDisco.getListaArchivosCompletos();
    _listaTemporales = gestorDisco.getListaArchivosTemporales();
    _listaTodos = gestorDisco.getListaArchivosTodos();

    _manejarIndices = gestorDisco.getManejarIndices();
    _manejarListaArchivos = gestorDisco.getManejarListaArchivos();
  }

  public void reservarEspacioFicheroNuevo( File fichero, long size ){
    byte[] bytes;
    int tamBuf = 9000;
    try{
      if( fichero.createNewFile() == true ){ //crea el fichero pero con 0 bytes
      FileOutputStream ficheroIndices = new FileOutputStream( fichero );
      BufferedOutputStream bufferedOutput = new BufferedOutputStream( ficheroIndices );
      //Meto bytes aleatorios (basura) para llegar al tamam�o indicado
      if( tamBuf < size ){
        bytes = new byte[ tamBuf ];
        long i;
        for( i = size;  i > tamBuf;  i-=tamBuf )
          bufferedOutput.write( bytes, 0, bytes.length );
        if( i > 0 ){
          //Si quedan todav�a bytes por escribir
          bytes = new byte[ (int)i ];
          bufferedOutput.write( bytes, 0, bytes.length );
        }
      }else{
        //Si es menor que 9000, pues grabo de un golpe todos los q sean
        bytes = new byte[ (int)size ];
        bufferedOutput.write( bytes, 0, bytes.length );
      }
      bufferedOutput.close();
      //creo que tambien hace falta cerrar el otro
      ficheroIndices.close();
      }else{
        System.out.println( "Problemas al crear el archivo <"+fichero.getName()+">" );
      }
    }catch( Exception e ){
      e.printStackTrace();
    }
  }

  //****Se debe actualizar las listas cada vez que se pone a bajar un nuevo archivo o cuando se
  //coloca un archivo nuevo a compartir y se "pincha en recargar"
  public boolean nuevoArchivoTemporal( Archivo archivoNuevo ){
    boolean creado = false;
    Archivo archivoExistencia;

    String hashFragmento = archivoNuevo.getHash();
    //Si el hash ya existe no le vuelvo a crear
    //Como es un archivo nuevo, temporal, debo crear un nuevo archivo de indices para este 
    //archivo

    //En principio debo mirar si ya existe en la lista de temporales, pero por si acaso miraria
    //tambien en la de completos, para que no se baje un archivo que ya tiene dos veces
    //Tal vez sea interesante distinguir el que se intente crear un archivo q esta en los temp
    //o el que se intente crear cuando se encuentra en los completos.
    archivoExistencia = _manejarListaArchivos.buscarArchivoEnLista( _listaTemporales, hashFragmento );
    if( archivoExistencia == null ){
      System.out.println( "No esta en la lista de temporales" );
      //Entonces miro tambien en los completos
      archivoExistencia = _manejarListaArchivos.buscarArchivoEnLista( _listaCompletos, hashFragmento );
      if( archivoExistencia == null ){
        System.out.println( "No esta en la lista de completos" );
        System.out.println( "Asi que creo el fichero temporal y el indice" );
        //El archivo no se encuentra, es nuevo! Lo creo en los temporales
        File fichero = new File( _directorioTemporales+"//" + archivoNuevo.getNombre()
            + extesionIndices );
        //problema con el getNombre, puede qhaya otro con el mismo nombreee!
        _manejarIndices.crearFicheroIndices( fichero, archivoNuevo, fragmentosArchivoNuevo( archivoNuevo ) );
        //Creo el fichero con el tama�o que se me indica, pero sin tener sentido
        fichero = new File( _directorioTemporales+"//" + archivoNuevo.getNombre() +extesionFicheroTemporal  );   
        reservarEspacioFicheroNuevo( fichero, archivoNuevo.getSize() );
        
        //Y si todo ha ido bien actualizo las listas
        System.out.println( "Actualizo las listas de los archivos" );
        _manejarListaArchivos.includirArchivoEnLista( archivoNuevo, _listaTemporales );
        _manejarListaArchivos.includirArchivoEnLista( archivoNuevo, _listaTodos );
        creado = true;
      }else{
        System.out.println( "Esta en los archivos completos, ya ha sido descargado" );
        //Esta en los archivos completos, ya ha sido descargado.
      }
    }else{
      System.out.println( "Ya se encuentra en los temporales, actualmente en descarga" );
      //Ya se encuentra en los temporales, actualmente en descarga.
    }
    //Se puede dejar en un simple if-else sino me interesa diferenciar esos dos problemas
    return creado;
  }

  public Vector<Fragmento> fragmentosArchivoNuevo( Archivo archivo ){
    Vector<Fragmento> listaFragmento = new Vector<Fragmento>();
   
    Fragmento fragmento = new Fragmento( archivo.getNombre(), 
          archivo.getHash(), archivo.getSize(), 0 ); //0 por ser el primero
      listaFragmento.add( fragmento );
      for( int i = 1; fragmento.getOffset()+tamanioBytesFragmento < fragmento.getTama(); i++ ){
        fragmento = new Fragmento( archivo.getNombre(),archivo.getHash(), 
            archivo.getSize(), i*tamanioBytesFragmento );
        listaFragmento.add( fragmento );
      }
      return listaFragmento;
  }



  private byte[] objetoAPrimitivo( Byte[] bytes ){
    byte[] pBytesFragmento = new byte[ bytes.length ];

    for( int i = 0;  i < bytes.length;  i++ ){
      pBytesFragmento[ i ] = bytes[ i ].byteValue();
    }

    return pBytesFragmento;
  }


  //Este ya debe comprobar mediante el part.met si le llegan byte[]-fragmentos duplicados o con error, etc
  public boolean guardarFragmentoEnArchivo(Fragmento fragmento, Byte[] byteArchivo){
    boolean guardado = false;
    Archivo archivoExistencia;
    int off, len = 0;
    File fichero = null;
    String hashFragmento = fragmento.getHash();

    //Tengo qcomprobar cuando completo un archivo, para moverlo a completos

    archivoExistencia = _manejarListaArchivos.buscarArchivoEnLista( _listaTemporales, hashFragmento );
    if( archivoExistencia == null ){
      //No esta en la lista, posible Fragmento corrupto
      System.out.println( "No esta en la lista, posiblemente fragmento corrupto" );
      //guardado = false;
    }else{
      //Guardo la parte donde toque
      try{
      fichero = new File( _directorioTemporales+"//" + archivoExistencia.getNombre()+
          extesionFicheroTemporal );
      RandomAccessFile punteroFichero = new RandomAccessFile( fichero, "rw" );
      /*FileOutputStream archivo = new FileOutputStream( fichero );
      BufferedOutputStream bufferedOutput = new BufferedOutputStream( archivo );*/   
      off = (int)fragmento.getOffset();
      //len = (int)(fragmento.getTama() - fragmento.getOffset() );
      //len = tamanioBytesFragmento;
      int tamanioBytesFragmentoAux = (int)(fragmento.getTama() - fragmento.getOffset() );
      if( tamanioBytesFragmentoAux < tamanioBytesFragmento )
        len = tamanioBytesFragmentoAux;
      else
        len = tamanioBytesFragmento;
      byte[] bytes = objetoAPrimitivo( byteArchivo );
      punteroFichero.seek( off );
      punteroFichero.write( bytes );      
      //bufferedOutput.write( bytes, off, len );
      //bufferedOutput.close();
      //creo que tambien hace falta cerrar el otro
      //archivo.close();
      punteroFichero.close();
      }catch( Exception e ){
        e.printStackTrace();
        return false;
      }

      //Actualizo el fichero de indices
      File ficheroIndices = new File( _directorioTemporales+"//" + archivoExistencia.getNombre()
            + extesionIndices );
      Indices indices = _manejarIndices.leeFicheroIndices( ficheroIndices );
      indices.addTengo( fragmento );
      _manejarIndices.guardarFicheroIndices( ficheroIndices, indices );

      if( indices.getIndicesFaltan().size() == 0 ){
        System.out.println( "Acabo de completar el fichero, lo muevo a los completos" );
        _manejarListaArchivos.eliminarArchivoDeLista( indices.getArchivo(), _listaTemporales );
        _manejarListaArchivos.includirArchivoEnLista( indices.getArchivo(), _listaCompletos );

        File ficheroCompleto = new File( _directorioCompletos+"//"+
            indices.getArchivo().getNombre() );
        mover( fichero, ficheroCompleto );
        
        if( _manejarIndices.borrarFicheroIndices( ficheroIndices ) == false )
          System.out.println( "Problemas al borrar el archivo de indices" );      

        /*_gestorDisco.recorrerListaArchivos( _listaTemporales );
        _gestorDisco.recorrerListaArchivos( _listaCompletos );*/
        //Y no la incluyo en listaTodos yaq tiene que estar
        //Tendre que eliminar el archivo de indices o algo asi
        guardado = true;
      }
      
      //Y la cantidad de fragmentos que tengo o me faltan segun como lo este haciendo
    }
    return guardado;
  }

  public boolean mover(File source, File destination) {
    if( !destination.exists() ) {
      // intentamos con renameTo
      boolean result = source.renameTo(destination);
      if( result == false ) {
        System.out.println("No se ha podido mover el temporal, copiando...");
        // intentamos copiar
        result = true;
        result &= copiar(source, destination);
        result &= source.delete();
      }
      return result;
    } else {
      // Si el fichero destination existe, cancelamos...
      return false;
    } 
  }

  public static boolean copiar( File source, File destination ){
    boolean resultado = false;
    // declaraci�n del flujo
    FileInputStream sourceFile = null;
    FileOutputStream destinationFile = null;
    try {
      // creamos fichero
      if( destination.createNewFile() == true ){
      // abrimos flujo
      sourceFile = new FileInputStream(source);
      destinationFile = new FileOutputStream(destination);
      // lectura por segmentos de 0.5Mb
      //byte buffer[]=new byte[512*1024];
      byte buffer[]=new byte[9000];
      int nbLectura;
      while( (nbLectura = sourceFile.read(buffer)) != -1 ) {
        destinationFile.write(buffer, 0, nbLectura);
      } 
      // copia correcta
      resultado = true;
      }
    } catch( FileNotFoundException f ) {
    } catch( IOException e ) {
    } finally {
      // pase lo que pase, cerramos flujo
      try {
        sourceFile.close();
      } catch(Exception e) { }
      try {
        destinationFile.close();
      } catch(Exception e) { }
    } 
    return resultado;
  }
}

