package com.example.leitor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetalhesEventoActivity extends AppCompatActivity {

    private TextView tvNomeEvento, tvDataInicio, tvDataTermino, tvEndereco, tvDescricao;
    private ImageView imageViewQRCode;
    private Button btnVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_evento);

        // Inicializar views
        tvNomeEvento = findViewById(R.id.tvNomeEvento);
        tvDataInicio = findViewById(R.id.tvDataInicio);
        tvDataTermino = findViewById(R.id.tvDataTermino);
        tvEndereco = findViewById(R.id.tvEndereco);
        tvDescricao = findViewById(R.id.tvDescricao);
        imageViewQRCode = findViewById(R.id.imageViewQRCode);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Obter dados do intent
        Intent intent = getIntent();
        tvNomeEvento.setText(intent.getStringExtra("nomeEvento"));
        tvDataInicio.setText(intent.getStringExtra("dataInicio"));
        tvDataTermino.setText(intent.getStringExtra("dataTermino"));
        tvEndereco.setText(intent.getStringExtra("endereco"));
        tvDescricao.setText(intent.getStringExtra("descricao"));

        // Carregar QR Code
        String qrCodeBase64 = intent.getStringExtra("qrCodeBase64");
        if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
            byte[] decodedString = Base64.decode(qrCodeBase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageViewQRCode.setImageBitmap(decodedByte);
        }

        btnVoltar.setOnClickListener(v -> finish());
    }
}