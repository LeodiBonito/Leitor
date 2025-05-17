package com.example.leitor;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private EditText edtEmail, edtSenha;
    private Button btnLogin;
    private TextView txtCadastrar;



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
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> validarLogin());


    }
    private void validarLogin() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        // Validação básica dos campos
        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        } else if(email.equals("root") || senha.equals("root") ) {
            startActivity(new Intent(MainActivity.this, tela_home.class));
        }
        //if {
        //} ligar com o else a baixo quando tiver banco de dados;
         else {
            // Login falhou - mostra mensagem
            mostrarDialogoErroLogin();
        }
    }

    private void mostrarDialogoErroLogin() {
        new AlertDialog.Builder(this)
                .setTitle("Login Falhou")
                .setMessage("Email ou senha incorretos. Deseja se cadastrar ou verificar os dados?")
                .setPositiveButton("Cadastrar", (dialog, which) -> {
                    Intent intent = new Intent(MainActivity.this, TelaCadastro.class);
                    startActivity(intent);
                })
                .setNegativeButton("Verificar Dados", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


}