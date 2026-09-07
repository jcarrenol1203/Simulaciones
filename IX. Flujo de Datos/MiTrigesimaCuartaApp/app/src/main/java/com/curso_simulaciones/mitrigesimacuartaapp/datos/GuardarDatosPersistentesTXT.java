package com.curso_simulaciones.mitrigesimacuartaapp.datos;

import android.app.Activity;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class GuardarDatosPersistentesTXT {

    /*
      Cada fila es un arreglo {tiempo, ax, ay, az, a}. Usar un solo
      Vector de filas (en vez de cinco Vector paralelos como en la
      app del luxómetro) evita que las columnas se desincronicen si
      llenarDatos() llegara a invocarse de forma desigual.
     */
    private Vector<float[]> filas = new Vector<>();

    /**
     * Constructor del Manejador de  Archivos
     */
    public GuardarDatosPersistentesTXT(){

    }

    /**
     * Para recibir los datos que se grabarán
     * en archivo .txt en una carpeta definida
     * por el usuario en un documento.
     * Serán seis columnas: # de dato (implícito por la posición),
     * tiempo, ax, ay, az, a.
     * @param tiempo
     * @param ax
     * @param ay
     * @param az
     * @param a
     */
    public void llenarDatos(double tiempo, double ax, double ay, double az, double a) {
        filas.addElement(new float[]{(float) tiempo, (float) ax, (float) ay, (float) az, (float) a});
    }

    /**
     * Para borrar los datos. Estos no serán
     * guardados en el arcivo .txt.
     */
    public void borrarDatos() {
        filas.removeAllElements();
    }

    /**
     * Para guradar los datos en formato .txt
     * @param actividad
     * @param carpeta
     */
    public void guardar(Activity actividad, String carpeta) {
        Date date = new Date();
        DateFormat hora_fecha = new SimpleDateFormat("yy-MM-dd hh:mm:ss");

        try {
            /*
              Paso 1 y Paso 2: Crear y abrir flujo de salida
              Esto es, crear y abrir el canal de salida
            */
            String marca= hora_fecha.format(date).toString();
            String nombre_archivo ="datos_"+marca+ ".txt";
            File file=null;
            File path = null;

            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q){
                //versiones inferiores a Android 10
                path = new File(Environment.getExternalStorageDirectory(), carpeta);
            } else {
                //versiones desde Android 10 en adelante
               path = new File(actividad.getExternalFilesDir(null), carpeta);
            }

            file = new File(path, nombre_archivo);

            FileOutputStream flujoSalida= new FileOutputStream(file);

            /*
             Agregar filtro. En este caso se le pasa a
             OutputStreamWriter para que escriba
           */
            OutputStreamWriter escritor = new OutputStreamWriter(flujoSalida);//fos);

            /*
              Paso 3: Escribir información mientras haya
            */
            // Escribimos el String en el archivo: # de dato, tiempo, ax, ay, az, a
            for (int i = 0; i < filas.size(); i = i + 1) {
                float[] fila = filas.get(i);
                float tiempo_dos_decimales = (float)(Math.round(fila[0] * 100) / 100f);
                float ax_dos_decimales = (float)(Math.round(fila[1] * 100) / 100f);
                float ay_dos_decimales = (float)(Math.round(fila[2] * 100) / 100f);
                float az_dos_decimales = (float)(Math.round(fila[3] * 100) / 100f);
                float a_dos_decimales = (float)(Math.round(fila[4] * 100) / 100f);
                escritor.write(""+(i+1));
                escritor.write("\t");
                escritor.write(""+tiempo_dos_decimales);
                escritor.write("\t");
                escritor.write(""+ax_dos_decimales);
                escritor.write("\t");
                escritor.write(""+ay_dos_decimales);
                escritor.write("\t");
                escritor.write(""+az_dos_decimales);
                escritor.write("\t");
                escritor.write(""+a_dos_decimales + "\r\n");
            }

           /*
            Paso 4: cerrar canal
           */
            escritor.flush();
            escritor.close();

            // Mostramos que se ha guardado
            String aviso= "Los datos fueron guardados en la carpeta:" + "\n" +
                    "Mis archivos/"+carpeta;
            Toast.makeText(actividad, aviso, 2000).show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
