package com.example.leitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class meusEventos extends AppCompatActivity {
    private Button btnVoltar;
    private ListView listViewMeusEventos;
    private ArrayList<String> listaEventos;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meus_eventos);

        btnVoltar = findViewById(R.id.btnVoltar);
        listViewMeusEventos = findViewById(R.id.listViewMeusEventos);

        // Inicializa a lista de eventos (substitua por seus dados reais)
        listaEventos = new ArrayList<>();

        // Configura o adapter
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, listaEventos);
        listViewMeusEventos.setAdapter(adapter);

        // Configura o clique nos itens da lista
        listViewMeusEventos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Recupera o evento selecionado
                String eventoSelecionado = listaEventos.get(position);

                // Cria a intenção para abrir a tela de manutenção
                Intent intent = new Intent(meusEventos.this, TelaManutencaoEvento.class);

                // Passa os dados do evento para a tela de manutenção
                intent.putExtra("evento", eventoSelecionado);
                intent.putExtra("posicao", position);

                startActivity(intent);
            }
        });

        btnVoltar.setOnClickListener(v -> finish());
    }

    // Método para adicionar um novo evento à lista
    public void adicionarEvento(String novoEvento) {
        listaEventos.add(novoEvento);
        adapter.notifyDataSetChanged();
    }
}