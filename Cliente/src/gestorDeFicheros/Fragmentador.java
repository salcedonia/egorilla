package gestorDeFicheros;

import mensajes.serverclient.*;
import datos.*;
import java.io.*;
import java.util.*;

/**
 * Fragmenta los archivos de los directorios especificados para los temporales y los completos.
 *
 */
/*Es mas optimo diferencias el fragmentador de completos e incompletos, ya que preguntar 
 * por una variable es mas rapido que buscar en que lista esta el fichero.*/

/*Hacer de properties la extension de los archivos de indices*/
public class Fragmentador{

  //Este podria volver a leer los datos de las properties o que le pasa los valores el 
  //gestor de disco.
  //Hacer properties

  private String extesionIndices = ".part.met";

  //private String extesionFicheroTemporal = ".tmp";

  private int tamanioBytesFragmento = 512;

  private String _directorioTemporales;

  private String _directorioCompletos;

  //Lista de todos (temporales+completos)
  private ListaArchivos _listaTodos;

  //Lista de los temporales
  private ListaArchivos _listaTemporales;

  //Lista de los completos
  private ListaArchivos _listaCompletos; //Se necesita de un accion automatica o de recargar

  private GestorDisco _gestorDisco;

  private ManejarIndices _manejarIndices;

  private ManejarListaArchivos _manejarListaArchivos;



  /**
   */
  public Fragmentador( GestorDisco gestorDisco, String directorioCompletos, 
      String directorioTemporales){
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


  private Byte[] primitivoAObjeto( byte[] bytes ){
    Byte[] oBytesFragmento = new Byte[ bytes.length ];

    for( int i = 0;  i < bytes.length;  i++ ){
      //oBytesFragmento[ i ] = new Byte( bytes[ i ] );
      oBytesFragmento[ i ] = Byte.valueOf( bytes[ i ] );
    }

    return oBytesFragmento;
  }


 public Byte[] dameBytesDelFragmento( Fragmento fragmento ){
    int bytesLeidos = 0, off, len = 0;
    byte[] bytesFragmento;
    Byte[] oBytesFragmento = null;
    Archivo archivoRequerido;
    File fichero;
    RandomAccessFile punteroFichero;

    String hashFragmento = fragmento.getHash();
    
    //Compruebo si tengo el fichero con ese hash (aunque se supone que siempre estar�)
    
    //Debo buscar por hash y no por nombre, yaq el nombre no tiene xq coincidir 
    //buscar en las listas
    archivoRequerido = _manejarListaArchivos.buscarArchivoEnLista( _listaCompletos, hashFragmento );
    if( archivoRequerido == null ){
      //No esta en los completos, asi que miramos en los incompletos
      archivoRequerido = _manejarListaArchivos.buscarArchivoEnLista( _listaTemporales, hashFragmento );
      if( archivoRequerido == null ){
        //El fichero no EXISTE - devuelvo un null - ERROR
      }else{
        //Voy al fichero de indices y miro si esa parte del fragmento (offset)        
        fichero = new File( _directorioTemporales+"//" + archivoRequerido.getNombre()
            + extesionIndices );
        Indices indices = _manejarIndices.leeFicheroIndices( fichero );
        
        //Miro si el fragmento est� en ese array
        if( indices.containsTengo( fragmento ) == true ){
          //Si esta -> Obtengo los bytes del fragmento que me han pedido
          try{
          punteroFichero = new RandomAccessFile( fichero, "r" );
          off = (int)fragmento.getOffset();
          int tamanioBytesFragmentoAux = (int)(fragmento.getTama() - fragmento.getOffset() );
          if( tamanioBytesFragmentoAux < tamanioBytesFragmento )
            len = tamanioBytesFragmentoAux;
          else
            len = tamanioBytesFragmento;
          bytesFragmento = new byte[ len ];
          //System.out.println("offset "+off +" len "+len+" point "+punteroFichero.getFilePointer() );
          punteroFichero.seek( off );
          //bytesLeidos = punteroFichero.read( bytesFragmento, off, len );
          bytesLeidos = punteroFichero.read( bytesFragmento );
          oBytesFragmento = primitivoAObjeto( bytesFragmento );
          punteroFichero.close();
          }catch( Exception e ){
            e.printStackTrace();
          }
          //bytesLeidos debe coincidir con fragmento.getTama() - fragmento.getOffset()
          //Fragmento corrupto! o archivo corrupto
          //bytesLeidos > len - no es posible, se lee hasta un maximo de len bytes
          //bytesLeidos == len - guay!
          //bytesLeidos < len - informacion del fragmento inadecuada
          if( bytesLeidos < len ){
            //logger de error
          }
        }else{
          //No hago nada, devuelvo un null - o bueno, puede devolver el array vacio, VER, ERROR
          //Sino esta -> null, vacio, ERROR
        }        
      }
    }else{
      //Obtengo los bytes del fragmento que me han pedido - como esta completo no 
      //tengo que hacer na
      try{
      fichero = new File( _directorioCompletos+"//"+ archivoRequerido.getNombre() );
      punteroFichero = new RandomAccessFile( fichero, "r" );
      off = (int)fragmento.getOffset();
      int tamanioBytesFragmentoAux = (int)(fragmento.getTama() - fragmento.getOffset() );
      if( tamanioBytesFragmentoAux < tamanioBytesFragmento )
        len = tamanioBytesFragmentoAux;
      else
        len = tamanioBytesFragmento;
      bytesFragmento = new byte[ len ];
      //System.out.println("offset "+off +" len "+len+" point "+punteroFichero.getFilePointer() );
      punteroFichero.seek( off );
      //bytesLeidos = punteroFichero.read( bytesFragmento, off, len );
      bytesLeidos = punteroFichero.read( bytesFragmento );
      //System.out.println("point "+punteroFichero.getFilePointer() );
      oBytesFragmento = primitivoAObjeto( bytesFragmento );
      punteroFichero.close();
      }catch( Exception e ){
        e.printStackTrace();
      }
      //bytesLeidos debe coincidir con fragmento.getTama() - fragmento.getOffset()
      //Fragmento corrupto! o archivo corrupto
      //bytesLeidos > len - no es posible, se lee hasta un maximo de len bytes
      //bytesLeidos == len - guay!
      //bytesLeidos < len - informacion del fragmento inadecuada
      if( bytesLeidos < len ){
        //logger de error
      }
    }    
    return oBytesFragmento;
  }


 public int cantidadFragmentosArchivo( String hash ){
    //Busco en las listas el archivo y llamo a la otra
    //int cantidadFragmentosArchivo( Archivo archivo )
    return 0;
  }


  public int cantidadFragmentosArchivo( Archivo archivo ){
    long tamanio = archivo.getSize();
    int cantidadFragmentos;

    cantidadFragmentos = (int)tamanio / tamanioBytesFragmento;

    if( tamanio % tamanioBytesFragmento == 0 ){
      //Al no haber decimales, todas las partes tiene el mismo tama�o
    }else{
      cantidadFragmentos+=1;
    }

    return cantidadFragmentos;
  }

  public Vector<Fragmento> queFragmentosTienes( String hash ){
    //Lo primero que hago es bucar en hash en la lista de temporales y de completos
    Archivo archivoRequerido;
    Vector<Fragmento> listaFragmento = null;
    //Compruebo si tengo el fichero con ese hash (aunque se supone que siempre estar�)    
    //Debo buscar por hash y no por nombre, yaq el nombre no tiene xq coincidir 
    //buscar en las listas
    archivoRequerido = _manejarListaArchivos.buscarArchivoEnLista( _listaCompletos, hash );
    if( archivoRequerido == null ){
      //Debo buscarlo en los temporales
      //No esta en los completos, asi que miramos en los incompletos
      System.out.println("No esta en los completos");
      archivoRequerido = _manejarListaArchivos.buscarArchivoEnLista( _listaTemporales, hash );
      if( archivoRequerido == null ){
        System.out.println("No esta en los temporales tampoco - ERROR");
        //El fichero no EXISTE - devuelvo un null - ERROR
      }else{
        System.out.println("Si esta en los temporales - Abro fichero indices");
        //Voy al fichero de indices y miro si esa parte del fragmento (offset)        
        File fichero = new File( _directorioTemporales+"//" + archivoRequerido.getNombre()
            + extesionIndices );
        Indices indices = _manejarIndices.leeFicheroIndices( fichero );
        listaFragmento = indices.getIndicesTengo();
      }
    }else{
      System.out.println("Si esta en los completos");
      //Tengo todo los fragmentos, asi que los digo todos!
      //Esto igual seria mejor: serializarlo y guardado - No va a cambiar asi no tengo que 
      //recarcular ni volver a crear todos los objetos o guardarlo en mem.
      //Puedo calcular la cantidad de partes si los bytes del mismo sin fijos
      //Si es variable mejor con un while
      //TODO: metodo ya creado por ahi
      listaFragmento = new Vector<Fragmento>();
      Fragmento fragmento = new Fragmento( archivoRequerido.getNombre(), 
          archivoRequerido.getHash(), archivoRequerido.getSize(), 0 ); //0 por ser el primero
      listaFragmento.add( fragmento );
      for( int i = 1;  fragmento.getOffset()+tamanioBytesFragmento < fragmento.getTama();  i++ ){
        fragmento = new Fragmento( archivoRequerido.getNombre(),archivoRequerido.getHash(), 
            archivoRequerido.getSize(), i*tamanioBytesFragmento );
        listaFragmento.add( fragmento );
      }
      //Esto �ltimo sobra, simplemente que el ultimo fragmento tiene
      //un tama�o de   fragmento.getTama() - fragmento.getOffset()   y no tiene xq ser de 512
      /*if( fragmento.getOffset() < fragmento.getTama() ){
        long diferencia = fragmento.getTama() - fragmento.getOffset();
        long offsetDiff = fragmento.getOffset()+diferencia;
        fragmento = new Fragmento( archivoRequerido.getNombre(),archivoRequerido.getHash(), 
            archivoRequerido.getSize(), offsetDiff );
        listaFragmento.add( fragmento );
      }*/
    }
    return listaFragmento;
  }

  
  public Vector<Fragmento> queFragmentosFaltan( String hash ){
    //Lo primero que hago es bucar en hash en la lista de temporales y de completos
    Archivo archivoRequerido;
    Vector<Fragmento> listaFragmento = null;

    //Compruebo si tengo el fichero con ese hash (aunque se supone que siempre estar�)
    
    //Debo buscar por hash y no por nombre, yaq el nombre no tiene xq coincidir 
    //buscar en las listas
    archivoRequerido = _manejarListaArchivos.buscarArchivoEnLista( _listaCompletos, hash );
    if( archivoRequerido == null ){
      //Debo buscarlo en los temporales
      //No esta en los completos, asi que miramos en los incompletos
      archivoRequerido = _manejarListaArchivos.buscarArchivoEnLista( _listaTemporales, hash );
      if( archivoRequerido == null ){
        //El fichero no EXISTE - devuelvo un null - ERROR
        System.out.println("No esta en los temporales tampoco - ERROR");
      }else{
        //Voy al fichero de indices y miro si esa parte del fragmento (offset)        
        File fichero = new File( _directorioTemporales+"//" + archivoRequerido.getNombre()
            + extesionIndices );
        Indices indices = _manejarIndices.leeFicheroIndices( fichero );
        listaFragmento = indices.getIndicesFaltan();
      }
    }else{
      //Tengo todo los fragmentos, asi que los digo todos!
      //Esto igual seria mejor: serializarlo y guardado - No va a cambiar asi no tengo que 
      //recarcular ni volver a crear todos los objetos o guardarlo en mem.
      //Puedo calcular la cantidad de partes si los bytes del mismo sin fijos
      //Si es variable mejor con un while
      listaFragmento = new Vector<Fragmento>();//le creo vacio xq no falta ninguno
      /*Fragmento fragmento = new Fragmento( archivoRequerido.getNombre(), 
          archivoRequerido.getHash(), 0, archivoRequerido.getSize() ); 0 por ser el primero
      listaFragmento.add( fragmento );
      for( int i = 0;  fragmento.getOffset() == fragmento.getTama();  i++ ){
        fragmento = new Fragmento( archivoRequerido.getNombre(),archivoRequerido.getHash(), 
            i*tamanioBytesFragmento, archivoRequerido.getSize() );
        listaFragmento.add( fragmento );
      }*/
    }
    return listaFragmento;
  }
}

