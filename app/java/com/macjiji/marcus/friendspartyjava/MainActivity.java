package com.macjiji.marcus.friendspartyjava;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.macjiji.marcus.friendspartyjava.adapter.CustomListViewAdapter;
import com.macjiji.marcus.friendspartyjava.objects.Event;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Marcus
 * @version 1.0
 *
 * Activité d'accueil recensant tous les événements passés et à venir
 *
 */

public class MainActivity extends AppCompatActivity {

    protected Button addEvent;

    protected ListView upcomingEvents, pastEvents;
    protected CustomListViewAdapter upcomingEventsAdapter, pastEventsAdapter;
    protected ArrayList<Event> upcomingEventList = new ArrayList<>();
    protected ArrayList<Event> pastEventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeButtons();
        initializeListView();

    }

    /**
     * Méthode de création du menu au sein de l'activité
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Méthode pour déconnecter l'utilisateur à FirebasAuth et le rediriger vers l'écran de Login.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_disconnect:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Méthode d'initialisation des boutons
     */
    private void initializeButtons(){
        addEvent = (Button)findViewById(R.id.add_event);
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddPartyActivity.class));
            }
        });
    }

    /**
     * Méthode d'initialisation de la ListView
     */
    private void initializeListView(){
        upcomingEvents = (ListView) findViewById(R.id.upcoming_events);
        pastEvents = (ListView) findViewById(R.id.past_events);

        // On crée l'adapter par rapport aux données présentes dans la liste
        upcomingEventsAdapter = new CustomListViewAdapter(this, upcomingEventList);
        // On attache l'adapter
        upcomingEvents.setAdapter(upcomingEventsAdapter);
        // On crée enfin la méthode qui va détecter le clic sur un item en particulier
        upcomingEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent eventPreview = new Intent(MainActivity.this, EventPreviewActivity.class);
                eventPreview.putExtra("eventName", upcomingEventList.get(i).getName());
                eventPreview.putExtra("date", upcomingEventList.get(i).getDateTimestamp());
                eventPreview.putExtra("username", upcomingEventList.get(i).getUser());
                eventPreview.putExtra("latitude", upcomingEventList.get(i).getLatitude());
                eventPreview.putExtra("longitude", upcomingEventList.get(i).getLongitude());
                startActivity(eventPreview);
            }
        });

        // On crée l'adapter par rapport aux données présentes dans la liste
        pastEventsAdapter = new CustomListViewAdapter(this, pastEventList);
        // On attache l'adapter
        pastEvents.setAdapter(pastEventsAdapter);
        // On crée enfin la méthode qui va détecter le clic sur un item en particulier
        pastEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent eventPreview = new Intent(MainActivity.this, EventPreviewActivity.class);
                eventPreview.putExtra("eventName", pastEventList.get(i).getName());
                eventPreview.putExtra("date", pastEventList.get(i).getDateTimestamp());
                eventPreview.putExtra("username", pastEventList.get(i).getUser());
                eventPreview.putExtra("latitude", pastEventList.get(i).getLatitude());
                eventPreview.putExtra("longitude", pastEventList.get(i).getLongitude());
                startActivity(eventPreview);
            }
        });

        prepareEventsDatas();

    }

    /**
     * Méthode qui va récupérer les données des événements enregistrés sur la base de données Firebase
     */
    private void prepareEventsDatas(){

        FirebaseDatabase.getInstance().getReference().child("events").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Etape 1 : On récupère les données sous forme d'objet Event
                Event event = dataSnapshot.getValue(Event.class);

                // Etape 2 : Ici, pour faire la distinction entre les événements passés et ceux à venir,
                //              on teste la valeur Timestamp de l'événement par rapport à la valeur
                //              Timestamp actuelle du système.
                if(event.getDateTimestamp() * 1000L < System.currentTimeMillis()){ // Si le Timestamp de l'événement est inférieur à la valeur actuelle du Timestamp système
                    pastEventList.add(event); // On ajoute l'événement dans la liste des événements passés...
                    pastEventsAdapter.notifyDataSetChanged(); // ... puis on notifie à l'adaptateur les changements
                } else { // Sinon, cela veut dire que l'événement n'est pas encore passé !
                    upcomingEventList.add(event); // On ajoute l'événement dans la liste des événements à venir...
                    upcomingEventsAdapter.notifyDataSetChanged(); // ... puis on notifie à l'adaptateur les changements
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

}
