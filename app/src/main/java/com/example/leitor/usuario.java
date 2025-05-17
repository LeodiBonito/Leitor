package com.example.leitor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class usuario extends AppCompatActivity {

    private Button btnVoltar,btnSair;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_usuario);

        Button btnSair = findViewById(R.id.btnSair);
        btnSair.setOnClickListener(v -> {finish();});

        Button btnVoltar = findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(usuario.this, tela_home.class);
            finish();
            startActivity(intent);
        });

    }


}