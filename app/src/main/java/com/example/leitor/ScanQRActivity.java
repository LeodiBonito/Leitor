package com.example.leitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanQRActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;
    private FirebaseAuth mAuth;

    private boolean isProcessing = false; // ✔️ Evita múltiplas leituras do mesmo QR

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qractivity);

        mAuth = FirebaseAuth.getInstance();

        surfaceView = findViewById(R.id.surfaceView);
        Button btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            initializeQRScanner();
        }
    }

    private void initializeQRScanner() {
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        if (!barcodeDetector.isOperational()) {
            Toast.makeText(this, "Não foi possível configurar o leitor QR", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScanQRActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(holder);
                    }
                } catch (IOException e) {
                    Log.e("Camera Error", "Erro ao iniciar câmera", e);
                    Toast.makeText(ScanQRActivity.this, "Erro ao iniciar câmera", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {}

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();

                if (qrCodes.size() > 0 && !isProcessing) {
                    isProcessing = true;

                    final String eventoId = qrCodes.valueAt(0).displayValue.trim(); // ✔️ QR com ID do evento

                    if (!eventoId.isEmpty()) {
                        salvarEventoInscrito(eventoId);
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(ScanQRActivity.this, "QR Code inválido", Toast.LENGTH_SHORT).show();
                            isProcessing = false;
                        });
                    }
                }
            }
        });
    }

    private void salvarEventoInscrito(String eventoId) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("inscricaoEvento")
                .child(eventoId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                if (snapshot.exists()) {
                    // Já está inscrito → atualiza hora de saída
                    userRef.child("horaSaida").setValue(currentTime).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            runOnUiThread(() -> {
                                Toast.makeText(ScanQRActivity.this, "Saída registrada!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(ScanQRActivity.this, "Erro ao registrar saída", Toast.LENGTH_SHORT).show();
                                isProcessing = false;
                            });
                        }
                    });
                } else {
                    // 🔥 Busca evento em eventosPublicos para registrar inscrição nova com hora de entrada
                    DatabaseReference eventoRef = FirebaseDatabase.getInstance()
                            .getReference("eventosPublicos")
                            .child(eventoId);

                    eventoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String nome = snapshot.child("nome").getValue(String.class);
                                String dataInicio = snapshot.child("dataInicio").getValue(String.class);
                                String dataTermino = snapshot.child("dataTermino").getValue(String.class);
                                String endereco = snapshot.child("endereco").getValue(String.class);
                                String descricao = snapshot.child("descricao").getValue(String.class);
                                String qrCodeBase64 = snapshot.child("qrCodeBase64").getValue(String.class);

                                Evento evento = new Evento();
                                evento.setId(eventoId);
                                evento.setNome(nome);
                                evento.setDataInicio(dataInicio);
                                evento.setDataTermino(dataTermino);
                                evento.setEndereco(endereco);
                                evento.setDescricao(descricao);
                                evento.setQrCodeBase64(qrCodeBase64);

                                userRef.setValue(evento).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        userRef.child("horaEntrada").setValue(currentTime);
                                        runOnUiThread(() -> {
                                            Toast.makeText(ScanQRActivity.this, "Entrada registrada com sucesso!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(ScanQRActivity.this, eventosInscritos.class);
                                            startActivity(intent);
                                            finish();
                                        });
                                    } else {
                                        runOnUiThread(() -> {
                                            Toast.makeText(ScanQRActivity.this, "Erro ao salvar inscrição", Toast.LENGTH_SHORT).show();
                                            isProcessing = false;
                                        });
                                    }
                                });
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(ScanQRActivity.this, "Evento não encontrado", Toast.LENGTH_SHORT).show();
                                    isProcessing = false;
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            runOnUiThread(() -> {
                                Toast.makeText(ScanQRActivity.this, "Erro ao acessar dados do evento", Toast.LENGTH_SHORT).show();
                                isProcessing = false;
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() -> {
                    Toast.makeText(ScanQRActivity.this, "Erro ao verificar inscrição", Toast.LENGTH_SHORT).show();
                    isProcessing = false;
                });
            }
        });
    }
}
