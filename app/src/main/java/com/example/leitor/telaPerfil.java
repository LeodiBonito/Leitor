package com.example.leitor;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class telaPerfil extends AppCompatActivity {

    private ImageView imageViewPerfil;
    private TextView txtEventosCriados,txtNome, txtEmail;
    private Button btnVoltar, btnSair;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_perfil);

        // Inicializa Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios");

        // Vincula elementos da interface

        txtEventosCriados = findViewById(R.id.txtEventosCriados);
        txtNome = findViewById(R.id.txtNome);
        txtEmail = findViewById(R.id.txtEmail);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnSair = findViewById(R.id.btnSair);

        // Eventos dos botões
        btnVoltar.setOnClickListener(v -> voltar());
        btnSair.setOnClickListener(v -> sair());

        // Carregar dados do usuário
        carregarDadosUsuario();
    }

    private void carregarDadosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            databaseReference.child(uid).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String nome = snapshot.child("nome").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    Long eventosCriados = snapshot.child("eventosCriados").getValue(Long.class);
                    Long eventosParticipados = snapshot.child("eventosParticipados").getValue(Long.class);

                    txtNome.setText(nome != null ? nome : "Nome não encontrado");
                    txtEmail.setText(email != null ? email : "Email não encontrado");

                    txtEventosCriados.setText(eventosCriados != null ? String.valueOf(eventosCriados) : "0");

                } else {
                    Toast.makeText(telaPerfil.this, "Usuário não encontrado no banco de dados", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(telaPerfil.this, "Erro ao buscar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(telaPerfil.this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    private void voltar() {
        finish();
    }

    private void sair() {
        new AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Deseja realmente sair?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(telaPerfil.this, MainActivity.class));
                    finishAffinity();
                })
                .setNegativeButton("Não", null)
                .show();
    }
}