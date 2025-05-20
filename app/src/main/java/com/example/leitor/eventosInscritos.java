package com.example.leitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class eventosInscritos extends AppCompatActivity {

    private ListView listViewEventosInscritos;
    private Button btnVoltar;
    private List<Evento> eventosList;
    private EventoAdapter adapter;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "EventosPrefs";
    private static final String EVENTOS_IDS_KEY = "eventos_ids";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventos_inscritos);

        listViewEventosInscritos = findViewById(R.id.listViewEventosInscritos);
        btnVoltar = findViewById(R.id.btnVoltar);
        eventosList = new ArrayList<>();
        adapter = new EventoAdapter(this, eventosList);
        listViewEventosInscritos.setAdapter(adapter);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        listViewEventosInscritos.setOnItemClickListener((parent, view, position, id) -> {
            Evento eventoSelecionado = eventosList.get(position);

            Intent intent = new Intent(eventosInscritos.this, DetalhesEventoActivity.class);
            intent.putExtra("nomeEvento", eventoSelecionado.getNome());
            intent.putExtra("dataInicio", eventoSelecionado.getDataInicio());
            intent.putExtra("dataTermino", eventoSelecionado.getDataTermino());
            intent.putExtra("endereco", eventoSelecionado.getEndereco());
            intent.putExtra("descricao", eventoSelecionado.getDescricao());
            intent.putExtra("qrCodeBase64", eventoSelecionado.getQrCodeBase64());

            startActivity(intent);
        });

        // Se vier um evento novo do leitor, adiciona ele à lista salva
        String eventoId = getIntent().getStringExtra("eventoId");
        if (eventoId != null && !eventoId.isEmpty()) {
            salvarEventoId(eventoId);
        }

        // Carregar todos os eventos armazenados localmente
        carregarEventosSalvos();

        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(eventosInscritos.this, tela_home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    // Salvar novo ID escaneado no SharedPreferences
    private void salvarEventoId(String eventoId) {
        Set<String> idsSalvos = sharedPreferences.getStringSet(EVENTOS_IDS_KEY, new HashSet<>());
        Set<String> novosIds = new HashSet<>(idsSalvos);
        novosIds.add(eventoId);

        sharedPreferences.edit().putStringSet(EVENTOS_IDS_KEY, novosIds).apply();
    }

    // Carrega todos os eventos já salvos
    private void carregarEventosSalvos() {
        Set<String> idsSalvos = sharedPreferences.getStringSet(EVENTOS_IDS_KEY, new HashSet<>());
        if (idsSalvos.isEmpty()) {
            Toast.makeText(this, "Você ainda não se inscreveu em nenhum evento", Toast.LENGTH_SHORT).show();
            return;
        }

        eventosList.clear();

        for (String id : idsSalvos) {
            DatabaseReference eventoRef = FirebaseDatabase.getInstance().getReference("eventos").child(id);
            eventoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Evento evento = snapshot.getValue(Evento.class);
                    if (evento != null) {
                        eventosList.add(evento);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(eventosInscritos.this, "Erro ao carregar evento", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
