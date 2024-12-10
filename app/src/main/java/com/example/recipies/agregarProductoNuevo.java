package com.example.recipies;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class agregarProductoNuevo extends AppCompatActivity {
    private ImageView imagenView;
    private TextView tituloTextView;
    private FirebaseFirestore db;
    private EditText caducidad, cantidadU , cantidadK;
    private Button guardar;
    private String email;
    private Spinner ubicacion;
    private Spinner tipo;
    private ArrayAdapter<CharSequence> Ubi;
    private ArrayAdapter<CharSequence> TIPO;
    private String textoSeleccionado,textoSeleccionado2;
    private ImageButton camara;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private Bitmap imageBitmap; // Variable para almacenar la imagen capturada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_producto_nuevo);

        imagenView = findViewById(R.id.mostrarimagen);
        tituloTextView = findViewById(R.id.mostrartitulo);
        caducidad = findViewById(R.id.caducidad);
        cantidadU = findViewById(R.id.editTextNumberUnidades);
        cantidadK = findViewById(R.id.editTextNumberKilogramos);
        guardar = findViewById(R.id.guardar);
        camara =  findViewById(R.id.addImage);
        ubicacion = findViewById(R.id.ubicacionSpinner);
        tipo = findViewById(R.id.TipoSpinner);

        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPrefs = getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);

        //spinners
        Ubi = ArrayAdapter.createFromResource(agregarProductoNuevo.this,R.array.Ubicacion, android.R.layout.simple_spinner_item);
        Ubi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ubicacion.setAdapter(Ubi);
        ubicacion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Obtener el texto seleccionado
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

        //spinners
        TIPO = ArrayAdapter.createFromResource(agregarProductoNuevo.this,R.array.Tipo, android.R.layout.simple_spinner_item);
        TIPO.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipo.setAdapter(TIPO);
        tipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Obtener el texto seleccionado
                textoSeleccionado2 = parentView.getItemAtPosition(position).toString();
                if (textoSeleccionado2.equals("Fruta o Verdura")) {
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

        camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if the request code is the same as the one we used for the camera.
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            // Check if the result is RESULT_OK, which means the user took a picture.
            if (resultCode == RESULT_OK) {
                // Get the image from the camera.
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");

                // Set the image to the ImageView.
                imagenView.setImageBitmap(imageBitmap);
            }
        }
    }

    public void Camera() {
        if (ContextCompat.checkSelfPermission(agregarProductoNuevo.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // El permiso no ha sido concedido
            // Se solicita al usuario
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // El permiso ya ha sido concedido
            openCamera();
        }
    }
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(agregarProductoNuevo.this.getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void mostrarDatePicker() {
        // Obtener la fecha actual
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        // Crear un DatePickerDialog y establecer el listener para obtener la fecha seleccionada
        DatePickerDialog datePickerDialog = new DatePickerDialog(agregarProductoNuevo.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // La fecha seleccionada se devuelve en year, month (0-11), dayOfMonth
                        // Formatear la fecha y establecerla en el EditText
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);

                        // Verificar si la fecha seleccionada es menor o igual que la fecha actual
                        if (selectedDate.before(cal)) {
                            Toast.makeText(agregarProductoNuevo.this, "La fecha debe ser mayor que la fecha actual", Toast.LENGTH_SHORT).show();
                        } else {
                            String fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
                            caducidad.setText(fechaSeleccionada);
                        }
                    }
                }, year, month, day);

        // Mostrar el DatePickerDialog
        datePickerDialog.show();
    }

    private void guardarDatosFirestore() {
        // Obtener los valores de los campos
        String nombreProducto = tituloTextView.getText().toString().trim();
        String fechaCaducidad = caducidad.getText().toString().trim();
        String cantidadProducto = cantidadU.getText().toString().trim();

        // Verificar si los campos están vacíos
        if (nombreProducto.isEmpty() || cantidadProducto.isEmpty() || imageBitmap == null) {
            Toast.makeText(agregarProductoNuevo.this, "Debe completar todos los campos y tomar una foto", Toast.LENGTH_SHORT).show();
            return; // No guardar si hay campos vacíos o no se ha tomado una foto
        }

        // Crear un nuevo documento en Firestore con los datos obtenidos
        Map<String, Object> producto = new HashMap<>();
        producto.put("Nombre", nombreProducto);
        producto.put("Cantidad", cantidadProducto);
        producto.put("Ubicacion", textoSeleccionado);

        // Si no es un producto congelado, agregar la fecha de caducidad
        if (!textoSeleccionado.equals("Congelador")) {
            producto.put("Caducidad", fechaCaducidad);
        }

        // Subir la imagen a Firebase Storage
        uploadImageToFirebaseStorage(nombreProducto, producto);
    }

    private void uploadImageToFirebaseStorage(final String nombreProducto, final Map<String, Object> producto) {
        // Generar un nombre único para la imagen
        final String imageName = UUID.randomUUID().toString() + ".jpg";

        // Obtener la referencia al storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + imageName);

        // Convertir la imagen bitmap a un byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Subir la imagen al storage
        UploadTask uploadTask = storageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Si la subida es exitosa, obtener la URL de la imagen
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Guardar la URL de la imagen en Firestore
                        producto.put("ImageURL", uri.toString());

                        // Añadir el documento a la colección "productos"
                        db.collection(email + " Productos").document(nombreProducto).set(producto).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(agregarProductoNuevo.this, "Ingresado Correctamente", Toast.LENGTH_SHORT).show();
                                // Cerrar la actividad después de guardar los datos
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(agregarProductoNuevo.this, "No Ingresado", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(agregarProductoNuevo.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
