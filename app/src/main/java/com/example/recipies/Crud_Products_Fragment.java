package com.example.recipies;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.squareup.picasso.Picasso;

import POJO.Productos;
import DeletePhotos.ImageCheckerProducts;
import adaptadores.adaptadorProductos;


public class Crud_Products_Fragment extends Fragment {
    private FloatingActionButton agregar;
    private FloatingActionButton ejecutarCabezas;
    private FloatingActionButton verdugoDeCabezas;
    private FloatingActionButton scanner;
    private FirebaseFirestore db;
    private RecyclerView mRecycler;
    private adaptadorProductos mAdapter;
    private Query query;
    private SearchView buscador;
    private ImageCheckerProducts imageChecker;

    public Crud_Products_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_crud__products_, container, false);
        buscador = v.findViewById(R.id.searchProduct);
        agregar = v.findViewById(R.id.agregarProductos);
        ejecutarCabezas = v.findViewById(R.id.eliminarProductos);
        verdugoDeCabezas = v.findViewById(R.id.verdugo);
        scanner = v.findViewById(R.id.EscanerProductos);
        mRecycler = v.findViewById(R.id.recyclerviewProducts);
        imageChecker = new ImageCheckerProducts();
        db = FirebaseFirestore.getInstance();



        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Products_Fragment fragment = new Products_Fragment();
                fragment.show(getParentFragmentManager(), "Agregar Productos");
                buscador.clearFocus();
            }
        });

        buscador.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String t) {
                buscador(capitalize(t));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String nextt) {
                try {
                    buscador(capitalize(nextt));
                } catch (Exception e) {
                    Log.e(TAG, "Error al buscar productos", e);
                }
                return false;
            }
        });

        ejecutarCabezas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.mostrarCheckBoxCabezas();
            }
        });

        scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scandesign();
            }
        });

        imageChecker.start();
        actualizarPantalla();
        return v;
    }

    private void scandesign() {
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
        String  codigoB = result.getContents();
        if (codigoB == null) {
            Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
            buscador.clearFocus();
        } else {
            buscarProducto(codigoB);
            buscador.clearFocus();
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
                    // Crear el AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    View dialogView = getLayoutInflater().inflate(R.layout.vista_scanner_productos, null);

                    // Obtener referencias a los elementos de la vista del AlertDialog
                    ImageView imageView = dialogView.findViewById(R.id.imageScan);
                    TextView textView = dialogView.findViewById(R.id.NombreScan);

                    // Establecer la imagen y el nombre del producto en los elementos de la vista
                    Picasso.get().load(producto.getImageURL()).into(imageView);
                    textView.setText(producto.getNombre());

                    // Establecer la vista personalizada del AlertDialog
                    builder.setView(dialogView);
                    // Mostrar el AlertDialog
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    //Toast.makeText(requireContext(), "Producto: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
                } else {
                    // Producto no encontrado
                    Toast.makeText(requireContext(), "Producto no encontrado", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Error al buscar productos", task.getException());
            }
        });
    }

    private void actualizarPantalla() {
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        query = db.collection("productos");
        FirestoreRecyclerOptions<Productos> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Productos>().setQuery(query, Productos.class).build();
        mAdapter = new adaptadorProductos(firestoreRecyclerOptions, getParentFragmentManager(),ejecutarCabezas,verdugoDeCabezas,getContext());
        mRecycler.setAdapter(mAdapter);
    }

    private void buscador(String t) {
        if (t.isEmpty()) {
            mostrarTodosProductos();
            return;
        }
        mAdapter.stopListening(); // Detener la escucha del adaptador actual

        Query searchQuery = query.orderBy("Nombre").startAt(t).endAt(t + "~");
        searchQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        FirestoreRecyclerOptions<Productos> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Productos>().setQuery(searchQuery, Productos.class).build();
                        mAdapter = new adaptadorProductos(firestoreRecyclerOptions, getParentFragmentManager(),ejecutarCabezas,verdugoDeCabezas,getContext());
                        mAdapter.startListening();
                        mRecycler.setAdapter(mAdapter);
                    } else {
                        mAdapter.stopListening(); // Detener la escucha del adaptador actual
                        mostrarTodosProductos();
                    }
                } else {
                    Log.e(TAG, "Error al buscar productos", task.getException());
                }
            }
        });
    }

    private void mostrarTodosProductos() {
        if (mAdapter != null) {
            mAdapter.stopListening(); // Detener la escucha del adaptador actual
        }
        FirestoreRecyclerOptions<Productos> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Productos>().setQuery(query, Productos.class).build();
        mAdapter = new adaptadorProductos(firestoreRecyclerOptions, getParentFragmentManager(),ejecutarCabezas,verdugoDeCabezas,getContext());
        mAdapter.startListening();
        mRecycler.setAdapter(mAdapter);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.trim().substring(0, 1).toUpperCase() + s.trim().substring(1).toLowerCase();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}

