package com.example.leitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

public class TelaManutencaoEvento extends AppCompatActivity {
    private EditText edtNomeEvento, edtDataInicio, edtDataTermino, edtEndereco, edtDescricao;
    private ImageView imageViewQrCode;
    private Button btnExcluir, btnAtualizar, btnVoltar;
    private DatabaseReference databaseRef;
    private String eventoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_manutencao_evento);

        // Inicializa Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("eventos");

        // Vincula os componentes
        edtNomeEvento = findViewById(R.id.edtNomeEvento);
        edtDataInicio = findViewById(R.id.edtDataInicio);
        edtDataTermino = findViewById(R.id.edtDataTermino);
        edtEndereco = findViewById(R.id.edtEndereco);
        edtDescricao = findViewById(R.id.edtDescricao);
        imageViewQrCode = findViewById(R.id.imageViewQrCode);
        btnExcluir = findViewById(R.id.btnExcluir);
        btnAtualizar = findViewById(R.id.btnAtualizar);
        btnVoltar = findViewById(R.id.btnVoltar);


        // Recupera os dados do Intent
        Intent intent = getIntent();
        if (intent != null) {
            eventoId = intent.getStringExtra("eventoId");
            edtNomeEvento.setText(intent.getStringExtra("eventoNome"));
            edtDataInicio.setText(intent.getStringExtra("dataInicio"));
            edtDataTermino.setText(intent.getStringExtra("dataTermino"));
            edtEndereco.setText(intent.getStringExtra("endereco"));
            edtDescricao.setText(intent.getStringExtra("descricao"));

            String qrCodeBase64 = intent.getStringExtra("qrCodeBase64");
            if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(qrCodeBase64, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                    if (decodedBitmap != null) {
                        imageViewQrCode.setImageBitmap(decodedBitmap);
                        Log.d("QR_DEBUG", "QR Code exibido com sucesso");
                    } else {
                        Log.e("QR_DEBUG", "Falha ao decodificar Bitmap");
                    }
                } catch (Exception e) {
                    Log.e("QR_DEBUG", "Erro ao decodificar QR Code: " + e.getMessage());
                }
            } else {
                Log.e("QR_DEBUG", "QR Code Base64 está vazio ou nulo");
            }
        }

        btnExcluir.setOnClickListener(v -> excluirEvento());
        btnAtualizar.setOnClickListener(v -> alterarEvento());
        btnVoltar.setOnClickListener(v -> finish());
    }

    private void excluirEvento() {
        if (eventoId != null && !eventoId.isEmpty()) {
            databaseRef.child(eventoId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Evento excluído com sucesso", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao excluir evento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Erro: ID do evento não encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void alterarEvento() {
        String nome = edtNomeEvento.getText().toString().trim();
        String dataInicio = edtDataInicio.getText().toString().trim();
        String dataTermino = edtDataTermino.getText().toString().trim();
        String endereco = edtEndereco.getText().toString().trim();
        String descricao = edtDescricao.getText().toString().trim();

        if (nome.isEmpty() || dataInicio.isEmpty() || dataTermino.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventoId != null && !eventoId.isEmpty()) {
            Evento evento = new Evento();
            evento.setId(eventoId);
            evento.setNome(nome);
            evento.setDataInicio(dataInicio);
            evento.setDataTermino(dataTermino);
            evento.setEndereco(endereco);
            evento.setDescricao(descricao);

            databaseRef.child(eventoId).setValue(evento)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Evento atualizado com sucesso", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao atualizar evento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Erro: ID do evento não encontrado", Toast.LENGTH_SHORT).show();
        }
    }
}