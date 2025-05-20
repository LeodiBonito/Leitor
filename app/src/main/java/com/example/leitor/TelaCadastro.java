package com.example.leitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // ✅ IMPORTADO PARA DEBUG
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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

        // 🔧 Inicializa Firebase Auth e nó 'usuarios' do Firebase Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("usuarios");

        // Inicializa componentes da tela
        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenha);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        // Botão cadastrar
        btnCadastrar.setOnClickListener(v -> {
            if (validarCampos()) {
                cadastrarUsuario();
            }
        });

        // Link para tela de login
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

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Map<String, Object> usuario = new HashMap<>();
                            usuario.put("nome", nome);
                            usuario.put("email", email);
                            usuario.put("senha", senha); // ⚠️ Apenas para teste
                            usuario.put("data_cadastro", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

                            mDatabase.child(user.getUid())
                                    .setValue(usuario)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(TelaCadastro.this, "Cadastro realizado!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(TelaCadastro.this, tela_home.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FIREBASE_DEBUG", "Erro ao salvar dados no Realtime Database", e); // ✅ DEBUG
                                        Toast.makeText(TelaCadastro.this, "Erro ao salvar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Log.e("FIREBASE_DEBUG", "Usuário retornado é nulo após cadastro."); // ✅ DEBUG
                        }
                    } else {
                        Log.e("FIREBASE_AUTH", "Erro ao criar usuário", task.getException()); // ✅ DEBUG
                        Toast.makeText(TelaCadastro.this, "Erro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
