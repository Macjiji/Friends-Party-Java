package com.macjiji.marcus.friendspartyjava;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 *
 * @author Marcus
 * @version 1.0
 *
 * Activité permettant d'avoir un aperçu d'un événement à venir
 *
 */

public class EventPreviewActivity extends AppCompatActivity implements OnMapReadyCallback {

    protected GoogleMap mMap;
    protected TextView eventName, userName, eventDate;
    protected Button comeToEvent, notComeToEvent;

    protected ListView participantListView;
    protected ArrayAdapter<String> participantArrayAdapter;
    private ArrayList<String> participantArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_preview);

        initializeMap();
        initializeTextViews();
        initializeListView();
        initializeButton();
    }

    /**
     * Méthode issue de l'interface OnMapReadyCallback
     * @see OnMapReadyCallback
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(getIntent().getDoubleExtra("latitude", -34), getIntent().getDoubleExtra("longitude", 151));
        mMap.addMarker(new MarkerOptions().position(sydney).title(getIntent().getStringExtra("eventName")));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    /**
     * Méthode d'initialisation de la Map
     */
    private void initializeMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Méthode d'initialisation de tous les champs textes
     */
    private void initializeTextViews(){
        // Etape 1 : On récupère la référence des vues via la classe R
        eventName = (TextView)findViewById(R.id.event_name);
        userName = (TextView)findViewById(R.id.event_username);
        eventDate = (TextView)findViewById(R.id.event_date);

        // Etape 2 : On ajoute les données récupérer grâce à l'Intent
        eventName.setText(getIntent().getStringExtra("eventName"));
        userName.setText(getIntent().getStringExtra("username"));

        // Etape 3 : Cas particulier. Comme on a enregistré les données de la date sous forme de Timestamp,
        //              on va dans un premier temps créer une instance de Calendar, qui se verra attribuer
        //              la valeur présent dans l'Intent.
        //           On formate enfin la date avec DateFormat
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTimeInMillis(getIntent().getLongExtra("dateTimestamp", 0));
        eventDate.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(new Date(getIntent().getLongExtra("date", 0) * 1000L)));

    }

    /**
     * Méthode d'initialisation de la liste de vues
     */
    private void initializeListView(){
        // Etape 1 : On récupère la référence du composant via la classe R
        participantListView = (ListView)findViewById(R.id.participant_list);
        // Etape 2 : On crée l'adaptateur : Ici, on créera un adaptateur simple à partir d'une liste de chaine de caractère
        participantArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, participantArrayList);
        // Etape 3 : On attache l'adaptateur à la vue
        participantListView.setAdapter(participantArrayAdapter);
        // Etape 4 : On finit par préparer la liste des données à insérer dans la liste de vues
        prepareParticipantDatas();
    }

    /**
     * Méthode d'initialisation du bouton de participation
     */
    private void initializeButton(){
        // Récupération des références
        comeToEvent = (Button)findViewById(R.id.come_to_event);
        notComeToEvent = (Button)findViewById(R.id.not_come_to_event);

        // Gestion du clic sur le bouton de participation à un événement
        comeToEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child("events").child(String.valueOf(getIntent().getLongExtra("date", 0))).child("participant").child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()).setValue(1);
            }
        });

        // Gestion du clic sur le bouton de non participation à un événement
        notComeToEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child("events").child(String.valueOf(getIntent().getLongExtra("date", 0))).child("participant").child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()).setValue(0);
            }
        });

        // Ici, on précise que si l'événement est passé, on n'affiche plus les boutons de participation !
        if(getIntent().getLongExtra("date", 0) * 1000L < System.currentTimeMillis()){
            comeToEvent.setVisibility(View.GONE);
            notComeToEvent.setVisibility(View.GONE);
        }

    }

    /**
     * Méthode permettant de créer la liste des particpant dans la liste adéquate
     */
    private void prepareParticipantDatas(){
        // On récupère les données à partir de la base de données Firebase. Tout se fera en temps réel grâce au
        //      Listener addChildListerner. L'avantage de cette implémentation est de pouvoir savoir quand un
        //      participant est ajouté (via onChildAdded), et quand un participant change d'avis (via onChildChanged).
        FirebaseDatabase.getInstance().getReference().child("events").child(String.valueOf(getIntent().getLongExtra("date", 0))).child("participant").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){ // On vérifie bien dans un premier temps que les données existent
                    if(dataSnapshot.getValue().toString().equals(String.valueOf(1))){ // On teste si la valeur de participation est égale à 1
                        participantArrayList.add(dataSnapshot.getKey()); // On ajoute les données à la liste des participant ...
                        participantArrayAdapter.notifyDataSetChanged(); // ... et on notifie à l'adaptateur que la liste des participants a changé !!
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getValue().toString().equals(String.valueOf(0))){ // On teste si la valeur de participation est égale à 0
                    participantArrayList.remove(dataSnapshot.getKey()); // On enlève en temps réel les données d'un participant qui ne souhaiterait plus participer ...
                    participantArrayAdapter.notifyDataSetChanged(); // ... et on notifie à l'adaptateur que la liste des participants a changé !!
                } else if(dataSnapshot.getValue().toString().equals(String.valueOf(1))){ // Sinon, on teste si la valeur de participation est égale à 1
                    participantArrayList.add(dataSnapshot.getKey()); // On ajoute les données à la liste des participant ...
                    participantArrayAdapter.notifyDataSetChanged(); // ... et on notifie à l'adaptateur que la liste des participants a changé !!
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }


}



