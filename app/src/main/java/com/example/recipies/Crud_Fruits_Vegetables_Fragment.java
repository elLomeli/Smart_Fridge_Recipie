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

import POJO.F_V;
import DeletePhotos.ImageCheckerFV;
import adaptadores.adaptadorFV;


public class Crud_Fruits_Vegetables_Fragment extends Fragment {

    private FloatingActionButton agregarFV;
    private FloatingActionButton ejecutarCabezasFV;
    private FloatingActionButton verdugoDeCabezasFV;
    private FirebaseFirestore db;
    private RecyclerView mRecyclerFV;
    private adaptadorFV mAdapterFV;
    private Query query;
    private SearchView buscadorFV;
    private ImageCheckerFV imageChecker;
    public Crud_Fruits_Vegetables_Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_crud__fruits__vegetables_, container, false);
        buscadorFV = v.findViewById(R.id.searchFV);
        agregarFV = v.findViewById(R.id.agregarFV);
        ejecutarCabezasFV = v.findViewById(R.id.eliminarFV);
        verdugoDeCabezasFV = v.findViewById(R.id.verdugoFV);
        mRecyclerFV = v.findViewById(R.id.recyclerviewFV);
        imageChecker = new ImageCheckerFV();
        db = FirebaseFirestore.getInstance();



        agregarFV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fruits_Vegetables_Fragment fragment = new Fruits_Vegetables_Fragment();
                fragment.show(getParentFragmentManager(), "Agregar Productos");
                buscadorFV.clearFocus();
            }
        });

        buscadorFV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        ejecutarCabezasFV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterFV.mostrarCheckBoxCabezas();
            }
        });

        imageChecker.start();
        actualizarPantalla();
        return v;
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        String  codigoB = result.getContents();
        if (codigoB == null) {
            Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
            buscadorFV.clearFocus();
        } else {
            buscarProducto(codigoB);
            buscadorFV.clearFocus();
        }
    });

    private void buscarProducto(String codigo) {
        Query query = db.collection("Frutas y Verduras").whereEqualTo("IDFrutaVerdura", codigo);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                    F_V producto = document.toObject(F_V.class);
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
        mRecyclerFV.setLayoutManager(new LinearLayoutManager(getActivity()));
        query = db.collection("Frutas y Verduras");
        FirestoreRecyclerOptions<F_V> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<F_V>().setQuery(query, F_V.class).build();
        mAdapterFV = new adaptadorFV(firestoreRecyclerOptions, getParentFragmentManager(),ejecutarCabezasFV,verdugoDeCabezasFV,getContext());
        mRecyclerFV.setAdapter(mAdapterFV);
    }

    private void buscador(String t) {
        if (t.isEmpty()) {
            mostrarTodosProductos();
            return;
        }
        mAdapterFV.stopListening(); // Detener la escucha del adaptador actual

        Query searchQuery = query.orderBy("Nombre").startAt(t).endAt(t + "~");
        searchQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        FirestoreRecyclerOptions<F_V> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<F_V>().setQuery(searchQuery, F_V.class).build();
                        mAdapterFV = new adaptadorFV(firestoreRecyclerOptions, getParentFragmentManager(),ejecutarCabezasFV,verdugoDeCabezasFV,getContext());
                        mAdapterFV.startListening();
                        mRecyclerFV.setAdapter(mAdapterFV);
                    } else {
                        mAdapterFV.stopListening(); // Detener la escucha del adaptador actual
                        mostrarTodosProductos();
                    }
                } else {
                    Log.e(TAG, "Error al buscar productos", task.getException());
                }
            }
        });
    }

    private void mostrarTodosProductos() {
        if (mAdapterFV != null) {
            mAdapterFV.stopListening(); // Detener la escucha del adaptador actual
        }
        FirestoreRecyclerOptions<F_V> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<F_V>().setQuery(query, F_V.class).build();
        mAdapterFV = new adaptadorFV(firestoreRecyclerOptions, getParentFragmentManager(),ejecutarCabezasFV,verdugoDeCabezasFV,getContext());
        mAdapterFV.startListening();
        mRecyclerFV.setAdapter(mAdapterFV);
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
        mAdapterFV.startListening();
        mRecyclerFV.setAdapter(mAdapterFV);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapterFV.stopListening();
    }
}