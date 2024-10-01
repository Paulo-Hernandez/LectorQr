package com.example.ims;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConfigActivity extends AppCompatActivity {

    private static final String CONFIG_FILE_NAME = "config.txt";
    private EditText editIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_config);

        // Manejo de Insets (barras del sistema)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialización del EditText y Button
        editIp = findViewById(R.id.edit_ip);
        Button btnGuardar = findViewById(R.id.btn_guardar);

        // Cargar la IP desde el archivo de configuración y mostrarla en el EditText
        loadIpFromConfigFile();

        // Listener para el botón Guardar
        btnGuardar.setOnClickListener(v -> {
            String newIp = editIp.getText().toString();
            if (!newIp.isEmpty()) {
                showSaveConfirmationDialog(newIp);
            } else {
                Toast.makeText(this, "Por favor, ingrese una IP válida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadIpFromConfigFile() {
        try (FileInputStream fis = openFileInput(CONFIG_FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ip=")) {
                    String ip = line.substring(3); // Extraer la IP después de "ip="
                    editIp.setText(ip); // Mostrar la IP en el EditText
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Si no se puede leer el archivo, mostrar un mensaje o dejar el EditText vacío
            editIp.setHint("Ingrese IP");
        }
    }

    private void showSaveConfirmationDialog(String newIp) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Desea guardar la nueva IP?")
                .setPositiveButton("Sí", (dialog, which) -> saveConfigFileAndGoToMain(newIp))
                .setNegativeButton("No", null)
                .show();
    }

    private void saveConfigFileAndGoToMain(String ip) {
        String content = "ip=" + ip;
        try (FileOutputStream fos = openFileOutput(CONFIG_FILE_NAME, MODE_PRIVATE)) {
            fos.write(content.getBytes());
            Toast.makeText(this, "IP guardada correctamente", Toast.LENGTH_SHORT).show();
            // Ir a la MainActivity después de guardar la IP
            Intent intent = new Intent(ConfigActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Cierra la ConfigActivity
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar la IP", Toast.LENGTH_SHORT).show();
        }
    }
}

