package com.example.leitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class eventosInscritos extends AppCompatActivity {

    private ListView listViewEventosInscritos;
    private EventoAdapter adapter;
    private List<Evento> eventosList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private Button btnVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventos_inscritos);


        btnVoltar = findViewById(R.id.btnVoltar);
        mAuth = FirebaseAuth.getInstance();
        listViewEventosInscritos = findViewById(R.id.listViewEventosInscritos);

        // Inicializa o adapter com o layout padrão do Android
        adapter = new EventoAdapter(this, eventosList);
        listViewEventosInscritos.setAdapter(adapter);

        btnVoltar.setOnClickListener(v -> finish());
        carregarEventosInscritos();
    }

    private void carregarEventosInscritos() {
        String uid = mAuth.getCurrentUser().getUid();

        DatabaseReference userInscricoesRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("eventos_inscritos");

        userInscricoesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> chavesEventos = new ArrayList<>();

                // Coleta todas as chaves de eventos inscritos
                for (DataSnapshot inscricaoSnapshot : snapshot.getChildren()) {
                    chavesEventos.add(inscricaoSnapshot.getKey());
                }

                carregarDetalhesDosEventos(chavesEventos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(eventosInscritos.this,
                        "Erro: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarDetalhesDosEventos(List<String> chavesEventos) {
        DatabaseReference eventosRef = FirebaseDatabase.getInstance().getReference("eventos");

        eventosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventosList.clear();

                for (String chaveComposta : chavesEventos) {
                    String[] partes = chaveComposta.split("_");
                    if (partes.length == 2) {
                        String uidDono = partes[0];
                        String eventoId = partes[1];

                        // Busca o evento específico
                        DataSnapshot eventoSnapshot = snapshot.child(uidDono).child(eventoId);
                        if (eventoSnapshot.exists()) {
                            Evento evento = eventoSnapshot.getValue(Evento.class);
                            if (evento != null) {
                                eventosList.add(evento);
                            }
                        }
                    }
                }

                // Notifica o adapter que os dados mudaram
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(eventosInscritos.this,
                        "Erro ao carregar eventos: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}