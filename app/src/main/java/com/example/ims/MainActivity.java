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
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import android.util.Log;


public class MainActivity extends AppCompatActivity {
    EditText n_palet;
    EditText n_cajas;
    EditText qr;
    EditText sevEditText;
    ImageButton saveButton;
    ImageButton delButton;
    ImageButton penButton;
    Button verificar;
    TextView contadorTextView;
    TextView contadorTextView2;
    int contador = 0;
    int contador_rep = 0;
    List<String> codigosValidos = new ArrayList<>();
    String numeroPrograma = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        n_palet = findViewById(R.id.txtpalet);
        n_cajas = findViewById(R.id.txtcajas);
        qr = findViewById(R.id.txtcodigo);

        cambiarEdicionEditText(false);


        sevEditText = findViewById(R.id.ipsev);
        contadorTextView = findViewById(R.id.contadorTextView);
        contadorTextView2 = findViewById(R.id.repetida);
        // Botones
        saveButton = findViewById(R.id.saveButton);
        delButton = findViewById(R.id.deleteButton);
        penButton = findViewById(R.id.pendeButton);
        verificar = findViewById(R.id.verificar);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cuando se hace clic en el botón "saveButton", enviar al servidor un mensaje "1" por el puerto 9000
                String palet = n_palet.getText().toString();


                    // Verificar si el contador es igual al número de cajas
                    if (contador == Integer.parseInt(n_cajas.getText().toString())) {
                        sendMessageToServer("1", palet, 9000);
                        contador_rep=0;
                        contador=0;
                        codigosValidos.clear();
                        clearCSVFile();
                        numeroPrograma = "";
                        verificar.setEnabled(true);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                qr.setText("");
                                n_cajas.setText("");
                                n_palet.setText("");
                                contadorTextView.setText(String.valueOf(contador));
                                contadorTextView2.setText(String.valueOf(contador_rep));
                                Toast.makeText(MainActivity.this, "El palet con número " + palet + " se ha GUARDADO", Toast.LENGTH_SHORT).show();
                            }
                        });
                        cambiarEdicionEditText(false);

                    } else {
                        // Si no es igual, mostrar un mensaje de advertencia
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Faltan cajas por contar", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

            }
        });

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el número de palet actual
                String palet = n_palet.getText().toString();

                // Mostrar un diálogo de confirmación
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("¿Está seguro de borrar el palet actual?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Cuando se hace clic en "Sí", enviar al servidor un mensaje "2" por el puerto 9000
                                sendMessageToServer("2", palet, 9000);
                                contador_rep=0;
                                contador=0;
                                codigosValidos.clear();
                                clearCSVFile();
                                cambiarEdicionEditText(false);
                                verificar.setEnabled(true);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        qr.setText("");
                                        n_cajas.setText("");
                                        n_palet.setText("");
                                        contadorTextView.setText(String.valueOf(contador));
                                        contadorTextView2.setText(String.valueOf(contador_rep));
                                        Toast.makeText(MainActivity.this, "El palet con número " + palet + " se ha borrado", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Cuando se hace clic en "No", cerrar el diálogo sin realizar ninguna acción
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        penButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Cuando se hace clic en el botón "penButton", enviar al servidor un mensaje "3" por el puerto 9000
                String palet = n_palet.getText().toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Confirmación");
                builder.setMessage("¿Desea dejar pendiente el palet? Por favor dejar Marcado en que caja Llevan contada");
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Realizar la acción correspondiente al dejar pendiente el palet
                        sendMessageToServer("3", palet, 9000);
                        contador_rep=0;
                        codigosValidos.clear();
                        numeroPrograma = "";
                        cambiarEdicionEditText(false);
                        verificar.setEnabled(true);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                qr.setText("");
                                Toast.makeText(MainActivity.this, "El palet con número " + palet + " se ha quedado pendiente con "+ n_cajas+ " Insertadas", Toast.LENGTH_SHORT).show();
                                n_cajas.setText("");
                                n_palet.setText("");
                                contadorTextView.setText(String.valueOf(contador));
                                contador=0;
                                contadorTextView2.setText(String.valueOf(contador_rep));
                            }
                        });

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // No hacer ningún cambio
                    }
                });
                // Mostrar el cuadro de diálogo
                builder.show();
            }
        });

        verificar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Obtener el número de palet desde el EditText
                String palet = n_palet.getText().toString();

                // Crear un hilo para realizar la solicitud al servidor
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Crear un socket para conectarse al servidor en el puerto 9000
                            Socket socket = new Socket("192.168.1.101", 9000);

                            // Obtener el OutputStream para enviar datos al servidor
                            OutputStream outputStream = socket.getOutputStream();
                            PrintWriter writer = new PrintWriter(outputStream);

                            // Enviar el mensaje al servidor (por ejemplo, "verificar,palet")
                            String message = "verificar," + palet;
                            writer.println(message);
                            writer.flush();

                            // Esperar la respuesta del servidor
                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String response = reader.readLine(); // Espera bloqueante hasta recibir la respuesta

                            // Manejar la respuesta del servidor en el hilo principal (UI thread)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (response.equals("no_existe")) {
                                        cambiarEdicionEditText(true);
                                        verificar.setEnabled(false);
                                        Toast.makeText(MainActivity.this, "Respuesta del servidor: " + response, Toast.LENGTH_SHORT).show();
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setMessage("Este número de palet ya existe. Por favor ingrese uno nuevo.")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        contador = 0;
                                                        contador_rep = 0;
                                                        codigosValidos.clear();
                                                        clearCSVFile();
                                                        n_palet.setText("");
                                                        n_cajas.setText("");
                                                        contadorTextView.setText(String.valueOf(contador));
                                                        contadorTextView2.setText(String.valueOf(contador_rep));
                                                    }
                                                });
                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();
                                    }
                                }
                            });

                            // Cerrar la conexión con el servidor
                            socket.close();
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();

                            // Manejar cualquier error de conexión o comunicación con el servidor en el hilo principal (UI thread)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }

        });


        // Evento para manejar cambios en el EditText de QR
        qr.addTextChangedListener(new TextWatcher() {
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
                String palet = n_palet.getText().toString();
                String cajas = n_cajas.getText().toString();
                String codigoQR = qr.getText().toString();
                if (palet.isEmpty() || cajas.isEmpty()) {
                    // Mostrar alerta de campo vacío
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Por favor, complete todos los campos.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            qr.setText("");
                                            n_palet.setText("Cambiar");
                                            n_cajas.setText("999");
                                        }
                                    });
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return; // Salir del método para evitar más procesamiento
                }
                int cajas_int;

                try {
                    cajas_int = Integer.parseInt(cajas);
                } catch (NumberFormatException e) {
                    // Manejar el caso en que cajas no sea un número válido
                    e.printStackTrace();
                    // Mostrar alerta de valor no válido en cajas
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("El valor en el campo 'cajas' no es un número válido.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Aquí puedes realizar alguna acción si el usuario confirma
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return; // Salir del método para evitar más procesamiento
                }

                if (numeroPrograma.equals("") && codigoQR.length() == 14 ){
                    numeroPrograma = codigoQR.substring(3, 7);
                }

                // Verificar si el código QR es válido
                if (codigoQR.length() == 14) {
                    String numeroProgramaActual = codigoQR.substring(3, 7);

                    if (codigosValidos.isEmpty() || numeroPrograma.equals(numeroProgramaActual)) {
                        if (!codigosValidos.contains(codigoQR)) {
                            // Agregar el código QR a la lista de códigos válidos y aumentar el contador
                            codigosValidos.add(codigoQR);
                            contador++;
                        }
                        else{
                            contador_rep++;
                        }

                        // Crear un archivo CSV temporal
                        try {
                            File tempFile = File.createTempFile("data", ".csv");

                            // Escribir los datos en el archivo CSV
                            FileWriter writer = new FileWriter(tempFile);
                            for (String codigo : codigosValidos) {
                                writer.append(palet);
                                writer.append(",");
                                writer.append(cajas);
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

                            if(contador == cajas_int){
                                sendFileToServer(tempFile,serverAddress);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    qr.setText("");
                                    contadorTextView.setText(String.valueOf(contador));
                                    contadorTextView2.setText(String.valueOf(contador_rep));
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
                                                qr.setText("");
                                                contadorTextView.setText(String.valueOf(contador));
                                                contadorTextView2.setText(String.valueOf(contador_rep));
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
        n_cajas.setEnabled(editable);
        qr.setEnabled(editable);
    }
}
