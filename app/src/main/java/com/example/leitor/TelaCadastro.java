package com.example.leitor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class TelaCadastro extends AppCompatActivity {

    private TextInputEditText edtNome, edtEmail, edtSenha, edtConfirmarSenha;
    private MaterialButton btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_cadastro);

        // Inicializa componentes
        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenha);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        // Botão Cadastrar
        btnCadastrar.setOnClickListener(v -> {
            if (validarCampos()) {
                cadastrarUsuario();
            }
        });

        // Texto "Já tem conta?"
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
        // Implemente seu cadastro aqui (Firebase, API, etc)
        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}