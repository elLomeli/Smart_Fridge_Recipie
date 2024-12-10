package com.example.recipies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private EditText txtMail;
    private TextInputLayout txtPassword;
    private Button btnLogin;
    private TextView lblRegister;
    private TextView lblForgot;
    private FirebaseAuth mAuth;
    private String correo;
    private FirebaseUser currentUser;
    private int loginAttempts = 0; // Contador de intentos de inicio de sesión
    private long lastFailedLoginTime = 0; // Registro de tiempo del último intento fallido
    private static final int MAX_LOGIN_ATTEMPTS = 5; // Máximo de intentos fallidos permitidos
    //private static final long LOCKOUT_TIME = 60000; // Tiempo de bloqueo en milisegundos (1 minutos)
    private static final long LOCKOUT_TIME = 900000; // Tiempo de bloqueo en milisegundos (15 minutos)

    private int colorActivado;
    private int colorDesactivado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Evitar que la pantalla se apague
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        txtMail = findViewById(R.id.txtMail);
        txtPassword = findViewById(R.id.txtPassword);
        lblRegister = findViewById(R.id.lblRegister);
        lblForgot = findViewById(R.id.lblForgot);
        btnLogin = findViewById(R.id.btnLogin);

        // Obtener los colores desde el archivo colors.xml
        colorActivado = getResources().getColor(R.color.activo);
        colorDesactivado = getResources().getColor(R.color.inactivo);

        // Establecer el botón de inicio de sesión como deshabilitado y con fondo gris inicialmente
        btnLogin.setEnabled(false);
        btnLogin.setBackgroundTintList(ColorStateList.valueOf(colorDesactivado));

        // Agregar oyentes de texto a los campos de correo y contraseña
        txtMail.addTextChangedListener(textWatcher);
        txtPassword.getEditText().addTextChangedListener(textWatcher);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        btnLogin.setOnClickListener(view -> {
            userLogin();
        });

        lblRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegisterActivity();
            }
        });

        lblForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openForgotActivity(txtMail.getText().toString());
            }
        });
    }

    // Oyente de texto para verificar los cambios en los campos de correo y contraseña
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            // Verificar si ambos campos contienen texto
            if (!TextUtils.isEmpty(txtMail.getText().toString()) && !TextUtils.isEmpty(txtPassword.getEditText().getText().toString())) {
                // Habilitar el botón de inicio de sesión y cambiar su color de fondo
                btnLogin.setEnabled(true);
                btnLogin.setBackgroundTintList(ColorStateList.valueOf(colorActivado));
            } else {
                // Si uno de los campos está vacío, deshabilitar el botón y cambiar su color de fondo
                btnLogin.setEnabled(false);
                btnLogin.setBackgroundTintList(ColorStateList.valueOf(colorDesactivado));
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null) {
            if (currentUser.getEmail().equals("admin@recipies.com")) {
                startActivity(new Intent(Login.this, ViewPager_Cruds.class));
            } else {
                startActivity(new Intent(Login.this, Menu.class));
            }
            finish();
        }
    }

    private void openForgotActivity(String email) {
        Intent intent = new Intent(this, Forgot.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    public void openRegisterActivity() {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
        finish();
    }// End openRegisterActivity

    public void userLogin() {
        String mail = txtMail.getText().toString();
        String password = txtPassword.getEditText().getText().toString();

        if (TextUtils.isEmpty(mail)) {
            txtMail.setError("Ingrese un correo");
            txtMail.requestFocus();
            return;
        } else if (!isValidEmail(mail)) {
            txtMail.setError("Ingrese un Correo válido");
            txtMail.requestFocus();
            return;
        } else if (TextUtils.isEmpty(password)) {
            txtPassword.setError("Ingrese una contraseña");
            txtPassword.requestFocus();
            return;
        }


        mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loginAttempts = 0; // Restablece el contador en caso de inicio de sesión exitoso
                    Toast.makeText(Login.this, "Bienvenid@", Toast.LENGTH_SHORT).show();

                    if (mail.equals("admin@recipies.com") && password.equals("admin1234")) {
                        Intent intent = new Intent(Login.this, ViewPager_Cruds.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(Login.this, Menu.class);
                        correo = txtMail.getText().toString();
                        SharedPreferences sharedPrefs = getSharedPreferences("correo", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.putString("email", correo);
                        editor.apply();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    finish();
                } else {
                    Toast.makeText(Login.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();

                    // Incrementa el contador de intentos
                    loginAttempts++;

                    if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                        Toast.makeText(Login.this, "Máximo número de intentos. Espera 15 minutos para iniciar sesión", Toast.LENGTH_SHORT).show();
                        // Si se supera el límite de intentos, bloquea temporalmente el inicio de sesión
                        lastFailedLoginTime = System.currentTimeMillis(); // Registra el tiempo del intento fallido
                        btnLogin.setEnabled(false); // Deshabilita el botón de inicio de sesión
                        btnLogin.setBackgroundTintList(ColorStateList.valueOf(colorDesactivado));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Desbloquea el inicio de sesión después de un tiempo
                                long currentTime = System.currentTimeMillis();
                                if (currentTime - lastFailedLoginTime >= LOCKOUT_TIME) {
                                    // Ha pasado suficiente tiempo, se desbloquea
                                    btnLogin.setEnabled(true); // Habilita el botón de inicio de sesión
                                    btnLogin.setBackgroundTintList(ColorStateList.valueOf(colorActivado));
                                    loginAttempts = 0; // Restablece el contador
                                }
                            }
                        }, LOCKOUT_TIME);
                    }
                }
            }
        });
    }

    private boolean isValidEmail(String mail) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String[] allowedDomains = {"hotmail.com", "gmail.com", "outlook.com", "live.com", "icloud.com", "me.com", "mac.com","recipies.com","live.ceti.mx","ceti.mx"};

        if (!mail.matches(emailPattern)) {
            return false; // No cumple el patrón de un correo válido
        }
        String domain = mail.substring(mail.indexOf("@") + 1);
        for (String allowedDomain : allowedDomains) {
            if (domain.equalsIgnoreCase(allowedDomain)) {
                return true; // Dominio permitido
            }
        }
        return false; // Dominio no permitido
    }

}
