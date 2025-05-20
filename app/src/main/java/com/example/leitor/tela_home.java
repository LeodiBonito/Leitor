package com.example.leitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class tela_home extends AppCompatActivity {
    private Button btnCriarEvento,btnUsuario,btnEscanearEvento,btnMeusEventos,btnEventosQueEntrei;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_home);

        // Aplicar padding para barras do sistema

        btnCriarEvento = findViewById(R.id.btnCriarEvento);
        btnCriarEvento.setOnClickListener(v -> {
            Intent intent = new Intent(tela_home.this, gerarQrCode.class);
            startActivity(intent);
        });
        btnUsuario = findViewById(R.id.btnUsuario);
        btnUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(tela_home.this, usuario.class);
            finish();
            startActivity(intent);
        });
        btnEscanearEvento = findViewById(R.id.btnEscanearEvento);
        btnEscanearEvento.setOnClickListener(v -> {
            Intent intent = new Intent(tela_home.this, ScanQRActivity.class);
            startActivity(intent);
        });
        btnEventosQueEntrei = findViewById(R.id.btnEventosQueEntrei);
        btnEventosQueEntrei.setOnClickListener(v -> {
            Intent intent = new Intent(tela_home.this, eventosInscritos.class);
            startActivity(intent);
        });

        btnMeusEventos = findViewById(R.id.btnMeusEventos);
        btnMeusEventos.setOnClickListener(v -> {
            Intent intent = new Intent(tela_home.this, meusEventos.class);
            startActivity(intent);
        });

        // Na sua MainActivity ou Application:
        FirebaseDatabase.getInstance().getReference(".info/connected")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean connected = snapshot.getValue(Boolean.class);
                        Log.d("FIREBASE", "Conectado: " + connected);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FIREBASE", "Erro: " + error.getMessage());
                    }
                });

    }
}
