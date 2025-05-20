package com.example.leitor;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText edtEmail, edtSenha;
    private Button btnLogin;
    private TextView txtCadastrar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        btnLogin = findViewById(R.id.btnLogin);
        txtCadastrar = findViewById(R.id.txtCadastrar);

        txtCadastrar.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TelaCadastro.class));
        });

        btnLogin.setOnClickListener(v -> validarLogin());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verifica se usuário já está logado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, tela_home.class));
            finish();
        }
    }

    private void validarLogin() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, tela_home.class));
                        finish();
                    } else {
                        mostrarDialogoErroLogin();
                    }
                });
    }

    private void mostrarDialogoErroLogin() {
        new AlertDialog.Builder(this)
                .setTitle("Login Falhou")
                .setMessage("Email ou senha incorretos. Deseja se cadastrar ou verificar os dados?")
                .setPositiveButton("Cadastrar", (dialog, which) -> {
                    startActivity(new Intent(MainActivity.this, TelaCadastro.class));
                })
                .setNegativeButton("Verificar Dados", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}