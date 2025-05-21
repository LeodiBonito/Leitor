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
    private ArrayList<String> listaIdsEventos;
    private ArrayAdapter<String> adapter;

    private DatabaseReference eventosRef, usuariosRef;
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
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);

        listaEventos = new ArrayList<>();
        listaIdsEventos = new ArrayList<>();

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, listaEventos);
        listViewMeusEventos.setAdapter(adapter);

        carregarEventosDoFirebase();

        listViewMeusEventos.setOnItemClickListener((parent, view, position, id) -> {
            String eventoId = listaIdsEventos.get(position);

            Intent intent = new Intent(meusEventos.this, TelaManutencaoEvento.class);
            intent.putExtra("eventoId", eventoId);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });

        btnVoltar.setOnClickListener(v -> finish());
    }

    private void carregarEventosDoFirebase() {
        eventosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listaEventos.clear();
                listaIdsEventos.clear();

                int contadorEventos = 0;

                for (DataSnapshot eventoSnapshot : snapshot.getChildren()) {
                    String id = eventoSnapshot.getKey();
                    String nomeEvento = eventoSnapshot.child("nome").getValue(String.class);

                    if (id != null && nomeEvento != null) {
                        listaEventos.add(nomeEvento);
                        listaIdsEventos.add(id);
                        contadorEventos++;
                    }
                }

                // Atualiza o contador de eventos no nó do usuário
                usuariosRef.child("eventosCriados").setValue(contadorEventos);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(meusEventos.this, "Erro ao carregar eventos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método opcional para adicionar evento manualmente na lista (não necessário com Firebase)
    public void adicionarEvento(String novoEvento) {
        listaEventos.add(novoEvento);
        adapter.notifyDataSetChanged();
    }
}