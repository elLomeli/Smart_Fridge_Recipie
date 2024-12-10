package com.example.recipies;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import LISTAS.IngredientData;
import LISTAS.StepData;
import adaptadores.adaptadorIngredientes;
import adaptadores.adaptadorPasos;

public class Recipies_Fragment extends DialogFragment {
    private boolean fotoAmpliada = false;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private Button addIngredientes;
    private Button addSteps;
    private Button guardar;
    private RecyclerView mRecycler;
    private RecyclerView mRecyclers;
    private ImageButton camara;
    private ImageView foto;
    private EditText titulo;
    private EditText calo;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String id;
    private List<IngredientData> ingredientesDataList = new ArrayList<>();
    private List<StepData> pasosDataList = new ArrayList<>();
    private adaptadorIngredientes mAdapter;
    private adaptadorPasos mAdapters;
    private ArrayAdapter<CharSequence> Horario;
    private ArrayAdapter<CharSequence> Personas;
    private ArrayAdapter<CharSequence> Plan;
    private ArrayAdapter<CharSequence> Vegetariana;
    private Spinner personas;
    private Spinner hora;
    private Spinner vege;
    private Spinner plan;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new adaptadorIngredientes(ingredientesDataList);
        mAdapters = new adaptadorPasos(pasosDataList);
        if(getArguments() != null)
        {
            id = getArguments().getString("id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recipies_, container, false);

        addIngredientes = v.findViewById(R.id.addIngredientes);
        addSteps = v.findViewById(R.id.addSteps);
        mRecycler = v.findViewById(R.id.recyclerviewIngredientes);
        mRecyclers = v.findViewById(R.id.recyclerviewPasos);
        camara =  v.findViewById(R.id.addImage);
        foto = v.findViewById(R.id.imageProducts);
        titulo = v.findViewById(R.id.Titulo);
        guardar = v.findViewById(R.id.addRecipies);
        calo = v.findViewById(R.id.txtcalorias);
        vege = v.findViewById(R.id.vege);
        plan = v.findViewById(R.id.spinnerplan);
        hora = v.findViewById(R.id.HorarioSpinner);
        personas = v.findViewById(R.id.personas);
        // Inicializar Firestore y Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Configura RecyclerView para Ingredientes
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // Configura RecyclerView para Pasos
        mRecyclers.setAdapter(mAdapters);
        mRecyclers.setLayoutManager(new LinearLayoutManager(getContext()));

        //spinners
        Horario = ArrayAdapter.createFromResource(getContext(),R.array.HorariosSpin, android.R.layout.simple_spinner_item);
        Horario.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hora.setAdapter(Horario);

        Vegetariana = ArrayAdapter.createFromResource(getContext(),R.array.Vege, android.R.layout.simple_spinner_item);
        Vegetariana.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vege.setAdapter(Vegetariana);

        Personas = ArrayAdapter.createFromResource(getContext(),R.array.Personas, android.R.layout.simple_spinner_item);
        Personas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        personas.setAdapter(Personas);

        Plan = ArrayAdapter.createFromResource(getContext(),R.array.PlanRespuesta, android.R.layout.simple_spinner_item);
        Plan.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        plan.setAdapter(Plan);

        addIngredientes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Crear un nuevo objeto IngredientData con un valor predeterminado o en blanco
                IngredientData newIngredient = new IngredientData();
                mAdapter.addIngredient(newIngredient);

                // Notificar al adaptador sobre el cambio
                mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);

                // Desplazar hasta el nuevo elemento
                mRecycler.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        });

        addSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Crear un nuevo objeto StepData con un valor predeterminado o en blanco
                StepData newStep = new StepData();
                mAdapters.addStep(newStep);

                // Notificar al adaptador sobre el cambio
                mAdapters.notifyItemInserted(mAdapters.getItemCount() - 1);

                // Desplazar hasta el nuevo elemento
                mRecyclers.scrollToPosition(mAdapters.getItemCount() - 1);
            }
        });

        foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarTamañoFoto();
            }
        });
        camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera(v);
            }
        });

        if(id == null || id ==""){
            guardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guardarReceta();
                }
            });
        }else{
            obtenerProductos();
            guardar.setText("Modificar");
            guardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modificarRecetas();
                }
            });
        }

        return v;
    }

    private void modificarRecetas() {
        // Obtener los nuevos datos del formulario
        String ntituloReceta = titulo.getText().toString().trim();
        String ncalorias = calo.getText().toString().trim();
        String nhorario = hora.getSelectedItem().toString();
        String nvege = vege.getSelectedItem().toString();
        String npersonas = personas.getSelectedItem().toString();
        String nplan = plan.getSelectedItem().toString();
        List<IngredientData> ningredientes = new ArrayList<>(mAdapter.getIngredientDataList());
        List<StepData> npasos = new ArrayList<>(mAdapters.getStepDataList());

        // Verificar si los campos obligatorios no están vacíos
        if (!TextUtils.isEmpty(ntituloReceta) && !ncalorias.isEmpty()) {
            // Todos los campos obligatorios tienen datos

            // Comprobar si la imagen de la receta es nula (no seleccionada)
            if (foto.getDrawable() == null) {
                Toast.makeText(getContext(), "Debe seleccionar una imagen para la receta", Toast.LENGTH_SHORT).show();
                return; // Salir de la función si no hay imagen seleccionada
            }

            Bitmap imageBitmap = ((BitmapDrawable) foto.getDrawable()).getBitmap();

            if (id != null && !id.isEmpty()) {
                // Si id no es nulo ni vacío, significa que estamos modificando una receta existente
                // Por lo tanto, llamamos a la función para actualizar la receta en lugar de agregar una nueva

                // Actualiza los datos en los adaptadores antes de guardar la receta modificada
                mAdapter.setIngredientDataList(ningredientes);
                mAdapters.setStepDataList(npasos);

                uploadImageAndUpdateRecipe(ntituloReceta, ncalorias, nhorario, nvege , npersonas, ningredientes, npasos, imageBitmap ,nplan);
            } else {
                // Si id es nulo o vacío, significa que estamos creando una nueva receta
                Toast.makeText(getContext(), "No se Modifico", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Alguno de los campos obligatorios está vacío
            Toast.makeText(getContext(), "Completa todos los campos antes de modificar la receta", Toast.LENGTH_SHORT).show();
        }
    }



    private void uploadImageAndUpdateRecipe(String ntituloReceta, String ncalorias, String nhorario, String nvege, String npersonas, List<IngredientData> ningredientes, List<StepData> npasos, Bitmap imageBitmap ,String nplan) {
        // Crear un nombre único para la imagen (puedes personalizarlo según tus necesidades)
        String imageName = "receta_" + System.currentTimeMillis() + ".jpg";

        // Crea una referencia al Storage donde se guardará la imagen
        StorageReference imageRef = storageReference.child("imagesRecetas/" + imageName);

        // Convierte el Bitmap en un array de bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Sube la imagen a Firebase Storage
        UploadTask uploadTask = imageRef.putBytes(imageData);

        // Maneja el resultado de la subida de la imagen
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    // La imagen se ha subido con éxito
                    // Obtiene la URL de descarga de la imagen
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();

                            // Ahora puedes guardar la URL de la imagen junto con los otros datos en Firestore
                            updateRecipeData(ntituloReceta, ncalorias, nhorario, nvege , npersonas, ningredientes, npasos, imageUrl , nplan);
                        }
                    });
                } else {
                    // Error al subir la imagen
                    Toast.makeText(getContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateRecipeData(String ntituloReceta, String ncalorias, String nhorario, String nvege, String npersonas, List<IngredientData> ningredientes, List<StepData> npasos, String imageUrl , String nplan) {
        // Crear un mapa para la receta, incluyendo la URL de la imagen
        Map<String, Object> receta = new HashMap<>();
        receta.put("titulo", ntituloReceta.substring(0, 1).toUpperCase() + ntituloReceta.substring(1).toLowerCase());
        receta.put("calorias", ncalorias);
        receta.put("imagenUrl", imageUrl);
        receta.put("numero_Personas", npersonas);
        receta.put("horario", nhorario);
        receta.put("vegetariana", nvege);
        receta.put("plan" , nplan);
        // Crear una lista de ingredientes como una lista de cadenas
        List<String> listaIngredientes = new ArrayList<>();
        for (IngredientData ingrediente : ningredientes) {
            listaIngredientes.add(ingrediente.getIngrediente());
        }
        receta.put("ingredientes", listaIngredientes);

        // Crear una lista de pasos como una lista de cadenas
        List<String> listaPasos = new ArrayList<>();
        for (StepData paso : npasos) {
            listaPasos.add(paso.getPaso());
        }
        receta.put("pasos", listaPasos);

        // Actualizar los datos de la receta en Firestore
        db.collection("recetas").document(id).update(receta).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), "Receta modificada con éxito", Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error al modificar la receta", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void obtenerProductos() {
        DocumentReference docRef = db.collection("recetas").document(id);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // Establecer los datos actuales en los campos del formulario
                titulo.setText(documentSnapshot.getString("titulo"));
                calo.setText(documentSnapshot.getString("calorias"));

                // Obtener la hora almacenada en Firestore
                String Horariobd = documentSnapshot.getString("horario");
                if (Horariobd != null) {
                    int medidaIndex = Horario.getPosition(Horariobd);
                    if (medidaIndex != -1) {
                        hora.setSelection(medidaIndex);
                    }
                }
                // Obtener la vegetariana almacenada en Firestore
                String vegebd = documentSnapshot.getString("vegetariana");
                if (vegebd != null) {
                    int medidaIndex = Vegetariana.getPosition(vegebd);
                    if (medidaIndex != -1) {
                        vege.setSelection(medidaIndex);
                    }
                }
                // Obtener las personas almacenada en Firestore
                String personasbd = documentSnapshot.getString("numero_Personas");
                if (personasbd != null) {
                    int medidaIndex = Personas.getPosition(personasbd);
                    if (medidaIndex != -1) {
                        personas.setSelection(medidaIndex);
                    }
                }

                // Obtener las personas almacenada en Firestore
                String planbd = documentSnapshot.getString("plan");
                if (planbd != null) {
                    int medidaIndex = Plan.getPosition(planbd);
                    if (medidaIndex != -1) {
                        plan.setSelection(medidaIndex);
                    }
                }

                // Load the image using Glide from the imageURL stored in Firestore.
                String imageURL = documentSnapshot.getString("imagenUrl");
                if (imageURL != null) {
                    Glide.with(getContext()).load(imageURL).into(foto);
                }

                // Obtener la lista de ingredientes almacenados en Firestore
                List<String> ingredientesList = (List<String>) documentSnapshot.get("ingredientes");
                if (ingredientesList != null) {
                    for (String nombreIngrediente : ingredientesList) {
                        // Crear una instancia de IngredientData y establecer el nombre del ingrediente
                        IngredientData ingredienteData = new IngredientData(nombreIngrediente);
                        // Agregar el ingrediente al adaptador de ingredientes
                        mAdapter.addIngredient(ingredienteData);
                    }
                    // Notificar al adaptador que los datos han cambiado
                    mAdapter.notifyDataSetChanged();
                }

                // Obtener la lista de pasos almacenados en Firestore
                List<String> pasosList = (List<String>) documentSnapshot.get("pasos");
                if (pasosList != null) {
                    for (String nombrePaso : pasosList) {
                        // Crear una instancia de StepData y establecer el nombre del paso
                        StepData pasoData = new StepData(nombrePaso);
                        // Agregar el paso al adaptador de pasos
                        mAdapters.addStep(pasoData);
                    }
                    // Notificar al adaptador que los datos han cambiado
                    mAdapters.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error al obtener los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void guardarReceta() {
        // Obtener los datos del formulario
        String tituloReceta = titulo.getText().toString().trim();
        String calorias = calo.getText().toString().trim();
        String horariof = hora.getSelectedItem().toString();
        String vegetariano = vege.getSelectedItem().toString();
        String personasn = personas.getSelectedItem().toString();
        String planr = plan.getSelectedItem().toString();
        List<IngredientData> ingredientes = mAdapter.getIngredientDataList();
        List<StepData> pasos = mAdapters.getStepDataList();

        // Verificar si los campos obligatorios no están vacíos
        if (!TextUtils.isEmpty(tituloReceta) && !calorias.isEmpty()) {
            // Todos los campos obligatorios tienen datos

            // Comprobar si la imagen de la receta es nula (no seleccionada)
            if (foto.getDrawable() == null) {
                Toast.makeText(getContext(), "Debe seleccionar una imagen para la receta", Toast.LENGTH_SHORT).show();
                return; // Salir de la función si no hay imagen seleccionada
            }

            Bitmap imageBitmap = ((BitmapDrawable) foto.getDrawable()).getBitmap();

            // Subir la imagen a Firebase Storage y luego guardar los datos de la receta
            uploadImageAndSaveRecipe(tituloReceta, calorias, horariof, vegetariano, personasn,ingredientes, pasos, imageBitmap, planr);
        } else {
            // Alguno de los campos obligatorios está vacío
            Toast.makeText(getContext(), "Completa todos los campos antes de guardar la receta", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndSaveRecipe(String tituloReceta, String calorias, String horariof, String vegetariano, String personasn, List<IngredientData> ingredientes, List<StepData> pasos, Bitmap imageBitmap, String planr) {
        // Crear un nombre único para la imagen (puedes personalizarlo según tus necesidades)
        String imageName = "receta_" + System.currentTimeMillis() + ".jpg";

        // Crear una referencia al Storage donde se guardará la imagen
        StorageReference imageRef = storageReference.child("imagesRecetas/" + imageName);

        // Convierte el Bitmap en un array de bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Sube la imagen a Firebase Storage
        UploadTask uploadTask = imageRef.putBytes(imageData);

        // Maneja el resultado de la subida de la imagen
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    // La imagen se ha subido con éxito
                    // Obtiene la URL de descarga de la imagen
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();

                            // Ahora puedes guardar la URL de la imagen junto con los otros datos en Firestore
                            saveRecipeData(tituloReceta, calorias, horariof, vegetariano, personasn, ingredientes, pasos, imageUrl,planr);
                        }
                    });
                } else {
                    // Error al subir la imagen
                    Toast.makeText(getContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveRecipeData(String tituloReceta, String calorias, String horariof, String vegetariano, String personasn, List<IngredientData> ingredientes, List<StepData> pasos, String imageUrl, String planr) {
        // Crear un mapa para la receta, incluyendo la URL de la imagen
        Map<String, Object> receta = new HashMap<>();
        receta.put("titulo", tituloReceta.substring(0, 1).toUpperCase() + tituloReceta.substring(1).toLowerCase());
        receta.put("calorias", calorias);
        receta.put("imagenUrl", imageUrl);
        receta.put("numero_Personas", personasn);
        receta.put("horario", horariof);
        receta.put("vegetariana", vegetariano);
        receta.put("plan", planr);

        // Crear una lista de cadenas para los ingredientes
        List<String> listaIngredientes = new ArrayList<>();
        for (IngredientData ingrediente : ingredientes) {
            listaIngredientes.add(ingrediente.getIngrediente());
        }
        receta.put("ingredientes", listaIngredientes);

        // Crear una lista de cadenas para los pasos
        List<String> listaPasos = new ArrayList<>();
        for (StepData paso : pasos) {
            listaPasos.add(paso.getPaso());
        }
        receta.put("pasos", listaPasos);

        // Guardar los datos de la receta en Firestore
        db.collection("recetas").add(receta).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getContext(), "Receta guardada con éxito", Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error al guardar la receta", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if the request code is the same as the one we used for the camera.
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            // Check if the result is RESULT_OK, which means the user took a picture.
            if (resultCode == Activity.RESULT_OK) {
                // Get the image from the camera.
                Bitmap image = (Bitmap) data.getExtras().get("data");

                // Set the image to the ImageView.
                foto.setImageBitmap(image);
            }
        }
    }

    public void Camera(View view) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, 1);
        }
    }

    private void cambiarTamañoFoto() {
        ViewGroup.LayoutParams params = foto.getLayoutParams();
        if (fotoAmpliada) {
            // Cambiar al tamaño original
            params.width = getResources().getDimensionPixelSize(R.dimen.foto_width);
            params.height = getResources().getDimensionPixelSize(R.dimen.foto_height);
        } else {
            // Cambiar al tamaño ampliado
            params.width = getResources().getDimensionPixelSize(R.dimen.foto_ampliada_width);
            params.height = getResources().getDimensionPixelSize(R.dimen.foto_ampliada_height);
        }
        foto.setLayoutParams(params);
        fotoAmpliada = !fotoAmpliada;
    }
}



