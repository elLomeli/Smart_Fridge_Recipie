package com.example.recipies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import POJO.Product;


public class MostrarFV extends AppCompatActivity {

    private ImageView imagenView;
    private TextView tituloTextView;
    private FirebaseFirestore db;
    private String nombre, imagen;
    private EditText cantidad;
    private Button guardar;
    private String email;
    private Spinner ubicacion;
    private ArrayAdapter<CharSequence> Ubi;
    private String textoSeleccionado;
    private float temperatura;
    private float humedad;
    private List<Product> productList;
    private int caducidadStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_fv);

        imagenView = findViewById(R.id.mostrarimagen);
        tituloTextView = findViewById(R.id.mostrartitulo);
        cantidad = findViewById(R.id.editTextNumber);
        guardar = findViewById(R.id.guardar);
        ubicacion = findViewById(R.id.ubicacionSpinner);

        db = FirebaseFirestore.getInstance();

        // Inicialmente deshabilitar el botón de guardar
        guardar.setEnabled(false);

        Intent intent = getIntent();
        nombre = intent.getStringExtra("nombre");
        tituloTextView.setText(nombre);
        imagen = intent.getStringExtra("imagenUrl");
        Picasso.get().load(imagen).into(imagenView);

        SharedPreferences sharedPrefs = getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);

        if (email == null || email.isEmpty()) {
            Toast.makeText(MostrarFV.this, "No se encontró el email en SharedPreferences", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProductData();

        Ubi = ArrayAdapter.createFromResource(this, R.array.Ubicacion2, android.R.layout.simple_spinner_item);
        Ubi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ubicacion.setAdapter(Ubi);
        ubicacion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                textoSeleccionado = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        guardar.setOnClickListener(v -> guardarDatosFirestore());

        registerReceiver(bluetoothReceiver, new IntentFilter("BluetoothData"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("BluetoothData".equals(intent.getAction())) {
                try {
                    temperatura = Float.parseFloat(intent.getStringExtra("temperatura"));
                    humedad = Float.parseFloat(intent.getStringExtra("humedad"));
                    calculateCaducidad();
                } catch (NumberFormatException e) {
                    Toast.makeText(MostrarFV.this, "Datos de temperatura o humedad inválidos", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void loadProductData() {
        try {
            InputStream is = getAssets().open("products.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            Gson gson = new Gson();
            Type productListType = new TypeToken<ArrayList<Product>>() {}.getType();
            productList = gson.fromJson(json, productListType);

            if (productList == null) {
                productList = new ArrayList<>();
            }

        } catch (IOException e) {
            e.printStackTrace();
            productList = new ArrayList<>();
        }
    }

    private String normalizeName(String name) {
        return Normalizer.normalize(name.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replaceAll("[^a-z0-9]", "");
    }

    private Product findProductByName(String name) {
        if (productList == null || productList.isEmpty()) {
            return null;
        }

        String normalizedName = normalizeName(name);

        for (Product product : productList) {
            String normalizedProductName = normalizeName(product.getNombre());
            if (normalizedProductName.equals(normalizedName)) {
                return product;
            }
        }
        return null;
    }

    private void calculateCaducidad() {
        String nombreNormalizado = normalizeName(nombre);
        Product product = findProductByName(nombreNormalizado);
        if (product == null) {
            return;
        }

        double tempReferencia = temperatura > product.getTemperaturaMaxima() ? product.getTemperaturaMaxima() : product.getTemperaturaMinima();

        double caducidad = (temperatura / tempReferencia) * (humedad / product.getHumedadMaxima()) * product.getTiempoMaximo();
        caducidad = Math.min(caducidad, product.getTiempoMaximo());
        caducidadStr = (int) Math.round(caducidad);

        TextView caducidadTextView = findViewById(R.id.caducidadTextView);
        if (caducidadTextView != null && !textoSeleccionado.equals("Congelador")) {
            caducidadTextView.setText(String.format("Caducidad calculada: %.0f días", caducidad));
        } else {
            caducidadTextView.setText("Caducidad calculada: 365 días");
        }

        // Habilitar el botón "Guardar" ahora que se ha calculado la caducidad
        guardar.setEnabled(true);
    }

    private void guardarDatosFirestore() {
        String nombreProducto = tituloTextView.getText().toString().trim();
        String cantidadProducto = cantidad.getText().toString().trim();

        if (TextUtils.isEmpty(nombreProducto) || TextUtils.isEmpty(cantidadProducto)) {
            Toast.makeText(MostrarFV.this, "Debe completar todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int cantidadInt = Integer.parseInt(cantidadProducto);
            if (cantidadInt <= 0) {
                Toast.makeText(MostrarFV.this, "La cantidad debe ser un número positivo", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(MostrarFV.this, "La cantidad debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String claveDocumento = nombreProducto + "_" + textoSeleccionado;

        // Crear nuevo producto si no existe
        Map<String, Object> producto = new HashMap<>();
        producto.put("Nombre", nombreProducto);
        producto.put("Cantidad", cantidadProducto);
        producto.put("ImageURL", imagen);
        producto.put("Ubicacion", textoSeleccionado);
        producto.put("Caducidad", "Congelador".equals(textoSeleccionado) ? 365 : caducidadStr);

        db.collection(email + " Productos").document(claveDocumento).set(producto)
                .addOnSuccessListener(unused -> {
                    // Guardar la cantidad en SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("productos", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String claveUnica = nombreProducto + "_" + textoSeleccionado;
                    editor.putInt(claveUnica, Integer.parseInt(cantidadProducto));
                    editor.apply();

                    Toast.makeText(MostrarFV.this, "Ingresado Correctamente en " + textoSeleccionado, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(MostrarFV.this, "No Ingresado", Toast.LENGTH_SHORT).show());
    }
}
