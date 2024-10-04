package com.example.ims;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.text.Editable;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Vibrator;
import android.content.Context;
import android.content.Intent;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import android.text.TextWatcher;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import android.util.Log;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    EditText n_palet;
    EditText n_cajas;
    EditText qr;
    EditText sevEditText;
    ImageButton saveButton;
    ImageButton delButton;
    ImageButton penButton;
    ImageButton configButton;
    Button verificar;

    Switch pendiente;
    Switch mixto;
    TextView contadorTextView;
    TextView contadorTextView2;

    private final String expirationDate = "07/10/2025";
    private static final String TAG = "MainActivity";


    int contador = 0;
    int contador_rep = 0;
    int largo_pen = 0;
    List<String> codigosValidos = new ArrayList<>();
    List<lecturavalida> codigosValidos1 = new ArrayList<>();
    String numeroPrograma = "";
    boolean switchpen = false;
    boolean switchmixto = false;
    boolean original_state_mixto;
    boolean eliminado = false;


    private static final String CONFIG_FILE_NAME = "config.txt";
    private static final String DEFAULT_IP = "192.168.1.101";


    private File tempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtener la hora actual desde la API
        fetchCurrentTimeFromAPI();

        // inputs
        n_palet = findViewById(R.id.txtpalet);
        n_cajas = findViewById(R.id.txtcajas);
        qr = findViewById(R.id.txtcodigo);

        //Textos
        sevEditText = findViewById(R.id.ipsev);
        contadorTextView = findViewById(R.id.contadorTextView);
        contadorTextView2 = findViewById(R.id.repetida);

        // Botones
        saveButton = findViewById(R.id.saveButton);
        delButton = findViewById(R.id.deleteButton);
        penButton = findViewById(R.id.pendeButton);
        verificar = findViewById(R.id.verificar);
        configButton = findViewById(R.id.config_button);

        // Switch
        pendiente = findViewById(R.id.Pendiente);
        mixto = findViewById(R.id.mixto);

        // Guardar el estado original del Switch al iniciar la actividad
        original_state_mixto = mixto.isChecked();

        cambiarEdicionEditText(false);

        // Verificar si el archivo de configuración existe
        File configFile = new File(getFilesDir(), CONFIG_FILE_NAME);

        if (!configFile.exists()) {
            // Si el archivo no existe, crearlo con la IP predeterminada
            createConfigFile(DEFAULT_IP);
        }

        configButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un Intent para abrir la ConfigActivity
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                // Iniciar la ConfigActivity
                startActivity(intent);
            }
        });

        // Funcionalidad boton Guardar
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
                        codigosValidos1.removeIf(lectura -> lectura.getPalet().equals(palet));
                        clearCSVFile();
                        numeroPrograma = "";
                        verificar.setEnabled(true);
                        n_palet.setEnabled(true);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                qr.setText("");
                                n_cajas.setText("");
                                n_palet.setText("");
                                contadorTextView.setText(String.valueOf(contador));
                                contadorTextView2.setText(String.valueOf(contador_rep));
                                pendiente.setChecked(false);
                                mixto.setChecked(false);
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

        // Funcionalidad Boton eliminar
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el número de palet actual
                String palet = n_palet.getText().toString();

                eliminado = true;

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
                                codigosValidos1.removeIf(lectura -> lectura.getPalet().equals(palet));
                                clearCSVFile();
                                numeroPrograma = "";
                                cambiarEdicionEditText(false);
                                verificar.setEnabled(true);
                                n_palet.setEnabled(true);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        qr.setText("");
                                        n_cajas.setText("");
                                        n_palet.setText("");
                                        contadorTextView.setText(String.valueOf(contador));
                                        contadorTextView2.setText(String.valueOf(contador_rep));
                                        pendiente.setChecked(false);
                                        mixto.setChecked(false);
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

        // funcionalidad boton Pendientes
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
                        numeroPrograma = "";
                        cambiarEdicionEditText(false);
                        verificar.setEnabled(true);
                        n_palet.setEnabled(true);
                        String paletParaBuscar = palet;

                        for (lecturavalida lectura : codigosValidos1) {
                            if (lectura.getPalet().equals(paletParaBuscar)) {
                                largo_pen++;
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                qr.setText("");
                                Toast.makeText(MainActivity.this, "El palet con número " + palet + " se ha quedado pendiente con "+ String.valueOf(largo_pen)+ " guardadas en este dispositivo", Toast.LENGTH_SHORT).show();
                                n_cajas.setText("");
                                n_palet.setText("");
                                contador = 0;
                                contador_rep = 0;
                                largo_pen = 0;
                                pendiente.setChecked(false);
                                mixto.setChecked(false);
                                contadorTextView.setText(String.valueOf(contador));
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

        pendiente.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Verifica si el estado del Switch ha cambiado a activado o desactivado
                if (isChecked) {
                    verificar.setEnabled(false);
                    n_palet.setEnabled(false);
                    n_cajas.setEnabled(true);
                    qr.setEnabled(true);
                    mixto.setEnabled(true);
                    switchpen = true;
                    int cantidad_pendientes = 0;
                    final String[] caja = {""};
                    String paletBuscado = n_palet.getText().toString();

                    for (lecturavalida lectura : codigosValidos1) {
                        if (lectura.getPalet().equals(paletBuscado)) {
                            cantidad_pendientes++;
                            caja[0] = lectura.getCajas();
                        }
                    }

                    contador = cantidad_pendientes;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            qr.setText("");
                            n_cajas.setText(caja[0]);
                            n_palet.setText(paletBuscado);
                            mixto.setChecked(false);
                            contadorTextView.setText(String.valueOf(contador));
                            contadorTextView2.setText(String.valueOf(contador_rep));
                        }
                    });

                } else {
                    verificar.setEnabled(true);
                    n_palet.setEnabled(true);
                    n_cajas.setEnabled(false);
                    qr.setEnabled(false);
                }
            }
        });

        mixto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Verifica si el estado del Switch ha cambiado a activado o desactivado
                if (isChecked) {
                    switchmixto = true;

                } else {
                    switchmixto = false;
                }
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
                            Socket socket = new Socket(readConfigFile(), 9000);

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
                                        n_palet.setEnabled(false);
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
                String serverAddress = readConfigFile();

                // Obtener los datos de palet, cajas y código QR
                String palet = n_palet.getText().toString();
                String cajas = n_cajas.getText().toString();
                String codigoQR = qr.getText().toString();
                boolean rep = false;
                if ((palet.isEmpty() || cajas.isEmpty()) && !switchpen) {

                    if(!eliminado){
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
                    }
                    else {
                        eliminado = false;
                    }

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

                if (numeroPrograma.equals("") && codigoQR.length() == 12 ){
                    numeroPrograma = codigoQR.substring(7, 11);
                }


                    // Verificar si el código QR es válido
                if (codigoQR.length() == 12) {
                    String numeroProgramaActual = codigoQR.substring(7, 11);
                    if (switchmixto || codigosValidos1.isEmpty() || numeroPrograma.equals(numeroProgramaActual)) {
                        // Verificar si el código QR ya existe
                        rep = false;
                        for (lecturavalida lectura : codigosValidos1) {
                            if (lectura.getCodigo().equals(codigoQR)) {
                                rep = true;
                                break;  // Romper el bucle si se encuentra coincidencia
                            }
                        }

                        if (!rep) {  // Si no hay repetido, agrega el código
                            codigosValidos1.add(new lecturavalida(codigoQR, palet, cajas));
                            contador++;
                        } else {
                            contador_rep++;
                        }

                        // Crear el archivo CSV temporal
                        try {
                            tempFile = File.createTempFile("data", ".csv");
                            escribirArchivoCSV(codigosValidos1, tempFile, palet, contador);

                            // Leer y mostrar el contenido del archivo CSV
                            String csvContent = leerArchivoCSV(tempFile);
                            System.out.println(csvContent); // Si necesitas ver el contenido en el log

                            // Verificar si se alcanzó el número de cajas
                            if (contador == cajas_int) {
                                sendFileToServer(tempFile, serverAddress);
                            }

                            // Actualizar la UI en el hilo principal
                            runOnUiThread(() -> {
                                qr.setText("");
                                contadorTextView.setText(String.valueOf(contador));
                                contadorTextView2.setText(String.valueOf(contador_rep));
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        mostrarAlertaProgramaNoCoincide();
                    }


                } else {

                    if (codigoQR.length() > 0){

                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null) {
                            vibrator.vibrate(2000);
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Se ha ingresado un codigo no valido, lealo de nuevo ")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        qr.setText("");
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
        String serverAddress = readConfigFile();

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
        mixto.setEnabled(editable);
    }

    // Método para crear un archivo de configuración
    private void createConfigFile(String ip) {
        String content = "ip=" + ip;
        try (FileOutputStream fos = openFileOutput(CONFIG_FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes());
            Log.i("MAIN", "Archivo de configuración creado con IP predeterminada: " + ip);
        } catch (IOException e) {
            Log.e(TAG, "Error al crear el archivo de configuración", e);
        }
    }

    // Método para leer el archivo de configuración
    private String readConfigFile() {
        StringBuilder configContent = new StringBuilder();
        try (FileInputStream fis = openFileInput(CONFIG_FILE_NAME)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                configContent.append(line);
            }
            Log.i(TAG, "Archivo de configuración leído correctamente");
        } catch (IOException e) {
            Log.e(TAG, "Error al leer el archivo de configuración", e);
        }

        // Parsear el contenido para obtener la IP
        String[] configParts = configContent.toString().split("=");
        if (configParts.length == 2 && "ip".equals(configParts[0])) {
            Log.d(TAG, "IP obtenida del archivo de configuración: " + configParts[1]);
            return configParts[1];
        } else {
            Log.w(TAG, "Formato del archivo de configuración inválido, usando IP predeterminada");
            return DEFAULT_IP; // Reemplaza con la IP predeterminada que desees
        }
    }

    // Método para escribir el archivo CSV
    private void escribirArchivoCSV(List<lecturavalida> codigosValidos1, File tempFile, String palet, int contador) throws IOException {
        FileWriter writer = new FileWriter(tempFile);
        for (lecturavalida lectura : codigosValidos1) {
            if (lectura.getPalet().equals(palet)) {
                writer.append(lectura.getPalet())
                        .append(",")
                        .append(String.valueOf(contador))
                        .append(",")
                        .append(lectura.getCodigo())
                        .append("\n");
            }
        }
        writer.flush();
        writer.close();
        Log.i(TAG, "Archivo CSV escrito correctamente");
    }

    // Método para leer el contenido del archivo CSV
    private String leerArchivoCSV(File tempFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(tempFile));
        StringBuilder csvContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            csvContent.append(line).append("\n");
        }
        reader.close();
        Log.i(TAG, "Contenido del archivo CSV leído correctamente");
        return csvContent.toString();
    }

    // Método para mostrar la alerta cuando los programas no coinciden
    private void mostrarAlertaProgramaNoCoincide() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(2000);
            Log.d(TAG, "Vibración activada durante 2000ms");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("El número de programa no coincide con el primer número de programa registrado. ¿Desea continuar?")
                .setPositiveButton("Sí", (dialog, which) -> runOnUiThread(() -> {
                    qr.setText("");
                    contadorTextView.setText(String.valueOf(contador));
                    contadorTextView2.setText(String.valueOf(contador_rep));
                    Log.d(TAG, "Usuario seleccionó 'Sí' en la alerta de programas no coincidentes");
                }))
                .setNegativeButton("No", (dialog, which) -> Log.d(TAG, "Usuario seleccionó 'No' en la alerta de programas no coincidentes"))
                .show();
    }
    private void fetchCurrentTimeFromAPI() {
        String url = "https://worldtimeapi.org/api/timezone/Etc/UTC"; // Cambiar a HTTPS

        // Realizar la solicitud HTTP en un hilo separado
        new Thread(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Analizar el JSON de respuesta para obtener la fecha y hora actual
                    JSONObject jsonObject = new JSONObject(response.toString());
                    String dateTime = jsonObject.getString("datetime");
                    // Ejemplo: "2024-10-04T19:13:11.708762+00:00"
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.getDefault());
                    Date currentDate = dateFormat.parse(dateTime);

                    // Verificar si la suscripción ha expirado
                    runOnUiThread(() -> {
                        isSubscriptionExpired(currentDate);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener la hora de la API", e);
                runOnUiThread(() -> {
                    showAlert("Error", "Error al obtener la hora de la API");
                });
            }
        }).start();
    }



    // Método para verificar si la suscripción ha expirado
    private boolean isSubscriptionExpired(Date currentDate) {
        // Formato de la fecha para analizar la fecha de expiración
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            // Convertir la fecha de expiración de String a Date
            Date expiration = dateFormat.parse(expirationDate);

            // Comparar la fecha actual con la fecha de expiración
            boolean expired = currentDate.after(expiration);

            // Calcular la diferencia en milisegundos
            long differenceInMillis = expiration.getTime() - currentDate.getTime();
            long differenceInDays = TimeUnit.DAYS.convert(differenceInMillis, TimeUnit.MILLISECONDS);

            if (expired) {
                Log.w(TAG, "La suscripción ha expirado");
                // Muestra un alert dialog si la suscripción ha expirado
                showAlert("Suscripción Expirada", "La suscripción ha expirado.");
            } else {
                Log.d(TAG, "La suscripción está vigente");
                // Mostrar mensaje si quedan 60 días o menos
                if (differenceInDays <= 60) {
                    String message = "Quedan " + differenceInDays + " días para la expiración.";
                    Log.d(TAG, message);
                    // Muestra un alert dialog si quedan 60 días o menos
                    showAlert_fecha("Aviso de Expiración", message);
                }
            }
            return expired;
        } catch (ParseException e) {
            Log.e(TAG, "Error al analizar la fecha de expiración", e);
            return false; // En caso de error al analizar la fecha, retornamos falso
        }
    }

    private void showAlert(String title2, String message2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title2);
        builder.setMessage(message2);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", (dialog, which) -> finish()); // Cerrar la aplicación al hacer clic en OK
        builder.show();
    }

    private void showAlert_fecha(String title2, String message2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title2);
        builder.setMessage(message2);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss()); // Cerrar el diálogo al hacer clic en OK
        builder.show();
    }
}
