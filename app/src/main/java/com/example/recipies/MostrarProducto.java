package com.example.recipies;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MostrarProducto extends AppCompatActivity {

    private ImageView imagenView;
    private TextView tituloTextView;
    private FirebaseFirestore db;
    private String nombre, imagen;
    private EditText caducidad, cantidad;
    private Button guardar;
    private String email;
    private Spinner ubicacion;
    private ArrayAdapter<CharSequence> Ubi;
    private String textoSeleccionado;
    private long diasRestantes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_producto);

        imagenView = findViewById(R.id.mostrarimagen);
        tituloTextView = findViewById(R.id.mostrartitulo);
        caducidad = findViewById(R.id.caducidad);
        cantidad = findViewById(R.id.editTextNumber);
        guardar = findViewById(R.id.guardar);
        ubicacion = findViewById(R.id.ubicacionSpinner);

        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPrefs = getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);

        Intent intent = getIntent();
        nombre = intent.getStringExtra("nombre");
        tituloTextView.setText(nombre);
        imagen = intent.getStringExtra("imagenUrl");
        Picasso.get().load(imagen).into(imagenView);

        // Configuración del Spinner
        Ubi = ArrayAdapter.createFromResource(MostrarProducto.this, R.array.Ubicacion, android.R.layout.simple_spinner_item);
        Ubi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ubicacion.setAdapter(Ubi);
        ubicacion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                textoSeleccionado = parentView.getItemAtPosition(position).toString();
                if (textoSeleccionado.equals("Congelador")) {
                    caducidad.setText(""); // Eliminar el texto de caducidad
                    caducidad.setVisibility(View.GONE); // Ocultar el EditText de caducidad
                } else {
                    caducidad.setVisibility(View.VISIBLE); // Mostrar el EditText de caducidad
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Este método se llama cuando no hay ningún elemento seleccionado.
            }
        });

        caducidad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDatePicker();
            }
        });

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarDatosFirestore();
            }
        });
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(MostrarProducto.this,
                (view, year1, month1, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, month1, dayOfMonth);

                    if (selectedDate.before(cal)) {
                        Toast.makeText(MostrarProducto.this, "La fecha debe ser mayor que la fecha actual", Toast.LENGTH_SHORT).show();
                    } else {
                        String fechaSeleccionada = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        caducidad.setText(fechaSeleccionada);

                        // Calcular los días restantes hasta la fecha seleccionada
                        diasRestantes = calcularDiasRestantes(selectedDate.getTimeInMillis());
                    }
                }, year, month, day);

        datePickerDialog.show();
    }

    // Método para calcular los días restantes
    private long calcularDiasRestantes(long fechaCaducidad) {
        long fechaActual = Calendar.getInstance().getTimeInMillis();
        long diferencia = (fechaCaducidad) - fechaActual;
        return TimeUnit.MILLISECONDS.toDays(diferencia);
    }

    private void guardarDatosFirestore() {
        String nombreProducto = tituloTextView.getText().toString().trim();
        String cantidadProducto = cantidad.getText().toString().trim();

        if (nombreProducto.isEmpty() || cantidadProducto.isEmpty()) {
            Toast.makeText(MostrarProducto.this, "Debe completar todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String idDocumento = nombreProducto + "_" + textoSeleccionado;

        // Crear el producto y guardarlo en Firestore
        Map<String, Object> producto = new HashMap<>();
        producto.put("Nombre", nombreProducto);
        producto.put("Cantidad", cantidadProducto);
        producto.put("ImageURL", imagen);
        producto.put("Ubicacion", textoSeleccionado);

        if (!textoSeleccionado.equals("Congelador")) {
            int diasRestantesStr = (int) (diasRestantes + 1);
            producto.put("Caducidad", diasRestantesStr);
        } else {
            producto.put("Caducidad", 365);
        }

        db.collection(email + " Productos").document(idDocumento).set(producto)
                .addOnSuccessListener(unused -> {
                    // Guardar la cantidad en SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("productos", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String claveUnica = nombreProducto + "_" + textoSeleccionado;
                    editor.putInt(claveUnica, Integer.parseInt(cantidadProducto));
                    editor.apply();

                    Toast.makeText(MostrarProducto.this, "Ingresado Correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(MostrarProducto.this, "No Ingresado", Toast.LENGTH_SHORT).show());
    }
}