package com.example.leitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ParticipantesEventoActivity extends AppCompatActivity {

    private ListView listViewParticipantes;
    private ParticipanteAdapter participanteAdapter;  // Adapter que criaremos para mostrar os participantes
    private List<usuario> participantesList = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener usuariosListener;
    private Button btnVoltar;

    private String eventoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participantes_evento);

        listViewParticipantes = findViewById(R.id.listViewParticipantes);
        Button btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        // Pega o eventoId passado pela Intent
        eventoId = getIntent().getStringExtra("eventoId");
        if (eventoId == null || eventoId.isEmpty()) {
            Toast.makeText(this, "ID do evento não fornecido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        participanteAdapter = new ParticipanteAdapter(this, participantesList);
        listViewParticipantes.setAdapter(participanteAdapter);

        carregarParticipantes();
    }

    private void carregarParticipantes() {
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        usuariosListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                participantesList.clear();

                for (DataSnapshot usuarioSnapshot : snapshot.getChildren()) {
                    // Verifica se o usuário está inscrito nesse evento
                    DataSnapshot inscricaoEventoSnapshot = usuarioSnapshot.child("inscricaoEvento").child(eventoId);
                    if (inscricaoEventoSnapshot.exists()) {
                        String nome = usuarioSnapshot.child("nome").getValue(String.class);
                        String email = usuarioSnapshot.child("email").getValue(String.class);
                        String uid = usuarioSnapshot.getKey();

                        usuario usuario = new usuario();
                        usuario.setNome(nome);
                        usuario.setEmail(email);
                        usuario.setUid(uid);

                        participantesList.add(usuario);
                    }
                }

                if (participantesList.isEmpty()) {
                    Toast.makeText(ParticipantesEventoActivity.this, "Nenhum participante encontrado.", Toast.LENGTH_SHORT).show();
                }
                participanteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParticipantesEventoActivity.this,
                        "Erro ao carregar participantes: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        usuariosRef.addValueEventListener(usuariosListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuariosRef != null && usuariosListener != null) {
            usuariosRef.removeEventListener(usuariosListener);
        }
    }
}