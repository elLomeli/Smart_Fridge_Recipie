package com.example.recipies;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.le.ScanResult;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

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
import android.widget.Toast;

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
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;


import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Products_Fragment extends DialogFragment {
    private boolean fotoAmpliada = false;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private ArrayAdapter<CharSequence> medidas;
    private ArrayAdapter<CharSequence> locacion;
    private String id;
    private EditText nombre;
    private EditText marca;
    private EditText codigo;
    private EditText gramos;
    private EditText piezas;
    private ImageView foto;
    private ImageButton camara;
    private Button add;
    private Spinner med;
    private Spinner loca;
    private ImageButton scan;
    private FirebaseFirestore db;
    private String codigoEscaneado;
    private boolean primerEscaneoRealizado = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            id = getArguments().getString("id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_products, container, false);
        db = FirebaseFirestore.getInstance();
        nombre = v.findViewById(R.id.nombreP);
        marca = v.findViewById(R.id.marca);
        codigo = v.findViewById(R.id.codigo);
        foto = v.findViewById(R.id.imageProducts);
        camara =  v.findViewById(R.id.addImage);
        gramos = v.findViewById(R.id.gramos);
        piezas = v.findViewById(R.id.piezas);
        add = v.findViewById(R.id.addProducts);
        scan = v.findViewById(R.id.getcode);
        med = v.findViewById(R.id.medida);
        loca = v.findViewById(R.id.locacion);

        //spinners
        medidas = ArrayAdapter.createFromResource(getContext(),R.array.unidades, android.R.layout.simple_spinner_item);
        medidas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        med.setAdapter(medidas);

        locacion = ArrayAdapter.createFromResource(getContext(),R.array.Areas, android.R.layout.simple_spinner_item);
        locacion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loca.setAdapter(locacion);

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
            scan.setVisibility(View.GONE);
            obtenerProductos();
            add.setText("Modificar");
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modificarProductos();
                }
            });
        }

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBarcodeScanner();
            }
        });
        return v;
    }

    private void openBarcodeScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES);
        options.setPrompt("Scan a barcode");
        options.setOrientationLocked(true);
        options.setCameraId(0);  // Use a specific camera of the device
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(true);
        barcodeLauncher.launch(options);
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        String codigoB = result.getContents();
        if (codigoB == null) {
            Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
        } else {
            if (!primerEscaneoRealizado) {
                // Primer escaneo
                primerEscaneoRealizado = true;
                codigoEscaneado = codigoB;
                Toast.makeText(getContext(), "First scan: " + codigoEscaneado, Toast.LENGTH_LONG).show();
            } else {
                // Segundo escaneo, verificar si coincide con el primer escaneo
                if (codigoEscaneado.equals(codigoB)) {
                    // Ambos escaneos coinciden
                    Toast.makeText(getContext(), "Scanned successfully: " + codigoB, Toast.LENGTH_LONG).show();
                    // Coloca el código escaneado en el campo "datoCodigo"
                    codigo.setText(codigoB);
                } else {
                    // Los códigos escaneados no coinciden, solicitar escaneo nuevamente
                    Toast.makeText(getContext(), "Scan again, codes do not match", Toast.LENGTH_LONG).show();
                    primerEscaneoRealizado = false;
                }
            }
        }
    });


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
        DocumentReference docRef = db.collection("productos").document(id);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // Establecer los datos actuales en los campos del formulario
                nombre.setText(documentSnapshot.getString("Nombre"));
                marca.setText(documentSnapshot.getString("Marca"));
                codigo.setText(documentSnapshot.getString("CodigoBarras"));
                gramos.setText(documentSnapshot.getString("Cantidad"));
                piezas.setText(documentSnapshot.getString("Piezas"));

                // Obtener la medida almacenada en Firestore
                String medidabd = documentSnapshot.getString("Medida");
                if (medidabd != null) {
                    int medidaIndex = medidas.getPosition(medidabd);
                    if (medidaIndex != -1) {
                        med.setSelection(medidaIndex);
                    }
                }

                // Obtener la locacion almacenada en Firestore
                String locacionbd = documentSnapshot.getString("Locacion");
                if (locacionbd != null) {
                    int locacionIndex = locacion.getPosition(locacionbd);
                    if (locacionIndex != -1) {
                        loca.setSelection(locacionIndex);
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
        String nuevaLocacion = loca.getSelectedItem().toString();
        String nuevaMedida = med.getSelectedItem().toString();
        String nuevoNombre = nombre.getText().toString();
        String nuevaMarca = marca.getText().toString();
        String nuevoCodigo = codigo.getText().toString();
        String nuevoGramos = gramos.getText().toString();
        String nuevasPiezas = piezas.getText().toString();


        if (nuevoNombre.isEmpty() || nuevaMarca.isEmpty() || nuevoCodigo.isEmpty() || nuevoGramos.isEmpty() || nuevaMedida.isEmpty() || nuevasPiezas.isEmpty() || nuevaLocacion.isEmpty()) {
            Toast.makeText(getContext(), "Faltan Datos", Toast.LENGTH_SHORT).show();
        } else {
            // Verificar si no hay imagen seleccionada
            if (foto.getDrawable() == null) {
                Toast.makeText(getContext(), "Debe contener una imagen", Toast.LENGTH_SHORT).show();
            } else {
                Bitmap imageBitmap = ((BitmapDrawable) foto.getDrawable()).getBitmap();
                // Upload the image to Firebase Storage and update the product data.
                uploadImageAndUpdateProduct(nuevoNombre, nuevaMarca,nuevasPiezas, nuevoCodigo, nuevoGramos, nuevaMedida , nuevaLocacion,imageBitmap);
            }
        }
    }

    private void uploadImageAndUpdateProduct(String nuevoNombre, String nuevaMarca, String nuevasPiezas,String nuevoCodigo, String nuevoGramos, String nuevaMedida, String nuevaLocacion,Bitmap imageBitmap) {
        // Create a unique filename for the image using a timestamp or any other unique identifier.
        String filename = "image_" + System.currentTimeMillis() + ".jpg";

        // Get a reference to the Firebase Storage root location.
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Create a reference to the specific image file.
        StorageReference imageRef = storageRef.child("imagesProducts/" + filename);

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
                        updateProductData(nuevoNombre, nuevaMarca, nuevasPiezas, nuevoCodigo, nuevoGramos,nuevaMedida, nuevaLocacion,imageURL);
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

    private void updateProductData(String nuevoNombre, String nuevaMarca, String nuevasPiezas, String nuevoCodigo, String nuevoGramos, String nuevaMedida,String nuevaLocacion,String imageURL) {
        Map<String, Object> map = new HashMap<>();
        map.put("Cantidad", nuevoGramos);
        map.put("Medida", nuevaMedida);
        map.put("Piezas", nuevasPiezas);
        map.put("Locacion", nuevaLocacion);
        map.put("CodigoBarras", nuevoCodigo);
        map.put("Marca", nuevaMarca.substring(0, 1).toUpperCase() + nuevaMarca.substring(1).toLowerCase());
        map.put("Nombre", nuevoNombre.substring(0, 1).toUpperCase() + nuevoNombre.substring(1).toLowerCase());
        map.put("ImageURL", imageURL);

        db.collection("productos").document(id).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    private void guardarProductos(){
        String datoMedida = med.getSelectedItem().toString();
        String datoLocacion = loca.getSelectedItem().toString();
        String datoName = nombre.getText().toString().trim();
        String datoMarca = marca.getText().toString().trim();
        String datoCodigo = codigo.getText().toString().trim();
        String datoGramos = gramos.getText().toString().trim();
        String datoPiezas = piezas.getText().toString().trim();

        if (datoName.isEmpty() || datoMarca.isEmpty() || datoCodigo.isEmpty() || datoGramos.isEmpty() || datoMedida.isEmpty() || datoPiezas.isEmpty() || datoLocacion.isEmpty()) {
            Toast.makeText(getContext(), "Faltan Datos", Toast.LENGTH_SHORT).show();
        } else {
            // Verificar si no hay imagen seleccionada
            if (foto.getDrawable() == null) {
                Toast.makeText(getContext(), "Debe contener una imagen", Toast.LENGTH_SHORT).show();
            } else {
                // Comprobar si el producto ya existe en la base de datos
                comprobarProductoExistente(datoCodigo, new OnProductCheckListener() {
                    @Override
                    public void onProductCheck(boolean exists) {
                        if (exists) {
                            Toast.makeText(getContext(), "El producto ya existe", Toast.LENGTH_SHORT).show();
                        } else {
                            Bitmap imageBitmap = ((BitmapDrawable) foto.getDrawable()).getBitmap();
                            // Upload the image to Firebase Storage and update the product data.
                            uploadImageAndSaveProduct(datoName, datoMarca, datoPiezas, datoCodigo, datoGramos, datoMedida,datoLocacion, imageBitmap);
                        }
                    }
                });
            }
        }
    }

    private void comprobarProductoExistente(String codigoProducto, final OnProductCheckListener listener) {
        Query query = db.collection("productos").whereEqualTo("CodigoBarras", codigoProducto).limit(1);
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

    private void uploadImageAndSaveProduct(String datoName, String datoMarca, String datoPiezas,String datoCodigo, String datoGramos, String datoMedida, String datoLocacion,Bitmap imageBitmap) {
        // Create a unique filename for the image using a timestamp or any other unique identifier.
        String filename = "image_" + System.currentTimeMillis() + ".jpg";

        // Get a reference to the Firebase Storage root location.
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Create a reference to the specific image file.
        StorageReference imageRef = storageRef.child("imagesProducts/" + filename);

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
                        saveProductData(datoName, datoMarca, datoPiezas, datoCodigo, datoGramos, datoMedida,datoLocacion,imageURL);
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

    private void saveProductData(String datoName, String datoMarca, String datoPiezas,String datoCodigo, String datoGramos,String datoMedida,String datoLocacion,String imageURL) {
        Map<String, Object> map = new HashMap<>();
        map.put("Cantidad", datoGramos);
        map.put("Medida", datoMedida);
        map.put("Piezas", datoPiezas);
        map.put("CodigoBarras", datoCodigo);
        map.put("Locacion", datoLocacion);
        map.put("Marca", datoMarca.substring(0, 1).toUpperCase() + datoMarca.substring(1).toLowerCase());
        map.put("Nombre", datoName.substring(0, 1).toUpperCase() + datoName.substring(1).toLowerCase());
        map.put("ImageURL", imageURL);

        db.collection("productos").document().set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
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