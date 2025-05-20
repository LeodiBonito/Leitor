// TELA GERAR QR CODE
package com.example.leitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
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
import android.util.Base64;
import java.io.ByteArrayOutputStream;

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

            if (nomeEvento.isEmpty() || dataHoraInicio.isEmpty() || dataHoraTermino.isEmpty() ||
                    endereco.isEmpty() || descricao.isEmpty()) {
                Toast.makeText(gerarQrCode.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gerar um ID único para o evento
            String eventoId = UUID.randomUUID().toString();

            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                qrCodeBitmap = barcodeEncoder.encodeBitmap(
                        eventoId,  // Apenas o ID do evento como conteúdo do QR Code
                        BarcodeFormat.QR_CODE,
                        600, 600
                );
                imageViewQRCode.setImageBitmap(qrCodeBitmap);
                Toast.makeText(gerarQrCode.this, "QR Code gerado com sucesso!", Toast.LENGTH_SHORT).show();

                // Armazena o ID que foi usado no QR Code para usar ao salvar
                editTextNomeEvento.setTag(eventoId);

            } catch (WriterException e) {
                e.printStackTrace();
                Toast.makeText(gerarQrCode.this, "Erro ao gerar QR Code", Toast.LENGTH_SHORT).show();
            }
        });

        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(gerarQrCode.this, tela_home.class);
            startActivity(intent);
            finish();
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
        // Recupera o ID que foi gerado para o QR Code
        String eventoId = (String) editTextNomeEvento.getTag();

        if (eventoId == null || eventoId.isEmpty()) {
            Toast.makeText(this, "Gere o QR Code antes de salvar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Referência do banco de dados
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference eventosRef = database.getReference("eventos");

        // Converte o QR Code Bitmap para Base64
        String qrCodeBase64 = "";
        if (qrCodeBitmap != null) {
            try {
                Log.d("QR_DEBUG", "Iniciando conversão do QR Code para Base64");
                Log.d("QR_DEBUG", "Dimensões do Bitmap: " + qrCodeBitmap.getWidth() + "x" + qrCodeBitmap.getHeight());

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                boolean compressSuccess = qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                Log.d("QR_DEBUG", "Compressão do Bitmap: " + (compressSuccess ? "SUCESSO" : "FALHA"));

                byte[] byteArray = byteArrayOutputStream.toByteArray();
                qrCodeBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

                Log.d("QR_DEBUG", "Conversão para Base64 completa");
                Log.d("QR_DEBUG", "Tamanho do Base64: " + qrCodeBase64.length() + " caracteres");
                Log.d("QR_DEBUG", "Primeiros 20 chars: " + qrCodeBase64.substring(0, Math.min(qrCodeBase64.length(), 20)));

            } catch (Exception e) {
                Log.e("QR_DEBUG", "Erro na conversão do QR Code: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e("QR_DEBUG", "qrCodeBitmap é NULO - Não foi possível gerar QR Code");
        }

        // Cria objeto Evento
        Evento novoEvento = new Evento();
        novoEvento.setId(eventoId);
        novoEvento.setNome(nomeEvento);
        novoEvento.setDataInicio(editTextDataHoraInicio.getText().toString());
        novoEvento.setDataTermino(editTextDataHoraTermino.getText().toString());
        novoEvento.setEndereco(editTextEndereco.getText().toString());
        novoEvento.setDescricao(editTextDescricao.getText().toString());
        novoEvento.setQrCodeBase64(qrCodeBase64);

        // Salva no Firebase
        eventosRef.child(eventoId).setValue(novoEvento)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Evento e QR Code salvos com sucesso!", Toast.LENGTH_SHORT).show();

                    // Limpa os campos após salvar
                    editTextNomeEvento.setText("");
                    editTextDataHoraInicio.setText("");
                    editTextDataHoraTermino.setText("");
                    editTextEndereco.setText("");
                    editTextDescricao.setText("");
                    imageViewQRCode.setImageBitmap(null);
                    qrCodeBitmap = null;
                    editTextNomeEvento.setTag(null); // Limpa o ID armazenado
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}