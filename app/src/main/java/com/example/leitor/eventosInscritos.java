package com.example.leitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
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

        adapter = new EventoAdapter(this, eventosList);
        listViewEventosInscritos.setAdapter(adapter);

        btnVoltar.setOnClickListener(v -> finish());

        carregarEventosInscritos();

        // 👉 Clique no item da lista para abrir tela de detalhes
        listViewEventosInscritos.setOnItemClickListener((parent, view, position, id) -> {
            Evento eventoSelecionado = eventosList.get(position);

            Intent intent = new Intent(eventosInscritos.this, DetalhesEventoActivity.class);
            intent.putExtra("nome", eventoSelecionado.getNome());
            intent.putExtra("dataInicio", eventoSelecionado.getDataInicio());
            intent.putExtra("dataTermino", eventoSelecionado.getDataTermino());
            intent.putExtra("endereco", eventoSelecionado.getEndereco());
            intent.putExtra("descricao", eventoSelecionado.getDescricao());
            intent.putExtra("qrCodeBase64", eventoSelecionado.getQrCodeBase64());

            startActivity(intent);
        });
    }

    private void carregarEventosInscritos() {
        String uid = mAuth.getCurrentUser().getUid();

        DatabaseReference eventosRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("inscricaoEvento");

        eventosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventosList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot eventoSnapshot : snapshot.getChildren()) {
                        Evento evento = eventoSnapshot.getValue(Evento.class);
                        if (evento != null) {
                            eventosList.add(evento);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(eventosInscritos.this,
                            "Nenhum evento inscrito.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(eventosInscritos.this,
                        "Erro: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
