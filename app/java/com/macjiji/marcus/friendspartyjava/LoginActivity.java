package com.macjiji.marcus.friendspartyjava;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

/**
 *
 * @author Marcus
 * @version 1.0
 *
 * Activité de Login
 *
 */

public class LoginActivity extends AppCompatActivity {

    protected EditText userMail, userPassword;
    protected Button login;
    protected TextView createAccount;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeFirebase();
        initializeButtons();
        initializeEditText();
        initializeTextViews();
    }

    /**
     * Méthode d'initialisation de Firebase.
     * Ici, on redirige directement l'utilisateur vers l'accueil s'il est déjà connecté via FirebaseAuth.
     */
    private void initializeFirebase(){
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
    }

    /**
     * Méthode d'initialisation des boutons
     */
    private void initializeButtons(){
        login = (Button)findViewById(R.id.button_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signInWithEmailAndPassword(userMail.getText().toString(), userPassword.getText().toString())
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                }
                            }
                        });
            }
        });
    }

    /**
     * Méthode d'initialisation des EditText
     */
    private void initializeEditText(){
        userMail = (EditText)findViewById(R.id.email);
        userPassword = (EditText)findViewById(R.id.password);
    }

    /**
     * Méthode d'initialisation des TextView.
     * Ici, j'ajoute un clickListener pour rediriger vers l'activité Register.
     */
    private void initializeTextViews(){
        createAccount = (TextView)findViewById(R.id.create_account);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }


}
