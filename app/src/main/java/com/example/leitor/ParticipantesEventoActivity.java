package com.example.leitor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.leitor.ParticipanteAdapter;
import com.example.leitor.R;
import com.example.leitor.usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ParticipantesEventoActivity extends AppCompatActivity {

    private ListView listViewParticipantes;
    private List<usuario> listaParticipantes;
    private ParticipanteAdapter adapter;

    private DatabaseReference participantesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participantes_evento);

        listViewParticipantes = findViewById(R.id.listViewParticipantes);
        listaParticipantes = new ArrayList<>();
        adapter = new ParticipanteAdapter(this, listaParticipantes);
        listViewParticipantes.setAdapter(adapter);

        // Recebe o ID do evento enviado pela tela anterior
        String eventoId = getIntent().getStringExtra("eventoId");

        if (eventoId != null) {
            participantesRef = FirebaseDatabase.getInstance().getReference("usuarios");
            carregarParticipantes(eventoId);
        } else {
            Toast.makeText(this, "ID do evento não encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarParticipantes(String eventoId) {
        participantesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaParticipantes.clear();
                for (DataSnapshot usuarioSnapshot : dataSnapshot.getChildren()) {
                    String uid = usuarioSnapshot.getKey();
                    String nome = usuarioSnapshot.child("nome").getValue(String.class);
                    String email = usuarioSnapshot.child("email").getValue(String.class);

                    // Verifica se o usuário está inscrito no evento
                    DataSnapshot inscricaoEventoSnapshot = usuarioSnapshot.child("inscricaoEvento").child(eventoId);
                    if (inscricaoEventoSnapshot.exists()) {
                        String horaEntrada = inscricaoEventoSnapshot.child("horaEntrada").getValue(String.class);
                        String horaSaida = inscricaoEventoSnapshot.child("horaSaida").getValue(String.class);

                        usuario usuario = new usuario();
                        usuario.setUid(uid);
                        usuario.setNome(nome);
                        usuario.setEmail(email);
                        usuario.setHoraEntrada(horaEntrada);
                        usuario.setHoraSaida(horaSaida);

                        listaParticipantes.add(usuario);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ParticipantesEventoActivity.this, "Erro ao carregar participantes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
