package com.example.recipies;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

;

public class Fruits_Vegetables_Fragment extends DialogFragment {
    private boolean fotoAmpliada = false;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private String id;

    // Generar un ID único para el producto
    private String datoID = generateUniqueID();
    private Spinner es;
    private EditText nombre;
    private TextView idFV;
    private EditText gramos;
    private EditText unidades;
    private ImageView foto;
    private ImageButton camara;
    private Button add;
    private FirebaseFirestore db;
    private ArrayAdapter<CharSequence> escoger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            id = getArguments().getString("id");
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fruits_vegetables, container, false);
        db = FirebaseFirestore.getInstance();
        nombre = v.findViewById(R.id.nombreFV);
        idFV = v.findViewById(R.id.IdFV);
        foto = v.findViewById(R.id.imageFV);
        camara =  v.findViewById(R.id.addFV);
        unidades = v.findViewById(R.id.UnidadFV);
        gramos = v.findViewById(R.id.gramosFV);
        add = v.findViewById(R.id.agregarFV);
        es = v.findViewById(R.id.FoV);
        idFV.setText(datoID);

        escoger = ArrayAdapter.createFromResource(getContext(),R.array.FV, android.R.layout.simple_spinner_item);
        escoger.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        es.setAdapter(escoger);

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
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guardarProductos();
                }
            });
        }else{
            obtenerProductos();
            add.setText("Modificar");
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modificarProductos();
                }
            });
        }
        return v;
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

    private  void obtenerProductos(){
        DocumentReference docRef = db.collection("Frutas y Verduras").document(id);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // Establecer los datos actuales en los campos del formulario
                nombre.setText(documentSnapshot.getString("Nombre"));
                unidades.setText(documentSnapshot.getString("Num_Unidades"));
                idFV.setText(documentSnapshot.getString("IDFrutaVerdura"));
                gramos.setText(documentSnapshot.getString("Gramos"));
                // Obtener la Tipo almacenada en Firestore
                String tipobd = documentSnapshot.getString("Tipo");
                if (tipobd != null) {
                    int locacionIndex = escoger.getPosition(tipobd);
                    if (locacionIndex != -1) {
                        es.setSelection(locacionIndex);
                    }
                }
                // Load the image using Glide from the imageURL stored in Firestore.
                String imageURL = documentSnapshot.getString("ImageURL");
                if (imageURL != null) {
                    Glide.with(getContext()).load(imageURL).into(foto);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error al obtener los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void modificarProductos() {
        // Obtener los nuevos datos del formulario
        String nuevoNombre = nombre.getText().toString();
        String nuevaUnidades = unidades.getText().toString();
        String nuevoGramos = gramos.getText().toString();
        String nuevaArea = es.getSelectedItem().toString();

        if (nuevoNombre.isEmpty() || nuevaUnidades.isEmpty() || nuevoGramos.isEmpty() || nuevaArea.isEmpty()) {
            Toast.makeText(getContext(), "Faltan Datos", Toast.LENGTH_SHORT).show();
        } else {
            // Verificar si no hay imagen seleccionada
            if (foto.getDrawable() == null) {
                Toast.makeText(getContext(), "Debe contener una imagen", Toast.LENGTH_SHORT).show();
            } else {
                Bitmap imageBitmap = ((BitmapDrawable) foto.getDrawable()).getBitmap();
                // Upload the image to Firebase Storage and update the product data.
                uploadImageAndUpdateProduct(nuevoNombre, nuevaUnidades, nuevoGramos, nuevaArea,imageBitmap);
            }
        }
    }

    private void uploadImageAndUpdateProduct(String nuevoNombre, String nuevaUnidades, String nuevoGramos, String nuevaArea,Bitmap imageBitmap) {
        // Create a unique filename for the image using a timestamp or any other unique identifier.
        String filename = "image_" + System.currentTimeMillis() + ".jpg";

        // Get a reference to the Firebase Storage root location.
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Create a reference to the specific image file.
        StorageReference imageRef = storageRef.child("imagesFV/" + filename);

        // Convert the image to bytes (JPEG format) for upload.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Upload the image to Firebase Storage.
        UploadTask uploadTask = imageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Image upload successful, get the download URL.
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // The download URL is available here.
                        String imageURL = uri.toString();

                        // Update the product data including the imageURL.
                        updateProductData(nuevoNombre, nuevaUnidades, nuevoGramos, nuevaArea,imageURL);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Image upload failed.
                Toast.makeText(getContext(), "Error uploading image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProductData(String nuevoNombre, String nuevaUnidades, String nuevoGramos, String nuevaArea,String imageURL) {
        Map<String, Object> map = new HashMap<>();
        map.put("Gramos", nuevoGramos);
        map.put("Num_Unidades", nuevaUnidades);
        map.put("Tipo", nuevaArea);
        map.put("Nombre", nuevoNombre.substring(0, 1).toUpperCase() + nuevoNombre.substring(1).toLowerCase());
        map.put("ImageURL", imageURL);

        db.collection("Frutas y Verduras").document(id).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), "Modificado Correctamente", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "No Modificado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarProductos() {
        String datoName = nombre.getText().toString().trim();
        String datoUnidades = unidades.getText().toString().trim();
        String datoGramos = gramos.getText().toString().trim();
        String datoArea = es.getSelectedItem().toString();
        if (datoName.isEmpty() || datoUnidades.isEmpty() || datoGramos.isEmpty() || datoArea.isEmpty()) {
            Toast.makeText(getContext(), "Faltan Datos", Toast.LENGTH_SHORT).show();
        } else {
            // Verificar si no hay imagen seleccionada
            if (foto.getDrawable() == null) {
                Toast.makeText(getContext(), "Debe contener una imagen", Toast.LENGTH_SHORT).show();
            } else {
                // Comprobar si el producto ya existe en la base de datos
                comprobarProductoExistente(datoID, new OnProductCheckListener() {
                    @Override
                    public void onProductCheck(boolean exists) {
                        if (exists) {
                            Toast.makeText(getContext(), "El producto ya existe", Toast.LENGTH_SHORT).show();
                        } else {
                            Bitmap imageBitmap = ((BitmapDrawable) foto.getDrawable()).getBitmap();
                            // Upload the image to Firebase Storage and update the product data.
                            uploadImageAndSaveProduct(datoName, datoID, datoUnidades, datoGramos, datoArea,imageBitmap);
                        }
                    }
                });
            }
        }
    }

    private String generateUniqueID() {
        // Generar un ID único utilizando UUID
        String uniqueID = UUID.randomUUID().toString();
        // Eliminar los caracteres no deseados del ID generado
        uniqueID = uniqueID.replaceAll("-", "").substring(0, 6);
        return uniqueID;
    }

    private void comprobarProductoExistente(String idFrutaVerdura, final OnProductCheckListener listener) {
        Query query = db.collection("Frutas y Verduras").whereEqualTo("IDFrutaVerdura", idFrutaVerdura).limit(1);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    listener.onProductCheck(true); // Producto existe
                } else {
                    listener.onProductCheck(false); // Producto no existe
                }
            }
        });
    }

    private interface OnProductCheckListener {
        void onProductCheck(boolean exists);
    }

    private void uploadImageAndSaveProduct(String datoName, String datoiD, String datoUnidades, String datoGramos, String datoArea,Bitmap imageBitmap) {
        // Create a unique filename for the image using a timestamp or any other unique identifier.
        String filename = "image_" + System.currentTimeMillis() + ".jpg";

        // Get a reference to the Firebase Storage root location.
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Create a reference to the specific image file.
        StorageReference imageRef = storageRef.child("imagesFV/" + filename);

        // Convert the image to bytes (JPEG format) for upload.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Upload the image to Firebase Storage.
        UploadTask uploadTask = imageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Image upload successful, get the download URL.
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // The download URL is available here.
                        String imageURL = uri.toString();

                        // Save the product data including the imageURL.
                        saveProductData(datoName, datoiD, datoUnidades, datoGramos, datoArea,imageURL);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Image upload failed.
                Toast.makeText(getContext(), "Error uploading image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProductData(String datoName, String datoID, String datoUnidades, String datoGramos, String datoArea,String imageURL) {
        Map<String, Object> map = new HashMap<>();
        map.put("Gramos", datoGramos);
        map.put("IDFrutaVerdura", datoID);
        map.put("Tipo", datoArea);
        map.put("Num_Unidades", datoUnidades);
        map.put("Nombre", datoName.substring(0, 1).toUpperCase() + datoName.substring(1).toLowerCase());
        map.put("ImageURL", imageURL);

        db.collection("Frutas y Verduras").document().set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), "Ingresado Correctamente", Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "No Ingresado", Toast.LENGTH_SHORT).show();
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

}