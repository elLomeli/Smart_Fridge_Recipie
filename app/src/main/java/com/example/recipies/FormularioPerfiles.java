package com.example.recipies;

import static android.view.View.VISIBLE;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class FormularioPerfiles extends AppCompatActivity {

    private String email;
    private FirebaseFirestore db;
    private EditText Nombre,Edad,Estatura,Peso,Apodo;
    private Button guardar;
    private String id,textoSeleccionado;
    private ImageView Imagen;
    private int imagenSeleccionada;
    private FirebaseStorage storage ;
    private StorageReference storageReference;
    private boolean modificar;
    private ArrayAdapter<CharSequence> Sexo;
    private Spinner sexo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_perfiles);

        // Evitar que la pantalla se apague
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Nombre = findViewById(R.id.txtPerfilNombre);
        Edad = findViewById(R.id.txtPerfilEdad);//este
        Estatura = findViewById(R.id.txtPerfilEstatura);//este
        Peso = findViewById(R.id.txtPerfilPeso);//este
        Apodo = findViewById(R.id.txtPerfilApodo);
        guardar = findViewById(R.id.guardarperfil);
        Imagen = findViewById(R.id.imagenPerfil);
        Intent intent = getIntent();
        id = intent.getStringExtra("perfil");
        modificar = intent.getBooleanExtra("modificar", false);
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPrefs = getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        sexo = findViewById(R.id.SpinerPerfilSexo);
        //spinner
        Sexo = ArrayAdapter.createFromResource(FormularioPerfiles.this,R.array.Sexo, android.R.layout.simple_spinner_item);
        Sexo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexo.setAdapter(Sexo);
        sexo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Obtener el texto seleccionado
                textoSeleccionado = parentView.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Este método se llama cuando no hay ningún elemento seleccionado.
            }
        });
        Toast.makeText(this, "Perfil" + id, Toast.LENGTH_SHORT).show();

        if (modificar) {
            cargarDatosDesdeFirestore();
        }

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                obtenerTextos();
            }
        });




        Imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Inicia la actividad para seleccionar la imagen
                Intent intent = new Intent(FormularioPerfiles.this, SeleccionarImagenActivity.class);
                startActivityForResult(intent, 1);
            }
        });

    }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1 && resultCode == RESULT_OK) {
                // Recibe la imagen seleccionada desde SeleccionarImagenActivity
                imagenSeleccionada = data.getIntExtra("imagenSeleccionada", 0);
                Imagen.setImageResource(imagenSeleccionada);
                Imagen.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        }


    private void obtenerTextos() {
        String NombrePerfil = Nombre.getText().toString().trim();
        String EdadPerfil = Edad.getText().toString().trim();
        String EstaturaPerfil = Estatura.getText().toString().trim();
        String PesoPerfil = Peso.getText().toString().trim();
        String ApodoPerfil = Apodo.getText().toString().trim();

        // Verificar si se ha seleccionado una imagen
        if (imagenSeleccionada == 0) {
            // Mostrar un mensaje de error si no se ha seleccionado ninguna imagen
            Toast.makeText(FormularioPerfiles.this, "Selecciona una imagen antes de guardar", Toast.LENGTH_SHORT).show();
            return; // Salir del método sin intentar guardar el perfil
        }

        if (camposCompletos(NombrePerfil, EdadPerfil, EstaturaPerfil, PesoPerfil, ApodoPerfil)) {
            guardarImagenEnStorage(NombrePerfil, EdadPerfil, EstaturaPerfil, PesoPerfil, ApodoPerfil);
        }
        else {
            // Muestra un mensaje de error si algún campo está vacío
            Toast.makeText(FormularioPerfiles.this, "Completa todos los campos antes de guardar", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean camposCompletos(String nombre, String edad, String estatura, String peso, String apodo) {
        return !TextUtils.isEmpty(nombre) && !TextUtils.isEmpty(edad) && !TextUtils.isEmpty(estatura) && !TextUtils.isEmpty(peso) && !TextUtils.isEmpty(apodo);
    }

    private void guardarImagenEnStorage(String nombrePerfil, String edadPerfil, String estaturaPerfil, String pesoPerfil, String apodoPerfil) {
        // Convierte la imagen a un array de bytes (si es necesario)
        BitmapDrawable bitmapDrawable = (BitmapDrawable) Imagen.getDrawable();
        if (bitmapDrawable != null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            // Genera un nombre de archivo único
            String nombreArchivo = "imagen_perfil_" + System.currentTimeMillis() + ".jpg";

            // Guarda la imagen en Firebase Storage
            StorageReference imagenRef = storageReference.child("logos/" + nombreArchivo);
            UploadTask uploadTask = imagenRef.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Obtiene la URL de la imagen almacenada
                    imagenRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String urlImagen = uri.toString();

                            // Llama a la función para guardar en Firestore con la URL de la imagen
                            saveProductData(nombrePerfil, edadPerfil, estaturaPerfil, pesoPerfil, apodoPerfil, urlImagen);
                        }
                    });
                }
            });
        }
    }

    private void saveProductData(String nombrePerfil, String edadPerfil, String estaturaPerfil, String pesoPerfil, String apodoPerfil, String urlImagen) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", nombrePerfil.substring(0, 1).toUpperCase() + nombrePerfil.substring(1).toLowerCase());
        map.put("apodo", apodoPerfil.substring(0, 1).toUpperCase() + apodoPerfil.substring(1).toLowerCase());
        map.put("peso", pesoPerfil);
        map.put("edad", edadPerfil);
        map.put("estatura", estaturaPerfil);
        map.put("url_imagen", urlImagen);
        map.put("sexo",textoSeleccionado);

        db.collection(email).document("perfil " + id).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(FormularioPerfiles.this, "Guardado Correctamente", Toast.LENGTH_SHORT).show();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("perfil", id);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FormularioPerfiles.this, "No guardado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDatosDesdeFirestore() {
        DocumentReference docRef = db.collection(email).document("perfil " + id);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Obtener datos del documento
                    String nombre = documentSnapshot.getString("nombre");
                    String edad = documentSnapshot.getString("edad");
                    String estatura = documentSnapshot.getString("estatura");
                    String alergias = documentSnapshot.getString("alergias");
                    String peso = documentSnapshot.getString("peso");
                    String apodo = documentSnapshot.getString("apodo");
                    String urlImagen = documentSnapshot.getString("url_imagen");

                    // Asignar datos a los EditText
                    Nombre.setText(nombre);
                    Edad.setText(edad);
                    Estatura.setText(estatura);
                    Peso.setText(peso);
                    Apodo.setText(apodo);

                    // Obtener el sexo almacenada en Firestore
                    String sexobd = documentSnapshot.getString("sexo");
                    if (sexobd != null) {
                        int medidaIndex = Sexo.getPosition(sexobd);
                        if (medidaIndex != -1) {
                            sexo.setSelection(medidaIndex);
                        }
                    }

                    // Cargar la imagen en ImageView (puedes usar tu lógica específica)
                    cargarImagenConGlide(urlImagen, Imagen);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error al obtener datos de Firestore", e);
            }
        });
    }

    // Añadir este método para cargar una imagen utilizando Glide (puedes usar tu lógica específica)
    private void cargarImagenConGlide(String urlImagen, ImageView imageView) {
        Glide.with(this)
                .load(urlImagen)
                .into(imageView);
    }

    @Override
    public void onBackPressed() {
        // Evitar que se realice alguna acción al presionar el botón de retroceso
        super.onBackPressed();
    }

}
