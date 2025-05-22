package com.example.leitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetalhesEventoActivity extends AppCompatActivity {

    private TextView tvNome, tvDataInicio, tvDataTermino, tvEndereco, tvDescricao;
    private ImageView imageQrCode;
    private Button btnVoltar;
    private DatabaseReference eventoPublicoRef;
    private String eventoId;
    private ValueEventListener valueEventListener;
    private Button btnVerParticipantes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_evento);

        btnVerParticipantes = findViewById(R.id.btnVerParticipantes);

        tvNome = findViewById(R.id.tvNome);
        tvDataInicio = findViewById(R.id.tvDataInicio);
        tvDataTermino = findViewById(R.id.tvDataTermino);
        tvEndereco = findViewById(R.id.tvEndereco);
        tvDescricao = findViewById(R.id.tvDescricao);
        imageQrCode = findViewById(R.id.imageQRCode);
        btnVoltar = findViewById(R.id.btnVoltar);


        Intent intent = getIntent();
        String nome = intent.getStringExtra("nome");
        String dataInicio = intent.getStringExtra("dataInicio");
        String dataTermino = intent.getStringExtra("dataTermino");
        String endereco = intent.getStringExtra("endereco");
        String descricao = intent.getStringExtra("descricao");
        String qrCodeBase64 = intent.getStringExtra("qrCodeBase64");
        eventoId = intent.getStringExtra("eventoId");

        mostrarDadosEvento(nome, dataInicio, dataTermino, endereco, descricao, qrCodeBase64);

        if (eventoId != null && !eventoId.isEmpty()) {
            configurarListenerAtualizacoes();
        }

        btnVoltar.setOnClickListener(v -> finish());
        btnVerParticipantes.setOnClickListener(v -> {
            if (eventoId != null && !eventoId.isEmpty()) {
                Intent it = new Intent(DetalhesEventoActivity.this, ParticipantesEventoActivity.class);
                it.putExtra("eventoId", eventoId);
                startActivity(it);
            } else {
                Toast.makeText(this, "ID do evento não disponível", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void mostrarDadosEvento(String nome, String dataInicio, String dataTermino,
                                    String endereco, String descricao, String qrCodeBase64) {
        tvNome.setText("Nome: " + nome);
        tvDataInicio.setText("Início: " + dataInicio);
        tvDataTermino.setText("Término: " + dataTermino);
        tvEndereco.setText("Endereço: " + endereco);
        tvDescricao.setText("Descrição: " + descricao);

        if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(qrCodeBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imageQrCode.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                Log.e("QR_CODE", "Erro ao decodificar QR Code", e);
            }
        }
    }

    private void configurarListenerAtualizacoes() {
        eventoPublicoRef = FirebaseDatabase.getInstance().getReference("eventosPublicos").child(eventoId);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(DetalhesEventoActivity.this,
                            "Este evento foi removido pelo organizador",
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                Evento eventoAtualizado = snapshot.getValue(Evento.class);
                if (eventoAtualizado != null) {
                    mostrarDadosEvento(
                            eventoAtualizado.getNome(),
                            eventoAtualizado.getDataInicio(),
                            eventoAtualizado.getDataTermino(),
                            eventoAtualizado.getEndereco(),
                            eventoAtualizado.getDescricao(),
                            eventoAtualizado.getQrCodeBase64()
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetalhesEventoActivity.this,
                        "Erro ao verificar atualizações",
                        Toast.LENGTH_SHORT).show();
            }
        };

        eventoPublicoRef.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventoPublicoRef != null && valueEventListener != null) {
            eventoPublicoRef.removeEventListener(valueEventListener);
        }
    }
}