package com.example.ims;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ImageView;
import android.content.Intent;
import android.net.Uri;

public class welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        // Referencias a las imágenes
        ImageView welcomeImage = findViewById(R.id.welcome);
        ImageView ingImage = findViewById(R.id.ing);

        // Configurar el clic en la imagen de welcome para abrir MainActivity
        welcomeImage.setOnClickListener(view -> {
            Intent intent = new Intent(welcome.this, MainActivity.class);
            startActivity(intent);
        });

        ingImage.setOnClickListener(view -> {
            String url = "https://ingmetrica.cl";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}