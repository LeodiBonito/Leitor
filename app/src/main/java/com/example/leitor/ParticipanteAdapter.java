package com.example.leitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ParticipanteAdapter extends ArrayAdapter<usuario> {

    public ParticipanteAdapter(Context context, List<usuario> participantes) {
        super(context, 0, participantes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        usuario participante = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);

        text1.setText(participante.getNome() != null ? participante.getNome() : "Nome não disponível");
        text2.setText(participante.getEmail() != null ? participante.getEmail() : "Email não disponível");

        return convertView;
    }
}