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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ScanQRActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private TextView tvResult;
    private BarcodeDetector barcodeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qractivity);

        surfaceView = findViewById(R.id.surfaceView);
        tvResult = findViewById(R.id.tvResult);
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
                if (qrCodes.size() > 0) {
                    final Barcode barcode = qrCodes.valueAt(0);
                    final String rawValue = barcode.displayValue;

                    runOnUiThread(() -> {
                        try {
                            // Processa como JSON
                            JSONObject json = new JSONObject(rawValue);
                            String nomeEvento = json.getString("evento");
                            String inicio = json.getString("inicio");
                            String termino = json.getString("termino");
                            String endereco = json.getString("endereco");
                            String descricao = json.getString("descricao");

                            tvResult.setText("Evento: " + nomeEvento);

                            // Exemplo: abrir nova tela com os dados
                            Intent intent = new Intent(ScanQRActivity.this, gerarQrCode .class);
                            intent.putExtra("nomeEvento", nomeEvento);
                            intent.putExtra("inicio", inicio);
                            intent.putExtra("termino", termino);
                            intent.putExtra("endereco", endereco);
                            intent.putExtra("descricao", descricao);
                            startActivity(intent);

                        } catch (JSONException e) {
                            // Se não for JSON, trata como texto simples
                            tvResult.setText(rawValue);

                            // Mensagem de inscrição no evento
                            Toast.makeText(ScanQRActivity.this,
                                    "✅ Inscrição confirmada no evento!\n" + rawValue,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeQRScanner();
            } else {
                Toast.makeText(this, "Permissão da câmera é necessária", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
        if (barcodeDetector != null) {
            barcodeDetector.release();
        }
    }
}