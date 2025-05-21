package com.example.leitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TelaCadastro extends AppCompatActivity {

    private TextInputEditText edtNome, edtEmail, edtSenha, edtConfirmarSenha;
    private MaterialButton btnCadastrar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_cadastro);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("usuarios");

        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenha);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        btnCadastrar.setOnClickListener(v -> {
            if (validarCampos()) {
                cadastrarUsuario();
            }
        });

        findViewById(R.id.txtLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private boolean validarCampos() {
        String nome = edtNome.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();
        String confirmarSenha = edtConfirmarSenha.getText().toString().trim();

        if (nome.isEmpty()) {
            edtNome.setError("Nome é obrigatório");
            return false;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email inválido");
            return false;
        }

        if (senha.isEmpty() || senha.length() < 6) {
            edtSenha.setError("Senha deve ter 6+ caracteres");
            return false;
        }

        if (!senha.equals(confirmarSenha)) {
            edtConfirmarSenha.setError("Senhas não coincidem");
            return false;
        }

        return true;
    }

    private void cadastrarUsuario() {
        String nome = edtNome.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        // 1. Verifica se o e-mail está vazio
        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Verifica se o e-mail já existe ANTES de cadastrar
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();

                        if (signInMethods != null && !signInMethods.isEmpty()) {
                            // E-mail já cadastrado
                            Toast.makeText(TelaCadastro.this,
                                    "Este e-mail já está cadastrado",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // 3. E-mail disponível - prossegue com cadastro
                            realizarCadastro(email, senha, nome);
                        }
                    } else {
                        Toast.makeText(TelaCadastro.this,
                                "Erro ao verificar e-mail: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void realizarCadastro(String email, String senha, String nome) {
        // 1. Força logout para limpar qualquer sessão residual
        FirebaseAuth.getInstance().signOut();

        // 2. Cria a conta
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 3. Salva dados adicionais no Database
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            salvarDadosUsuarioNoDatabase(user.getUid(), email, nome);
                        }
                    } else {
                        // Tratamento específico para erros de cadastro
                        String errorMessage = "Erro no cadastro";
                        if (task.getException() != null) {
                            if (task.getException().getMessage().contains("already in use")) {
                                errorMessage = "E-mail já cadastrado (conflito pós-verificação)";
                            } else {
                                errorMessage = task.getException().getMessage();
                            }
                        }
                        Toast.makeText(TelaCadastro.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void salvarDadosUsuarioNoDatabase(String uid, String email, String nome) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nome", nome);
        usuario.put("email", email);
        usuario.put("data_cadastro", ServerValue.TIMESTAMP); // Usa timestamp do servidor

        FirebaseDatabase.getInstance().getReference("usuarios")
                .child(uid)
                .setValue(usuario)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TelaCadastro.this, "Cadastro realizado! Faça login.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(TelaCadastro.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TelaCadastro.this, "Erro ao salvar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
