package com.example.recipies;

import static android.content.ContentValues.TAG;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

import POJO.Productos;
import adaptadores.AdaptadorListaMisProductos;

public class Congelador extends Fragment {

    private FloatingActionButton agregaFV;
    private FloatingActionButton agregaProducto;
    private FloatingActionButton agregar;
    private FloatingActionButton scanner;
    private FloatingActionButton Nuevo;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private AdaptadorListaMisProductos adapter;
    private String email;
    private boolean botonesVisibles = false;

    public Congelador() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Load any arguments if necessary
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_congelador, container, false);

        agregaFV = v.findViewById(R.id.agregaFV);
        agregaProducto = v.findViewById(R.id.agregaProducto);
        Nuevo = v.findViewById(R.id.agreganuevo);
        scanner = v.findViewById(R.id.EscanerProductos);
        recyclerView = v.findViewById(R.id.recyclerviewMisProductos);
        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPrefs = requireActivity().getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);

        agregar = v.findViewById(R.id.agregar);
        agregar.setOnClickListener(view1 -> toggleBotonesAdicionales());

        scanner.setOnClickListener(b -> scandesign());

        agregaFV.setOnClickListener(view12 -> {
            agregaFV dialogo = new agregaFV();
            dialogo.show(requireActivity().getSupportFragmentManager(), "Agregar Frutas y Verduras");
        });

        agregaProducto.setOnClickListener(view13 -> {
            agregaProducto dialogo = new agregaProducto();
            dialogo.show(requireActivity().getSupportFragmentManager(), "Agregar Producto");
        });

        Nuevo.setOnClickListener(view14 -> {
            Intent intent = new Intent(getContext(),agregarProductoNuevo.class);
            startActivity(intent);
        });
        // Configurar el RecyclerView con un GridLayoutManager de 4 columnas
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));

        // Crear y establecer el adaptador para el RecyclerView
        adapter = new AdaptadorListaMisProductos(requireContext(), new ArrayList<>(), "Congelador",email);
        recyclerView.setAdapter(adapter);
        obtenerProductosFirestore();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Obtener y mostrar los productos de Firestore
        obtenerProductosFirestore();
    }

    private void obtenerProductosFirestore() {
        db.collection(email + " Productos")
                .whereEqualTo("Ubicacion", "Congelador")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Productos> productosList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Productos producto = document.toObject(Productos.class);
                            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("productos", Context.MODE_PRIVATE);
                            // Verificar si hay una cantidad guardada en SharedPreferences
                            if (sharedPreferences.contains(producto.getNombre() + "_Congelador")) {
                                // Si hay una cantidad guardada, utilizarla
                                int cantidadGuardada = sharedPreferences.getInt(producto.getNombre() + "_Congelador", 0);
                                producto.setCantidad(String.valueOf(cantidadGuardada));
                            } else {
                                // Si no hay una cantidad guardada, utilizar la cantidad de Firebase
                                producto.setCantidad(String.valueOf(producto.getCantidad()));
                            }
                            productosList.add(producto);
                        }
                        adapter.actualizarLista(productosList);
                        adapter.reorganizarLista(); // Mover elementos con cantidad 0 al final de la lista
                    } else {
                        Log.e("CongeladorFragment", "Error al obtener productos", task.getException());
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        guardarCantidadEnSharedPreferences();
        guardarCantidadEnFirestore();
    }

    // Método para guardar las cantidades en SharedPreferences al cerrar el fragmento
    private void guardarCantidadEnSharedPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("productos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (Productos producto : adapter.getListaproductos()) {
            String claveUnica = producto.getNombre() + "_Congelador"; // Usa la ubicación para la clave
            editor.putInt(claveUnica, Integer.parseInt(producto.getCantidad()));
        }
        editor.apply();
    }

    // Actualiza Firestore al cerrar el fragmento
    private void guardarCantidadEnFirestore() {
        for (Productos producto : adapter.getListaproductos()) {
            db.collection(email + " Productos").document(producto.getNombre() + "_Congelador")
                    .update("Cantidad", producto.getCantidad())
                    .addOnSuccessListener(unused -> Log.d("CongeladorFragment", "Cantidad actualizada en Firestore para: " + producto.getNombre()))
                    .addOnFailureListener(e -> Log.e("CongeladorFragment", "Error al actualizar cantidad en Firestore para: " + producto.getNombre(), e));
        }
    }

    private void scandesign() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES);
        options.setPrompt("Escanea el codigo de barras");
        options.setOrientationLocked(true);
        options.setCameraId(0);  // Use a specific camera of the device
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(true);
        barcodeLauncher.launch(options);
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        String  codigoB = result.getContents();
        if (codigoB == null) {
            Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();

        } else {
            buscarProducto(codigoB);
        }
    });

    private void buscarProducto(String codigo) {
        Query query = db.collection("productos").whereEqualTo("CodigoBarras", codigo);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                    Productos producto = document.toObject(Productos.class);
                    // Crear un intent para abrir la actividad MostrarProducto
                    Intent intent = new Intent(getContext(), MostrarProducto.class);
                    // Pasar los detalles del producto a la actividad MostrarProducto
                    intent.putExtra("nombre", producto.getNombre());
                    intent.putExtra("imagenUrl", producto.getImageURL());
                    // Abrir la actividad MostrarProducto
                    startActivity(intent);
                } else {
                    // Producto no encontrado
                    Toast.makeText(getContext(), "Producto no encontrado", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Error al buscar productos", task.getException());
            }
        });
    }


    private void toggleBotonesAdicionales() {
        if (botonesVisibles) {
            Nuevo.hide();
            agregaFV.hide();
            agregaProducto.hide();
        } else {
            Nuevo.show();
            agregaFV.show();
            agregaProducto.show();
        }
        botonesVisibles = !botonesVisibles;
    }
}