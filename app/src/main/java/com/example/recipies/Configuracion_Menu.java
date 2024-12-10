package com.example.recipies;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import adaptadores.adaptadorViewPagerCruds;

public class Configuracion_Menu extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button Cerrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion_menu);
        mAuth = FirebaseAuth.getInstance();
        Cerrar = findViewById(R.id.cerrar);

        // Evitar que la pantalla se apague
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Crear un cuadro de diálogo de confirmación
                AlertDialog.Builder builder = new AlertDialog.Builder(Configuracion_Menu.this);
                builder.setMessage("¿Está seguro de que desea cerrar sesión?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // El usuario ha confirmado, cerrar la sesión
                                mAuth.signOut();
                                startActivity(new Intent(Configuracion_Menu.this, Login.class));
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // El usuario ha cancelado, no hacer nada
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}
