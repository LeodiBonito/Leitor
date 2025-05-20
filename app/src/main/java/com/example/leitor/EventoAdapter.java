package com.example.leitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class EventoAdapter extends ArrayAdapter<Evento> {

    public EventoAdapter(Context context, List<Evento> eventos) {
        super(context, 0, eventos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Evento evento = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(evento.getNome());

        return convertView;
    }
}