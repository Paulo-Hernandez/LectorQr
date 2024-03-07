package com.example.ims;

import android.text.TextWatcher;
import android.os.Bundle;
import android.view.View;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.text.Editable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import java.util.HashMap;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    EditText n_palet;
    EditText n_cajas;
    EditText qr;
    EditText sevEditText;
    ImageButton saveButton;
    TextView contadorTextView;
    TextView contadorTextView2;
    int contador = 0;
    int contador_rep = 0;
    List<RegistroTemporal> registrosTemporales = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        n_palet = findViewById(R.id.txtpalet);
        n_cajas = findViewById(R.id.txtcajas);
        qr = findViewById(R.id.txtcodigo);
        sevEditText = findViewById(R.id.ipsev);
        saveButton = findViewById(R.id.saveButton);
        contadorTextView = findViewById(R.id.contadorTextView);
        contadorTextView2 = findViewById(R.id.repetida);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener la dirección IP del servidor
                String serverAddress = sevEditText.getText().toString();

                // Verificar si la dirección IP está vacía
                if (serverAddress.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, ingrese la dirección IP del servidor", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Crear un hilo para enviar la lista de registros temporales al servidor
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Establecer la conexión TCP con el servidor
                            Socket socket = new Socket(serverAddress, 8080);

                            // Crear un flujo de salida de objetos para enviar la lista al servidor
                            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                            // Enviar la lista de registros temporales al servidor
                            oos.writeObject(registrosTemporales);
                            oos.flush();

                            // Cerrar la conexión y el flujo de datos
                            oos.close();
                            socket.close();
                        } catch (UnknownHostException e) {
                            // Manejar el error de host desconocido
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Error: Host desconocido", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (IOException e) {
                            // Manejar cualquier otra excepción de E/S
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Error de E/S", Toast.LENGTH_SHORT).show();
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

            // Declarar un HashMap para almacenar los códigos QR y la cantidad de repeticiones
            HashMap<String, Integer> qrRepetidos = new HashMap<>();

            @Override
            public void afterTextChanged(Editable s) {
                String palet = n_palet.getText().toString();
                String cajas = n_cajas.getText().toString();
                String codigoQR = qr.getText().toString();

                // Verificar si algún campo está vacío o si la longitud del código QR no es igual a 15
                if (!palet.isEmpty() && !cajas.isEmpty() && codigoQR.length() == 15) {
                    // Agregar el registro a la lista de registros temporales
                    RegistroTemporal registro = new RegistroTemporal(palet, cajas, codigoQR);
                    registrosTemporales.add(registro);

                    if (qrRepetidos.containsKey(codigoQR)) {
                        // Aumentar el contador de repeticiones
                        contador_rep++;
                        contadorTextView2.setText(String.valueOf(contador_rep));
                    } else {
                        // Incrementar el contador y actualizar la vista
                        contador++;
                        contadorTextView.setText(String.valueOf(contador));
                    }
                    // Limpiar el campo de texto de QR
                    qr.setText("");
                } else {
                    // Mostrar un mensaje indicando que los datos son inválidos
                    Toast.makeText(MainActivity.this, "Los datos son inválidos. Asegúrese de que todos los campos estén llenos y que el código QR tenga una longitud de 15 caracteres.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

class RegistroTemporal {
    private String palet;
    private String cajas;
    private String qr;

    public RegistroTemporal(String palet, String cajas, String qr) {
        this.palet = palet;
        this.cajas = cajas;
        this.qr = qr;
    }

    public String getPalet() {
        return palet;
    }

    public String getCajas() {
        return cajas;
    }

    public String getCodigoQR() {
        return qr;
    }
}

