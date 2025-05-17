package com.example.leitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class meusEventos extends AppCompatActivity {
    private Button btnVoltar;
    private ListView listViewMeusEventos;
    private ArrayList<String> listaEventos;
    private ArrayAdapter<String> adapter;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_eventos);

        btnVoltar = findViewById(R.id.btnVoltar);
        listViewMeusEventos = findViewById(R.id.listViewMeusEventos);

        // Inicializa o Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("eventos");

        // Inicializa a lista de eventos
        listaEventos = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaEventos);
        listViewMeusEventos.setAdapter(adapter);

        // Carrega os eventos do Firebase
        carregarEventosDoFirebase();

        // Configura o clique nos itens da lista
        listViewMeusEventos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                abrirTelaManutencao(position);
            }
        });

        btnVoltar.setOnClickListener(v -> finish());
    }

    private void carregarEventosDoFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaEventos.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Supondo que você tenha uma classe Evento com getNome()
                    Evento evento = snapshot.getValue(Evento.class);
                    if (evento != null) {
                        String eventoFormatado = formatarEventoParaLista(evento);
                        listaEventos.add(eventoFormatado);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(meusEventos.this,
                        "Falha ao carregar eventos: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatarEventoParaLista(Evento evento) {
        return evento.getNome() + "\n" +
                "Início: " + evento.getDataInicio() + "\n" +
                "Término: " + evento.getDataTermino();
    }

    private void abrirTelaManutencao(int position) {
        Intent intent = new Intent(this, TelaManutencaoEvento.class);
        intent.putExtra("posicao", position);

        // Se você quiser passar o ID do evento também:
        // String eventoId = listaIdsEventos.get(position);
        // intent.putExtra("eventoId", eventoId);

        startActivity(intent);
    }

    // Método para adicionar um novo evento (se ainda necessário)
    public void adicionarEvento(String novoEvento) {
        listaEventos.add(novoEvento);
        adapter.notifyDataSetChanged();
    }
}