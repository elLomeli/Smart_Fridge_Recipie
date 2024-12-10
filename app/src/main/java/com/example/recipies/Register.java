package com.example.recipies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import android.os.Handler;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import Correo.JavaMailAPI;

public class Register extends AppCompatActivity {
    private EditText txtUser;
    private EditText txtMail;
    private EditText txtPhone;
    private TextInputLayout txtPassword,txtConfirmPassword;
    private Button btnRegister;
    private TextView lblLogin;
    private Button btnSendSMS;
    private String userID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int colorActivado;
    private int colorDesactivado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Evitar que la pantalla se apague
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        txtUser = findViewById(R.id.txtUser);
        txtMail = findViewById(R.id.txtMail);
        txtPhone = findViewById(R.id.txtPhone);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword);
        lblLogin = findViewById(R.id.lblLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnSendSMS = findViewById(R.id.buttonSendSMS);

        // Obtener los colores desde el archivo colors.xml
        colorActivado = getResources().getColor(R.color.activo);
        colorDesactivado = getResources().getColor(R.color.inactivo);

        // Establecer el botón de registro como deshabilitado y con fondo gris inicialmente
        btnRegister.setEnabled(false);
        btnRegister.setBackgroundTintList(ColorStateList.valueOf(colorDesactivado));

        // Agregar oyentes de texto a los campos relevantes
        txtUser.addTextChangedListener(textWatcher);
        txtMail.addTextChangedListener(textWatcher);
        txtPhone.addTextChangedListener(textWatcher);
        txtPassword.getEditText().addTextChangedListener(textWatcher);
        txtConfirmPassword.getEditText().addTextChangedListener(textWatcher);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();



        // Evitar que la pantalla se apague
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Limita que sean 10 dígitos y solo sean números
        txtPhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        txtPhone.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        txtPassword.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Activa la visibilidad de la contraseña al seleccionar el TextInputLayout
                    txtPassword.setPasswordVisibilityToggleEnabled(true);
                } else {
                    // Desactiva la visibilidad de la contraseña cuando el TextInputLayout pierde el enfoque
                    txtPassword.setPasswordVisibilityToggleEnabled(false);
                }
            }
        });

        btnRegister.setOnClickListener(view -> {
            createUser();
        });

        lblLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoginActivity();
            }
        });

        // Verificar la longitud del texto en txtPhone y habilitar/deshabilitar btnSendSMS
        txtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String phone = s.toString();
                if (phone.length() >= 10) {
                    btnSendSMS.setEnabled(true);
                } else {
                    btnSendSMS.setEnabled(false);
                }
            }
        });

        btnSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openVerificationDialog();

            }
        });

    }

    // Oyente de texto para verificar los cambios en los campos relevantes
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            // Verificar si todos los campos relevantes contienen texto
            if (!TextUtils.isEmpty(txtUser.getText().toString()) &&
                    !TextUtils.isEmpty(txtMail.getText().toString()) &&
                    !TextUtils.isEmpty(txtPhone.getText().toString()) &&
                    !TextUtils.isEmpty(txtPassword.getEditText().getText().toString()) &&
                    !TextUtils.isEmpty(txtConfirmPassword.getEditText().getText().toString())) {
                // Habilitar el botón de registro y cambiar su color de fondo
                btnRegister.setEnabled(true);
                btnRegister.setBackgroundTintList(ColorStateList.valueOf(colorActivado));
            } else {
                // Si uno de los campos está vacío, deshabilitar el botón y cambiar su color de fondo
                btnRegister.setEnabled(false);
                btnRegister.setBackgroundTintList(ColorStateList.valueOf(colorDesactivado));
            }
        }
    };


    private void openVerificationDialog() {
        String phoneNumber = txtPhone.getText().toString();
        String email = txtMail.getText().toString();
        Random random = new Random();
        int code = random.nextInt(9000) + 1000; // Generar un número aleatorio de 4 dígitos

        if (TextUtils.isEmpty(email)) {
            // Si el campo de correo electrónico está vacío, muestra un mensaje de error y no abre el AlertDialog
            Toast.makeText(this, "Por favor ingresa tu correo electrónico", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmación de número")
                .setMessage("¿Es correcto " + phoneNumber + " el número de teléfono?" + " Se te enviara por correo ")
                .setPositiveButton("Sí", null) // Establece null para el listener por ahora
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancelar la acción o realizar cualquier otra tarea necesaria
                    }
                });

        // Crear el AlertDialog
        AlertDialog alertDialog = builder.create();

        // Deshabilitar el botón "Sí" si el campo de correo está vacío
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setEnabled(!TextUtils.isEmpty(email));
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Aquí colocar el código para enviar el correo electrónico y realizar otras acciones
                        Bundle bundle = new Bundle();
                        bundle.putString("codigo", String.valueOf(code));
                        VerificationDialogFragment dialogFragment = new VerificationDialogFragment();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(getSupportFragmentManager(), "VerificationDialog");
                        txtPhone.setEnabled(false);
                        try {
                            String mSubject = "Confirmación de teléfono celular";
                            String mMessage = "Tu código de verificación para " + phoneNumber + " es " + code;
                            JavaMailAPI javaMailAPI = new JavaMailAPI(Register.this, email, mSubject, mMessage);
                            javaMailAPI.execute();
                        } catch (Exception e) {
                            Toast.makeText(Register.this, "No se envió el código", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        alertDialog.dismiss();
                    }
                });
            }
        });

        // Mostrar el AlertDialog
        alertDialog.show();
    }


    public void openLoginActivity() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }// End openLoginActivity

    public void createUser() {
        String name = txtUser.getText().toString();
        String mail = txtMail.getText().toString();
        String phone = txtPhone.getText().toString();
        String password = txtPassword.getEditText().getText().toString();
        String confirmPassword = txtConfirmPassword.getEditText().getText().toString();

        if (TextUtils.isEmpty(name)) {
            txtUser.setError("Ingrese un Nombre");
            txtUser.requestFocus();
        } else if (name.matches(".*\\d.*")) {
            txtUser.setError("El nombre no debe contener números");
            txtUser.requestFocus();
        } else if (!name.matches("[a-zA-ZñÑ\\s]+")) {
            txtUser.setError("El nombre no debe contener caracteres especiales");
            txtUser.requestFocus();
        }else if (TextUtils.isEmpty(mail)) {
            txtMail.setError("Ingrese un Correo");
            txtMail.requestFocus();
        } else if (!isValidEmail(mail)) {
            txtMail.setError("Ingrese un Correo válido");
            txtMail.requestFocus();
        } else if (TextUtils.isEmpty(phone)) {
            txtPhone.setError("Ingrese un Teléfono");
            txtPhone.requestFocus();
        } else if (phone.length() < 10) {
            txtPhone.setError("Faltan números en el teléfono");
            txtPhone.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            txtPassword.setError("Ingrese la contraseña");
            txtPassword.requestFocus();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {txtPassword.setError(null);}}, 2000);
        } else if (!isValidPassword(password)) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Contraseña inválida");
        builder.setMessage("La contraseña debe cumplir los siguientes requisitos:\n" +
                "- Al menos 8 caracteres\n" +
                "- Al menos una letra minúscula\n" +
                "- Al menos una letra mayúscula\n" +
                "- Al menos un dígito\n" +
                "- Al menos un carácter especial (@#$%^&+=)");
        builder.setPositiveButton("OK", null);
        builder.show();
    } else if (TextUtils.isEmpty(confirmPassword)) {
            txtConfirmPassword.setError("Ingrese de nuevo la contraseña");
            txtConfirmPassword.requestFocus();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {txtConfirmPassword.setError(null);}}, 2000);
        } else if (!password.equals(confirmPassword)) {
            txtConfirmPassword.setError("las contraseñas no coinciden");
            txtConfirmPassword.requestFocus();
        } else {
            // Verificar si el correo y el teléfono ya existen en la base de datos
            Task<QuerySnapshot> mailTask = FirebaseFirestore.getInstance().collection("users")
                    .whereEqualTo("Correo", mail)
                    .get();

            Task<QuerySnapshot> phoneTask = FirebaseFirestore.getInstance().collection("users")
                    .whereEqualTo("Teléfono", phone)
                    .get();

            Tasks.whenAllSuccess(mailTask, phoneTask).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
                @Override
                public void onComplete(@NonNull Task<List<Object>> task) {
                    if (task.isSuccessful()) {
                        List<Object> results = task.getResult();
                        QuerySnapshot mailQuerySnapshot = (QuerySnapshot) results.get(0);
                        QuerySnapshot phoneQuerySnapshot = (QuerySnapshot) results.get(1);

                        if (mailQuerySnapshot.isEmpty() && phoneQuerySnapshot.isEmpty()) {
                            guardarUsuario(mail, password, phone, name);
                        } else {
                            if (!mailQuerySnapshot.isEmpty()) {
                                // El correo ya existe en la base de datos, mostrar mensaje de error
                                txtMail.setError("El correo ya está registrado");
                                txtMail.requestFocus();
                            }
                            if (!phoneQuerySnapshot.isEmpty()) {
                                // El teléfono ya existe en la base de datos, mostrar mensaje de error
                                txtPhone.setError("El teléfono ya está registrado");
                                txtPhone.requestFocus();
                            }
                        }
                    } else {
                        // Error al consultar la base de datos
                        Toast.makeText(Register.this, "Error al consultar la base de datos", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void guardarUsuario(String mail, String password, String phone, String name) {
        mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    DocumentReference documentReference = db.collection("users").document(mail);
                    userID = mAuth.getCurrentUser().getUid();
                    Map<String, Object> user = new HashMap<>();
                    user.put("Nombre", name);
                    user.put("Correo", mail);
                    user.put("Teléfono", phone);

                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("TAG", "onSuccess: Datos registrados" + userID);
                        }
                    });
                    Toast.makeText(Register.this, "Usuario Registrado", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Register.this, Login.class));
                } else {
                    Toast.makeText(Register.this, "Usuario no registrado" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        return password.matches(passwordPattern);
    }
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String[] allowedDomains = {"hotmail.com", "gmail.com", "outlook.com", "live.com", "icloud.com", "me.com", "mac.com","live.ceti.mx", "ceti.mx"};

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
        super.onBackPressed();
        openLoginActivity();
    }


}