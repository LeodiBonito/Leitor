package com.example.leitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class ParticipanteAdapter extends ArrayAdapter<usuario> {

    public ParticipanteAdapter(Context context, List<usuario> participantes) {
        super(context, android.R.layout.simple_list_item_2, android.R.id.text1, participantes);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView text1 = view.findViewById(android.R.id.text1);
        TextView text2 = view.findViewById(android.R.id.text2);

        usuario participante = getItem(position);

        if (participante != null) {
            text1.setText(participante.getNome());

            String email = participante.getEmail() != null ? participante.getEmail() : "Email não disponível";
            String entrada = participante.getHoraEntrada() != null ? participante.getHoraEntrada() : "Sem entrada";
            String saida = participante.getHoraSaida() != null ? participante.getHoraSaida() : "Sem saída";

            text2.setText(email + "\nEntrada: " + entrada + " | Saída: " + saida);
        }

        return view;
    }
}
