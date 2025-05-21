package com.example.leitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
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

    private DatabaseReference eventosRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meus_eventos);

        btnVoltar = findViewById(R.id.btnVoltar);
        listViewMeusEventos = findViewById(R.id.listViewMeusEventos);

        auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();
        eventosRef = FirebaseDatabase.getInstance().getReference("eventos").child(uid);

        listaEventos = new ArrayList<>();

        // Configura o adapter
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, listaEventos);
        listViewMeusEventos.setAdapter(adapter);

        // Carrega os eventos do Firebase
        carregarEventosDoFirebase();

        // Configura o clique nos itens da lista
        listViewMeusEventos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String eventoSelecionado = listaEventos.get(position);

                Intent intent = new Intent(meusEventos.this, TelaManutencaoEvento.class);
                intent.putExtra("evento", eventoSelecionado);
                intent.putExtra("posicao", position);

                startActivity(intent);
            }
        });

        btnVoltar.setOnClickListener(v -> finish());
    }

    // Método para buscar eventos do Firebase
    private void carregarEventosDoFirebase() {
        eventosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listaEventos.clear();
                for (DataSnapshot eventoSnapshot : snapshot.getChildren()) {
                    String nomeEvento = eventoSnapshot.child("nome").getValue(String.class);
                    if (nomeEvento != null) {
                        listaEventos.add(nomeEvento);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(meusEventos.this, "Erro ao carregar eventos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método opcional para adicionar evento manualmente
    public void adicionarEvento(String novoEvento) {
        listaEventos.add(novoEvento);
        adapter.notifyDataSetChanged();
    }
}