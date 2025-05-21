package com.example.leitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DetalhesEventoActivity extends AppCompatActivity {

    private TextView tvNome, tvDataInicio, tvDataTermino, tvEndereco, tvDescricao;
    private ImageView imageQrCode;
    private Button btnVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_evento);

        tvNome = findViewById(R.id.tvNome);
        tvDataInicio = findViewById(R.id.tvDataInicio);
        tvDataTermino = findViewById(R.id.tvDataTermino);
        tvEndereco = findViewById(R.id.tvEndereco);
        tvDescricao = findViewById(R.id.tvDescricao);
        imageQrCode = findViewById(R.id.imageQRCode);
        btnVoltar = findViewById(R.id.btnVoltar);

        // 🔄 Recuperar dados passados pela Intent
        Intent intent = getIntent();
        String nome = intent.getStringExtra("nome");
        String dataInicio = intent.getStringExtra("dataInicio");
        String dataTermino = intent.getStringExtra("dataTermino");
        String endereco = intent.getStringExtra("endereco");
        String descricao = intent.getStringExtra("descricao");
        String qrCodeBase64 = intent.getStringExtra("qrCodeBase64");

        // 📝 Mostrar os dados nos TextViews
        tvNome.setText("Nome: " + nome);
        tvDataInicio.setText("Início: " + dataInicio);
        tvDataTermino.setText("Término: " + dataTermino);
        tvEndereco.setText("Endereço: " + endereco);
        tvDescricao.setText("Descrição: " + descricao);

        // 📷 Decodificar o QR Code e mostrar na ImageView
        if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
            byte[] decodedBytes = Base64.decode(qrCodeBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imageQrCode.setImageBitmap(bitmap);
        }

        btnVoltar.setOnClickListener(v -> finish());
    }
}
