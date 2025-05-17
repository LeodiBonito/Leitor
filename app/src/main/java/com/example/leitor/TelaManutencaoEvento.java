package com.example.leitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TelaManutencaoEvento extends AppCompatActivity {
    private EditText edtEvento;
    private Button btnExcluir, btnAlterar;
    private int posicao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_manutencao_evento);

        edtEvento = findViewById(R.id.edtEvento);
        btnExcluir = findViewById(R.id.btnExcluir);
        btnAlterar = findViewById(R.id.btnAlterar);

        // Recupera os dados do Intent
        Intent intent = getIntent();
        if (intent != null) {
            String evento = intent.getStringExtra("evento");
            posicao = intent.getIntExtra("posicao", -1);

            edtEvento.setText(evento);
        }

        btnExcluir.setOnClickListener(v -> {
            // Retorna para meusEventos com a posição a ser removida
            Intent resultIntent = new Intent();
            resultIntent.putExtra("posicao", posicao);
            resultIntent.putExtra("acao", "excluir");
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        btnAlterar.setOnClickListener(v -> {
            String eventoAlterado = edtEvento.getText().toString().trim();

            if (eventoAlterado.isEmpty()) {
                Toast.makeText(this, "Digite um nome para o evento", Toast.LENGTH_SHORT).show();
                return;
            }

            // Retorna para meusEventos com a posição e novo nome
            Intent resultIntent = new Intent();
            resultIntent.putExtra("posicao", posicao);
            resultIntent.putExtra("evento", eventoAlterado);
            resultIntent.putExtra("acao", "alterar");
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    public void voltar(View view) {
        finish();
    }
}