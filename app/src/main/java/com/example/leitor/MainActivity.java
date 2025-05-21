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

import java.util.List;

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

        txtCadastrar.setOnClickListener(v -> verificarEmailAntesDeCadastrar());

        btnLogin.setOnClickListener(v -> validarLogin());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Comente esta linha se não quiser login automático
        // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // if (user != null) {
        //    startActivity(new Intent(this, tela_home.class));
        //    finish();
        // }
    }

    private void verificarEmailAntesDeCadastrar() {
        String email = edtEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Digite um e-mail para cadastrar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verifica se o e-mail já está cadastrado
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();
                        if (signInMethods != null && !signInMethods.isEmpty()) {
                            // E-mail já cadastrado
                            new AlertDialog.Builder(this)
                                    .setTitle("E-mail já cadastrado")
                                    .setMessage("Este e-mail já está registrado. Deseja fazer login?")
                                    .setPositiveButton("Sim", (dialog, which) -> {
                                        // Foca no campo de senha para facilitar o login
                                        edtSenha.requestFocus();
                                    })
                                    .setNegativeButton("Não", null)
                                    .show();
                        } else {
                            // E-mail disponível, prossegue para cadastro
                            startActivity(new Intent(MainActivity.this, TelaCadastro.class));
                        }
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Erro ao verificar e-mail: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
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
                    verificarEmailAntesDeCadastrar();
                })
                .setNegativeButton("Verificar Dados", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}