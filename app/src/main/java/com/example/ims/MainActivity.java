package com.example.ims;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import android.view.View;
import android.widget.ImageButton;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    EditText n_palet;
    EditText n_cajas;
    EditText qr;
    ImageButton saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        n_palet = findViewById(R.id.txtpalet);
        n_cajas = findViewById(R.id.txtcajas);
        qr = findViewById(R.id.txtcodigo);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los valores de los EditText
                String palet = n_palet.getText().toString();
                String cajas = n_cajas.getText().toString();
                String codigoQR = qr.getText().toString();

                // Insertar los datos en la base de datos
                insertarDatos(palet, cajas, codigoQR);
            }
        });
    }

    // Método para insertar datos en la base de datos
    private void insertarDatos(String n_palet, String n_cajas, String qr) {
        Connection connection = null;
        PreparedStatement statement = null;

        // Configuración para permitir conexiones en el hilo principal (No recomendado en producción)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            // Establecer la conexión con la base de datos
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String url = "jdbc:jtds:sqlserver://192.168.0.101:1344/Db_Pimpihue";
            String username = "sa";
            String password = "12345678";
            connection = DriverManager.getConnection(url, username, password);

            // Consulta SQL para insertar datos
            String sql = "INSERT INTO DETALLES_LECTURAS (n_palet, n_cajas, qr) VALUES (?, ?, ?)";
            statement = connection.prepareStatement(sql);
            statement.setString(1, n_palet);
            statement.setString(2, n_cajas);
            statement.setString(3, qr);

            // Ejecutar la consulta
            statement.executeUpdate();
            Log.d("DBHelper", "Datos insertados correctamente.");
        } catch (ClassNotFoundException e) {
            Log.e("DBHelper", "Error al cargar el controlador JDBC.", e);
        } catch (SQLException e) {
            Log.e("DBHelper", "Error al establecer la conexión con la base de datos o al insertar datos.", e);
        } finally {
            // Cerrar la conexión y el statement
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                Log.e("DBHelper", "Error al cerrar la conexión o el statement.", e);
            }
        }
    }
}
