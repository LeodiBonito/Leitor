package com.example.leitor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class TelaManutencaoEvento extends AppCompatActivity {

    private EditText edtNome, edtDescricao, edtEndereco;
    private EditText edtDataInicio, edtDataTermino;
    private Button btnAtualizar;
    private DatabaseReference databaseRef;
    private FirebaseUser user;
    private Evento eventoEdicao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_manutencao_evento);

        // Inicializa Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference("eventos");

        // Inicializa componentes
        edtNome = findViewById(R.id.edtNomeEvento);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtEndereco = findViewById(R.id.edtEndereco);
        edtDataInicio = findViewById(R.id.edtDataInicio);
        edtDataTermino = findViewById(R.id.edtDataTermino);
        btnAtualizar = findViewById(R.id.btnAtualizar);

        // Verifica se é edição
        String eventoId = getIntent().getStringExtra("EVENTO_ID");
        if (eventoId != null) {
            carregarEventoParaEdicao(eventoId);
        }

        btnAtualizar.setOnClickListener(v -> salvarEvento());
    }

    private void carregarEventoParaEdicao(String eventoId) {
        databaseRef.child(eventoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventoEdicao = snapshot.getValue(Evento.class);
                if (eventoEdicao != null) {
                    preencherCampos(eventoEdicao);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TelaManutencaoEvento.this, "Erro ao carregar evento", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preencherCampos(Evento evento) {
        edtNome.setText(evento.getNome());
        edtDescricao.setText(evento.getDescricao());
        edtEndereco.setText(evento.getEndereco());
        edtDataInicio.setText(evento.getDataInicio());
        edtDataTermino.setText(evento.getDataTermino());
    }

    private void salvarEvento() {
        String nome = edtNome.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();
        String endereco = edtEndereco.getText().toString().trim();
        String dataInicio = edtDataInicio.getText().toString().trim();
        String dataTermino = edtDataTermino.getText().toString().trim();

        if (validarCampos(nome, dataInicio, dataTermino)) {
            // Gerar QR Code (simplificado - na prática gere um código real)
            String qrCodeBase64 = "IVBORw0KGgoAAAANSUHEUgAAAAJYCAYAAAC+ZpjcAAAAAXNSR0IArs4c60AAAAARzOkIUCAglCHwiZIgAAASLSURBVHic7dhBr";

            Evento evento;
            if (eventoEdicao != null) {
                // Edição
                evento = eventoEdicao;
                evento.setNome(nome);
                evento.setDescricao(descricao);
                evento.setEndereco(endereco);
                evento.setDataInicio(dataInicio);
                evento.setDataTermino(dataTermino);
            } else {
                // Novo evento
                String id = UUID.randomUUID().toString();
                evento = new Evento(id,nome,dataInicio,dataTermino,endereco,descricao,qrCodeBase64);
                evento.setId(id);
            }

            databaseRef.child(evento.getId()).setValue(evento)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Evento salvo com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao salvar evento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private boolean validarCampos(String nome, String dataInicio, String dataTermino) {
        if (nome.isEmpty()) {
            edtNome.setError("Nome é obrigatório");
            return false;
        }
        if (dataInicio.isEmpty()) {
            edtDataInicio.setError("Data de início é obrigatória");
            return false;
        }
        if (dataTermino.isEmpty()) {
            edtDataTermino.setError("Data de término é obrigatória");
            return false;
        }
        return true;
    }
}