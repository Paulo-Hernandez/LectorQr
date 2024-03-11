package com.example.ims;

import android.os.Bundle;
import android.view.View;
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
import java.net.Socket;
import java.util.HashMap;
import android.text.TextWatcher;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    EditText n_palet;
    EditText n_cajas;
    EditText qr;
    EditText sevEditText;
    ImageButton saveButton;
    ImageButton delButton;
    ImageButton penButton;
    TextView contadorTextView;
    TextView contadorTextView2;
    int contador = 0;
    int contador_rep = 0;
    List<String> codigosValidos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        n_palet = findViewById(R.id.txtpalet);
        n_cajas = findViewById(R.id.txtcajas);
        qr = findViewById(R.id.txtcodigo);
        sevEditText = findViewById(R.id.ipsev);
        contadorTextView = findViewById(R.id.contadorTextView);
        contadorTextView2 = findViewById(R.id.repetida);
        // Botones
        saveButton = findViewById(R.id.saveButton);
        delButton = findViewById(R.id.deleteButton);
        penButton = findViewById(R.id.pendeButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cuando se hace clic en el botón "saveButton", enviar al servidor un mensaje "1" por el puerto 9000
                String palet = n_palet.getText().toString();
                String cajas = n_cajas.getText().toString();

                // Verificar si el contador es igual al número de cajas
                if (contador == Integer.parseInt(cajas)) {
                    // Si es igual, enviar al servidor un mensaje "1" por el puerto 9000
                    sendMessageToServer("1", palet, 9000);
                    contador_rep=0;
                    contadorTextView2.setText(String.valueOf(contador_rep));
                    contador=0;
                    contadorTextView.setText(String.valueOf(contador));
                    codigosValidos.clear();
                    qr.setText("");
                    n_cajas.setText("");
                    n_palet.setText("");
                    Toast.makeText(MainActivity.this, "El palet con número " + palet + " se ha GUARDADO", Toast.LENGTH_SHORT).show();
                } else {
                    // Si no es igual, mostrar un mensaje de advertencia
                    Toast.makeText(MainActivity.this, "Faltan cajas por contar", Toast.LENGTH_SHORT).show();
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
                                contadorTextView2.setText(String.valueOf(contador_rep));
                                contador=0;
                                contadorTextView.setText(String.valueOf(contador));
                                codigosValidos.clear();
                                qr.setText("");
                                n_cajas.setText("");
                                n_palet.setText("");
                                Toast.makeText(MainActivity.this, "El palet con número " + palet + " se ha borrado", Toast.LENGTH_SHORT).show();
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
                        contadorTextView2.setText(String.valueOf(contador_rep));
                        contador=0;
                        contadorTextView.setText(String.valueOf(contador));
                        codigosValidos.clear();
                        qr.setText("");
                        n_cajas.setText("");
                        n_palet.setText("");
                        Toast.makeText(MainActivity.this, "El palet con número " + palet + " se ha borrado", Toast.LENGTH_SHORT).show();
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

            // Declarar un HashMap para almacenar los códigos QR y la cantidad de repeticiones
            HashMap<String, Integer> qrRepetidos = new HashMap<>();

            @Override
            public void afterTextChanged(Editable s) {
                // Obtener la dirección IP del servidor
                String serverAddress = sevEditText.getText().toString();

                // Verificar si la dirección IP está vacía
                if (serverAddress.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, ingrese la dirección IP del servidor", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Obtener los datos de palet, cajas y código QR
                String palet = n_palet.getText().toString();
                String cajas = n_cajas.getText().toString();
                String codigoQR = qr.getText().toString();

                // Verificar si la longitud del código QR es 14
                if (codigoQR.length() == 14) {
                    // Verificar si el código ya existe en la lista de códigos válidos
                    String numeroProgramaActual = codigoQR.substring(3, 7);
                    if (codigosValidos.isEmpty() || numeroProgramaActual.equals(codigosValidos.get(0).substring(3, 8))) {
                        // Verificar si el código ya existe en la lista de códigos válidos
                        if (codigosValidos.contains(codigoQR)) {
                            qr.setText("");
                            // Si existe, incrementar contador de repeticiones
                            contador_rep++;
                            contadorTextView2.setText(String.valueOf(contador_rep));
                        } else {
                            // Si no existe, agregarlo a la lista de códigos válidos y aumentar contador
                            codigosValidos.add(codigoQR);
                            contador++;
                            contadorTextView.setText(String.valueOf(contador));

                            // Crear un hilo para enviar los datos al servidor
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        // Establecer conexión con el servidor en el puerto 8000
                                        Socket socket = new Socket(serverAddress, 8000);

                                        // Obtener el OutputStream del socket para enviar datos al servidor
                                        OutputStream outputStream = socket.getOutputStream();
                                        PrintWriter writer = new PrintWriter(outputStream);

                                        // Enviar los datos de palet, cajas y código QR al servidor
                                        writer.println(palet + "," + cajas + "," + codigoQR);
                                        writer.flush();

                                        // Limpiar el campo de texto de QR
                                        qr.setText("");

                                        // Cerrar la conexión
                                        socket.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            thread.start();
                        }
                    } else {
                        // Si el número de programa no coincide con el primer número de programa registrado,
                        // mostrar un mensaje de alerta y hacer vibrar el teléfono
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null) {
                            vibrator.vibrate(1000);
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("El número de programa no coincide con el primer número de programa registrado. ¿Desea continuar?")
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        qr.setText("");
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Aquí no realizas ninguna acción o manejas el caso de cancelación
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                } else {
                    // Si la longitud del código QR no es 14, muestra un mensaje de error

                }
            }

        });
    }
    // Función para enviar un mensaje al servidor
    private void sendMessageToServer(String message, String palet, int port) {
        // Obtener la dirección IP del servidor
        String serverAddress = sevEditText.getText().toString();

        // Verificar si la dirección IP está vacía
        if (serverAddress.isEmpty()) {
            Toast.makeText(MainActivity.this, "Por favor, ingrese la dirección IP del servidor", Toast.LENGTH_SHORT).show();
            return;
        }

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

                    // Cerrar la conexión
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}





