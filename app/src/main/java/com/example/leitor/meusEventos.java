package com.example.leitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.example.leitor.Evento;

public class meusEventos extends AppCompatActivity {
    private DatabaseReference databaseRef;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> eventosList = new ArrayList<>();
    private List<Evento> eventosObjList = new ArrayList<>(); // Lista para armazenar objetos Evento completos
    private ListView listView;
    private TextView txtSemEventos;
    private Button btnVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_eventos);

        try {
            FirebaseApp.initializeApp(this);
            databaseRef = FirebaseDatabase.getInstance().getReference("eventos");

            listView = findViewById(R.id.listViewMeusEventos);
            txtSemEventos = findViewById(R.id.txtSemEventos);
            btnVoltar = findViewById(R.id.btnVoltar);

            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventosList);
            listView.setAdapter(adapter);

            btnVoltar.setOnClickListener(v -> finish());

            // Configurar clique nos itens da lista
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    abrirTelaManutencao(position);
                }
            });

            carregarEventos();

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao inicializar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }
    private void abrirTelaManutencao(int position) {
        if (position >= 0 && position < eventosObjList.size()) {
            Evento evento = eventosObjList.get(position);
            Log.d("EventoDebug", "QR Code exists: " + (evento.getQrCodeBase64() != null));
            Intent intent = new Intent(this, TelaManutencaoEvento.class);
            intent.putExtra("eventoId", evento.getId());
            intent.putExtra("eventoNome", evento.getNome());
            intent.putExtra("dataInicio", evento.getDataInicio());
            intent.putExtra("dataTermino", evento.getDataTermino());
            intent.putExtra("endereco", evento.getEndereco());
            intent.putExtra("descricao", evento.getDescricao());
            intent.putExtra("qrCodeBase64", evento.getQrCodeBase64());
            startActivity(intent);
        }
    }

    private void carregarEventos() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventosList.clear();
                eventosObjList.clear();

                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Evento evento = data.getValue(Evento.class);
                        if (evento != null && evento.getNome() != null) {
                            evento.setId(data.getKey()); // Armazena o ID do Firebase
                            eventosList.add(evento.getNome());
                            eventosObjList.add(evento);
                        }
                    }
                    txtSemEventos.setVisibility(View.GONE);
                } else {
                    txtSemEventos.setVisibility(View.VISIBLE);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(meusEventos.this, "Erro ao carregar eventos: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}