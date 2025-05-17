package com.example.leitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class gerarQrCode extends AppCompatActivity {

    EditText editTextNomeEvento, editTextDataHoraInicio, editTextDataHoraTermino, editTextEndereco, editTextDescricao;
    Button buttonGenerate, btnVoltar, btnSalvar;
    ImageView imageViewQRCode;
    private Bitmap qrCodeBitmap;

    private TextWatcher createDateTimeFormatter(final EditText editText) {
        return new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().replaceAll("[^\\d]", "");
                StringBuilder formatted = new StringBuilder();

                int len = input.length();
                for (int i = 0; i < len && i < 12; i++) {
                    formatted.append(input.charAt(i));
                    if (i == 1 || i == 3) formatted.append('/');
                    if (i == 7) formatted.append('-');
                    if (i == 9) formatted.append(':');
                }

                editText.setText(formatted.toString());
                editText.setSelection(editText.getText().length());

                isFormatting = false;
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerar_qr_code);

        editTextNomeEvento = findViewById(R.id.editTextNomeEvento);
        editTextDataHoraInicio = findViewById(R.id.editTextDataHoraInicio);
        editTextDataHoraTermino = findViewById(R.id.editTextDataHoraTermino);
        editTextEndereco = findViewById(R.id.editTextEndereco);
        editTextDescricao = findViewById(R.id.editTextDescricao);
        buttonGenerate = findViewById(R.id.buttonGenerate);
        btnSalvar = findViewById(R.id.btnSalvar);
        imageViewQRCode = findViewById(R.id.imageViewQRCode);
        btnVoltar = findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(v -> finish());

        InputFilter dateTimeFilter = new InputFilter() {
            Pattern mPattern = Pattern.compile("\\d{0,2}/?\\d{0,2}/?\\d{0,4}\\-?\\d{0,2}:?\\d{0,2}");

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String result = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());
                Matcher matcher = mPattern.matcher(result);
                if (!matcher.matches()) return "";
                return null;
            }
        };

        editTextDataHoraInicio.setFilters(new InputFilter[]{dateTimeFilter});
        editTextDataHoraTermino.setFilters(new InputFilter[]{dateTimeFilter});

        editTextDataHoraInicio.addTextChangedListener(createDateTimeFormatter(editTextDataHoraInicio));
        editTextDataHoraTermino.addTextChangedListener(createDateTimeFormatter(editTextDataHoraTermino));

        buttonGenerate.setOnClickListener(v -> {
            String nomeEvento = editTextNomeEvento.getText().toString().trim();
            String dataHoraInicio = editTextDataHoraInicio.getText().toString().trim();
            String dataHoraTermino = editTextDataHoraTermino.getText().toString().trim();
            String endereco = editTextEndereco.getText().toString().trim();
            String descricao = editTextDescricao.getText().toString().trim();

            if (nomeEvento.isEmpty() || dataHoraInicio.isEmpty() || dataHoraTermino.isEmpty() || endereco.isEmpty() || descricao.isEmpty()) {
                Toast.makeText(gerarQrCode.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            String conteudoQRCodeJson = String.format(
                    "{\"evento\":\"%s\",\"inicio\":\"%s\",\"termino\":\"%s\",\"endereco\":\"%s\",\"descricao\":\"%s\"}",
                    nomeEvento, dataHoraInicio, dataHoraTermino, endereco, descricao
            );

            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                qrCodeBitmap = barcodeEncoder.encodeBitmap(
                        conteudoQRCodeJson,
                        BarcodeFormat.QR_CODE,
                        600, 600
                );
                imageViewQRCode.setImageBitmap(qrCodeBitmap);
                Toast.makeText(gerarQrCode.this, "QR Code gerado com sucesso!", Toast.LENGTH_SHORT).show();
            } catch (WriterException e) {
                e.printStackTrace();
                Toast.makeText(gerarQrCode.this, "Erro ao gerar QR Code", Toast.LENGTH_SHORT).show();
            }
        });

        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(gerarQrCode.this, tela_home.class);
            startActivity(intent);
            finish(); // Opcional - remove a activity da pilha
        });

        btnSalvar.setOnClickListener(v -> {
            if (qrCodeBitmap == null) {
                Toast.makeText(this, "Gere o QR Code antes de salvar", Toast.LENGTH_SHORT).show();
                return;
            }

            String nomeEvento = editTextNomeEvento.getText().toString().trim();
            if (nomeEvento.isEmpty()) {
                Toast.makeText(this, "O nome do evento é obrigatório", Toast.LENGTH_SHORT).show();
                return;
            }

            salvarEvento(nomeEvento);
        });
    }

    private void salvarEvento(String nomeEvento) {
        // Referência do banco de dados
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference eventosRef = database.getReference("eventos");

        // Cria um novo ID único
        String eventoId = eventosRef.push().getKey();

        // Cria objeto Evento
        Evento novoEvento = new Evento(
                nomeEvento,
                editTextDataHoraInicio.getText().toString(),
                editTextDataHoraTermino.getText().toString(),
                editTextEndereco.getText().toString(),
                editTextDescricao.getText().toString()
        );
        novoEvento.setId(eventoId);

        // Salva no Firebase
        eventosRef.child(eventoId).setValue(novoEvento)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Evento salvo com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}