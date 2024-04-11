package com.example.ims;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.text.Editable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Vibrator;
import android.content.Context;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import android.text.TextWatcher;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import android.util.Log;


public class MainActivity extends AppCompatActivity {
    EditText n_lote;
    EditText codigo;
    EditText sevEditText;
    TextView contadorCajasLote;
    TextView contadorKilosLote;
    TextView contadorCajasProducto;
    TextView contadorKilosProducto;

    // contadores
    int contador_cajas_lote = 0;
    float contador_kilos_lote = 0;
    int contador_cajas_producto = 0;
    String contador_K_lote_str = "";

    String peso = "";
    String id_articulo = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        n_lote = findViewById(R.id.txtpalet);
        codigo = findViewById(R.id.qr);
        List<String> codigosValidos = new ArrayList<>();

        cambiarEdicionEditText(false);


        sevEditText = findViewById(R.id.ipsev);
        // Contador de lote
        contadorCajasLote = findViewById(R.id.textViewCounterCajas2);
        contadorKilosLote = findViewById(R.id.textViewCounterKilos2);

        // Contador de Producto
        contadorCajasProducto = findViewById(R.id.textViewCounterCajas4);
        contadorKilosProducto = findViewById(R.id.textViewCounterKilos1);
        // Botones





        // Evento para manejar cambios en el EditText de QR
        codigo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se requiere acción antes de que cambie el texto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No se requiere acción mientras cambia el texto
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Obtener la dirección IP del servidor
                String serverAddress = "192.168.1.101";

                // Obtener los datos de palet, cajas y código QR
                String lote = n_lote.getText().toString();
                String codigo_ean = codigo.getText().toString();
                if (lote.isEmpty()) {
                    // Mostrar alerta de campo vacío
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Por favor, complete todos los campos.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            codigo.setText("");
                                            n_lote.setText("Lote");
                                        }
                                    });
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return; // Salir del método para evitar más procesamiento
                }


                    // Verificar si el código QR es válido
                if (codigo_ean.length() == 13) {

                    id_articulo = codigo_ean.substring(1,7);
                    peso = codigo_ean.substring(6,12);


                    if (codigosValidos.isEmpty()) {
                        if (!codigosValidos.contains(codigo_ean)) {
                            // Agregar el código QR a la lista de códigos válidos y aumentar el contador
                            codigosValidos.add(codigo_ean);
                            contador_cajas_lote++;
                            contador_kilos_lote = contador_kilos_lote + Float.parseFloat(peso);

                        }
                        else{

                        }
                        DecimalFormat df = new DecimalFormat("#.##");
                        contador_K_lote_str = df.format(contador_kilos_lote);

                        // Crear un archivo CSV temporal
                        try {
                            File tempFile = File.createTempFile("data", ".csv");

                            // Escribir los datos en el archivo CSV
                            FileWriter writer = new FileWriter(tempFile);
                            for (String codigo : codigosValidos) {
                                writer.append(lote);
                                writer.append(",");
                                writer.append(codigo);
                                writer.append("\n"); // Agrega un salto de línea
                            }
                            writer.flush();
                            writer.close();

                            // Leer el contenido del archivo CSV y mostrarlo por log
                            BufferedReader reader = new BufferedReader(new FileReader(tempFile));
                            StringBuilder csvContent = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                csvContent.append(line).append("\n");
                            }
                            reader.close();


                            sendFileToServer(tempFile,serverAddress);


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    codigo.setText("");
                                    contadorCajasLote.setText(String.valueOf(contador_cajas_lote));
                                    contadorKilosLote.setText(String.valueOf(contador_K_lote_str));
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Si el número de programa no coincide con el primer número de programa registrado,
                        // mostrar un mensaje de alerta y hacer vibrar el teléfono
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null) {
                            vibrator.vibrate(2000);
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("El número de programa no coincide con el primer número de programa registrado. ¿Desea continuar?")
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Aquí puedes realizar alguna acción si el usuario no confirma
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                } else {

                    if (codigo_ean.length() > 0){

                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null) {
                            vibrator.vibrate(2000);
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Se ha ingresado un codigo no valido, lealo de nuevo ")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        codigo.setText("");
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    }

                }
            }


        });
    }
    // Función para enviar un mensaje al servidor
    private void sendMessageToServer(String message, String palet, int port) {
        // Obtener la dirección IP del servidor
        String serverAddress = "192.168.1.101";

        // Crear un hilo para enviar el mensaje al servidor
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Establecer conexión con el servidor en el puerto especificado
                    Socket socket = new Socket(serverAddress, port);

                    // Obtener el OutputStream del socket para enviar datos al servidor
                    OutputStream outputStream = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(outputStream);

                    // Enviar el mensaje al servidor
                    writer.println(message + "," + palet);
                    writer.flush();
                    writer.close();

                    // Cerrar la conexión
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void sendFileToServer(File file, String serverAddress) {
        // Verifica si el archivo existe y es un archivo válido
        if (!file.exists() || !file.isFile()) {
            Log.e("MainActivity", "El archivo no existe o no es válido.");
            return;
        }

        // Inicia un nuevo hilo para enviar el archivo al servidor
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Lee el contenido del archivo y envíalo al servidor
                try (Socket socket = new Socket(serverAddress, 8000);
                     FileInputStream fis = new FileInputStream(file);
                     BufferedInputStream bis = new BufferedInputStream(fis);
                     OutputStream os = socket.getOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    // Lee los datos del archivo y envíalos al servidor
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                        Log.i("MainActivity", new String(buffer, 0, bytesRead));
                    }

                    Log.i("MainActivity", "Archivo enviado correctamente al servidor.");

                } catch (IOException e) {
                    Log.e("MainActivity", "Error al enviar el archivo al servidor.", e);
                }
            }
        }).start();
    }

    private void clearCSVFile() {
        try {
            // Crear un archivo CSV temporal
            File tempFile = File.createTempFile("data", ".csv");

            // Limpiar el archivo CSV
            FileWriter writer = new FileWriter(tempFile);
            writer.write("");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void cambiarEdicionEditText(boolean editable) {
        // Cambiar la edición de los EditText según el valor de la variable 'editable'

    }
}
