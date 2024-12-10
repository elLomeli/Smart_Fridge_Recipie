package com.example.recipies;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class Forgot extends AppCompatActivity {
    private EditText editTextEmail;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.editTextEmail);
        Button buttonSendCode = findViewById(R.id.buttonSendCode);

        String email = getIntent().getStringExtra("email");
        if (email != null) {
            editTextEmail.setText(email);
        }

        buttonSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = editTextEmail.getText().toString().trim();
                if (TextUtils.isEmpty(userEmail)) {
                    editTextEmail.setError("Ingrese un Correo");
                } else if (isValidEmail(userEmail)) {
                    sendRecoveryCode(userEmail);
                    editTextEmail.setError("Ingrese un Correo Valido");
                } else {
                    Toast.makeText(Forgot.this, "Por favor, proporciona un correo electrónico válido", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void sendRecoveryCode(String email) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(Forgot.this, "Correo Enviado a " + email, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Forgot.this,Login.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Toast.makeText(Forgot.this, "Correo no Enviado", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String[] allowedDomains = {"hotmail.com", "gmail.com", "outlook.com", "live.com", "icloud.com", "me.com", "mac.com"};

        if (!email.matches(emailPattern)) {
            return false; // No cumple el patrón de un correo válido
        }
        String domain = email.substring(email.indexOf("@") + 1);
        for (String allowedDomain : allowedDomains) {
            if (domain.equalsIgnoreCase(allowedDomain)) {
                return true; // Dominio permitido
            }
        }
        return false; // Dominio no permitido
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}