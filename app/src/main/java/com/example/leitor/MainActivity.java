package com.example.leitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Habilitar edge-to-edge antes de definir o layout
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Configurar o listener para as barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Listener para o texto "Cadastre-se"
        TextView txtCadastrar = findViewById(R.id.txtCadastrar);
        txtCadastrar.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TelaCadastro.class));
        });

        // Listener para o botão de Login
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, tela_home.class)); // Nome de classe corrigido
        });
    }
}