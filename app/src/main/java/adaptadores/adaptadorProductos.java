package adaptadores;


import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipies.Products_Fragment;
import com.example.recipies.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import POJO.Productos;
import POJO.RecetasUsuario;

public class adaptadorProductos extends FirestoreRecyclerAdapter<Productos, adaptadorProductos.ViewHolder> {
    private FragmentManager fm;
    private boolean mostrarCabezas;
    private FloatingActionButton ejecutarCabezas;
    private FloatingActionButton dead;
    private FirebaseFirestore db;
    private List<String> selectedIds; // Lista de IDs de productos seleccionados
    private Context context;

    public adaptadorProductos(@NonNull FirestoreRecyclerOptions<Productos> options, FragmentManager fm, FloatingActionButton ejecutarCabezas, FloatingActionButton dead,Context context) {
        super(options);
        this.fm = fm;
        this.mostrarCabezas = false;
        this.ejecutarCabezas = ejecutarCabezas;
        this.dead = dead;
        this.selectedIds = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vista_productos, parent, false);
        return new ViewHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull adaptadorProductos.ViewHolder holder, int position, @NonNull Productos product) {
        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
        final String id = documentSnapshot.getId(); // el id es el codigo de barras

        holder.nombre.setText(product.getNombre());
        holder.marca.setText(product.getMarca());

        // Cargar la imagen utilizando Picasso
        Picasso.get().load(product.getImageURL()).into(holder.foto);

        if(mostrarCabezas)
        {
            holder.cabezas.setVisibility(View.VISIBLE);
            dead.setVisibility(View.VISIBLE);
            ejecutarCabezas.setImageResource(R.drawable.cancel);
        }
        else
        {
            holder.cabezas.setVisibility(View.GONE);
            dead.setVisibility(View.GONE);
            ejecutarCabezas.setImageResource(R.drawable.borrar);
            selectedIds.clear();
        }

        // Establecer el estado seleccionado del checkbox
        holder.cabezas.setChecked(selectedIds.contains(id));

        holder.cabezas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedIds.add(id);
            } else {
                selectedIds.remove(id);
            }
        });

        dead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSeleccionado();
            }
        });

        holder.editar.setOnClickListener(v -> {
            Products_Fragment fragment = new Products_Fragment();
            Bundle bundle = new Bundle();
            bundle.putString("id", id);
            fragment.setArguments(bundle);
            fragment.show(fm, "modificar producto");
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre;
        TextView marca;
        ImageButton editar;
        ImageView foto;
        CheckBox cabezas;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.vistaN);
            marca = itemView.findViewById(R.id.vistaM);
            editar = itemView.findViewById(R.id.btn_edit);
            foto = itemView.findViewById(R.id.imagePose);
            cabezas = itemView.findViewById(R.id.cortarcabezas);
        }
    }

    public void mostrarCheckBoxCabezas() {
        mostrarCabezas = !mostrarCabezas;
        notifyDataSetChanged();
    }

    public void setSeleccionado() {
        if (mostrarCabezas) {
            // Eliminar los productos seleccionados
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            for (String id : selectedIds) {
                deleteProductAndImage(id, null); // imageURL es null ya que no se utiliza en este método
            }

            // Limpiar la lista de IDs seleccionados
            selectedIds.clear();

            // Ocultar los checkboxes y actualizar la vista
            mostrarCheckBoxCabezas();
        }
    }

    private void deleteProductAndImage(String id, String imageURL) {
        db = FirebaseFirestore.getInstance();
        // Eliminar el producto de Firestore
        db.collection("productos").document(id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error al eliminar el producto", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

