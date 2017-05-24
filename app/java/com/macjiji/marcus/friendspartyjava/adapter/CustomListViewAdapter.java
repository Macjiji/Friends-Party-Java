package com.macjiji.marcus.friendspartyjava.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.macjiji.marcus.friendspartyjava.R;
import com.macjiji.marcus.friendspartyjava.objects.Event;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 *
 * @author Marcus
 * @version 1.0
 *
 * Classe Adaptateur personnalisé pour les RecyclerView
 *
 */

public class CustomListViewAdapter extends ArrayAdapter<Event> {

    public CustomListViewAdapter(Context context, ArrayList<Event> eventArrayList) {
        super(context, 0, eventArrayList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Etape 1 : On récupère une liste de courses particulière
        Event event = getItem(position);

        // Etape 2 : On utilise le LayoutInflater pour inclure le layout list_item
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_list_row, parent, false);
        }

        // Etape 3 : On récupère la référence du champ de texte shoppingListName
        TextView eventName = (TextView) convertView.findViewById(R.id.eventName);
        TextView dateName = (TextView) convertView.findViewById(R.id.eventDate) ;

        // Etape 4 : On inclus le nom de la liste et la couleur de la liste sur la vue texte
        eventName.setText(event.getName());
        dateName.setText(getDate(event.getDateTimestamp()));

        // Etape 5 : On retournne la vue créée
        return convertView;
    }

    private String getDate(long date){
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(date * 1000L);
    }



}
