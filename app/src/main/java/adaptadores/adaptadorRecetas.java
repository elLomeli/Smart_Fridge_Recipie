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
import com.example.recipies.Recipies_Fragment;
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

import POJO.Recetas;

public class adaptadorRecetas extends FirestoreRecyclerAdapter<Recetas, adaptadorRecetas.ViewHolder> {

    private FragmentManager fm;
    private boolean mostrarCabezas;
    private FloatingActionButton ejecutarCabezas;
    private FloatingActionButton dead;
    private FirebaseFirestore db;
    private List<String> selectedIds; // Lista de IDs de productos seleccionados
    private Context context;

    public adaptadorRecetas(@NonNull FirestoreRecyclerOptions<Recetas> options, FragmentManager fm, FloatingActionButton ejecutarCabezas, FloatingActionButton dead, Context context) {
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
    public adaptadorRecetas.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vista_recetas, parent, false);
        return new ViewHolder(v);
    }


    @Override
    protected void onBindViewHolder(@NonNull adaptadorRecetas.ViewHolder holder, int position, @NonNull Recetas recetas) {
        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
        final String id = documentSnapshot.getId();

        holder.titulo.setText(recetas.getTitulo());
        // Utiliza Picasso para cargar la imagen en el ImageView
        Picasso.get().load(recetas.getImagenUrl()).into(holder.foto);

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
            Recipies_Fragment fragment = new Recipies_Fragment();
            Bundle bundle = new Bundle();
            bundle.putString("id", id);
            fragment.setArguments(bundle);
            fragment.show(fm, "modificar receta");
        });

    }

    public void mostrarCheckBoxCabezas() {
        mostrarCabezas = !mostrarCabezas;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo;
        ImageButton editar;
        ImageView foto;
        CheckBox cabezas;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.vistaT);
            editar = itemView.findViewById(R.id.btn_edit);
            foto = itemView.findViewById(R.id.imageRe);
            cabezas = itemView.findViewById(R.id.cortarcabezas);
        }
    }

    // Elimina productos seleccionados
    public void setSeleccionado() {
        if (mostrarCabezas) {
            // Eliminar los productos seleccionados
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            for (String id : selectedIds) {
                deleteRecipe(id); // Llama al método para eliminar recetas
            }

            // Limpiar la lista de IDs seleccionados
            selectedIds.clear();

            // Ocultar los checkboxes y actualizar la vista
            mostrarCheckBoxCabezas();
        }
    }

    private void deleteRecipe(String id) {
        db = FirebaseFirestore.getInstance();
        // Eliminar la receta de Firestore
        db.collection("recetas").document(id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Receta eliminada correctamente", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error al eliminar la receta", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
