package com.example.leitor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class TelaManutencaoEvento extends AppCompatActivity {

    private EditText edtNome, edtDescricao, edtEndereco;
    private EditText edtDataInicio, edtDataTermino;
    private ImageView imgQrCode;
    private Button btnAtualizar;
    private Button btnExcluir, btnVoltar;
    private DatabaseReference databaseRef;
    private DatabaseReference publicRef;
    private String uid;
    private Evento eventoEdicao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_manutencao_evento);

        uid = getIntent().getStringExtra("uid");
        if (uid == null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) uid = user.getUid();
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("eventos").child(uid);
        publicRef = FirebaseDatabase.getInstance().getReference("eventosPublicos");

        edtNome = findViewById(R.id.edtNomeEvento);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtEndereco = findViewById(R.id.edtEndereco);
        edtDataInicio = findViewById(R.id.edtDataInicio);
        edtDataTermino = findViewById(R.id.edtDataTermino);
        imgQrCode = findViewById(R.id.imgQrCode);
        btnAtualizar = findViewById(R.id.btnAtualizar);
        btnExcluir = findViewById(R.id.btnExcluir);
        btnVoltar = findViewById(R.id.btnVoltar);

        String eventoId = getIntent().getStringExtra("eventoId");
        if (eventoId != null) {
            carregarEventoParaEdicao(eventoId);
        }

        btnAtualizar.setOnClickListener(v -> salvarEvento());
        btnExcluir.setOnClickListener(v -> excluirEvento());
        btnVoltar.setOnClickListener(v -> finish());
    }

    private void carregarEventoParaEdicao(String eventoId) {
        databaseRef.child(eventoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventoEdicao = snapshot.getValue(Evento.class);
                if (eventoEdicao != null) {
                    eventoEdicao.setId(snapshot.getKey());
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

        if (evento.getQrCodeBase64() != null && !evento.getQrCodeBase64().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(evento.getQrCodeBase64(), Base64.DEFAULT);
                Bitmap qrBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imgQrCode.setImageBitmap(qrBitmap);
                imgQrCode.setVisibility(View.VISIBLE);
            } catch (IllegalArgumentException e) {
                Log.e("QR_CODE", "Erro ao decodificar QR Code", e);
                imgQrCode.setVisibility(View.GONE);
            }
        } else {
            imgQrCode.setVisibility(View.GONE);
        }
    }

    private void salvarEvento() {
        String nome = edtNome.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();
        String endereco = edtEndereco.getText().toString().trim();
        String dataInicio = edtDataInicio.getText().toString().trim();
        String dataTermino = edtDataTermino.getText().toString().trim();

        if (validarCampos(nome, dataInicio, dataTermino)) {
            String qrCodeBase64 = "IVBORw0KGgoAAAANSUHEUgAAAAJYCAYAAAC+ZpjcAAAAAXNSR0IArs4c60AAAAARzOkIUCAglCHwiZIgAAASLSURBVHic7dhBr";

            Evento evento;
            if (eventoEdicao != null) {
                evento = eventoEdicao;
                evento.setNome(nome);
                evento.setDescricao(descricao);
                evento.setEndereco(endereco);
                evento.setDataInicio(dataInicio);
                evento.setDataTermino(dataTermino);
                if (evento.getQrCodeBase64() == null) {
                    evento.setQrCodeBase64(qrCodeBase64);
                }
            } else {
                String id = UUID.randomUUID().toString();
                evento = new Evento(id, nome, dataInicio, dataTermino, endereco, descricao, qrCodeBase64);
                evento.setId(id);
            }

            // Salvar no nó do usuário
            databaseRef.child(evento.getId()).setValue(evento)
                    .addOnSuccessListener(aVoid -> {
                        // Salvar no nó público
                        publicRef.child(evento.getId()).setValue(evento)
                                .addOnSuccessListener(aVoid1 -> {
                                    // Atualizar nas inscrições de todos os usuários
                                    atualizarEventoNosInscritos(evento);
                                    Toast.makeText(TelaManutencaoEvento.this, "Evento salvo com sucesso!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(TelaManutencaoEvento.this, "Erro ao salvar evento público", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(TelaManutencaoEvento.this, "Erro ao salvar evento", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void atualizarEventoNosInscritos(Evento eventoAtualizado) {
        DatabaseReference inscricoesRef = FirebaseDatabase.getInstance()
                .getReference("usuarios");

        inscricoesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.hasChild("inscricaoEvento/" + eventoAtualizado.getId())) {
                        userSnapshot.child("inscricaoEvento")
                                .child(eventoAtualizado.getId())
                                .getRef()
                                .setValue(eventoAtualizado)
                                .addOnFailureListener(e -> {
                                    Log.e("UPDATE_EVENT", "Erro ao atualizar inscrição: " + e.getMessage());
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UPDATE_EVENT", "Erro ao atualizar inscrições: " + error.getMessage());
            }
        });
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

    private void excluirEvento() {
        if (eventoEdicao == null || eventoEdicao.getId() == null) {
            Toast.makeText(this, "Nenhum evento selecionado para exclusão", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseRef.child(eventoEdicao.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    publicRef.child(eventoEdicao.getId()).removeValue()
                            .addOnSuccessListener(aVoid1 -> {
                                removerEventoDasInscricoes(eventoEdicao.getId());
                                Toast.makeText(TelaManutencaoEvento.this, "Evento excluído com sucesso", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(TelaManutencaoEvento.this, "Erro ao excluir evento público", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TelaManutencaoEvento.this, "Erro ao excluir evento", Toast.LENGTH_SHORT).show();
                });
    }

    private void removerEventoDasInscricoes(String eventoId) {
        DatabaseReference inscricoesRef = FirebaseDatabase.getInstance()
                .getReference("usuarios");

        inscricoesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.hasChild("inscricaoEvento/" + eventoId)) {
                        userSnapshot.child("inscricaoEvento")
                                .child(eventoId)
                                .getRef()
                                .removeValue()
                                .addOnFailureListener(e -> {
                                    Log.e("REMOVE_EVENT", "Erro ao remover inscrição: " + e.getMessage());
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("REMOVE_EVENT", "Erro ao remover inscrições: " + error.getMessage());
            }
        });
    }
}