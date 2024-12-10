package adaptadores;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipies.MostrarFV;
import com.example.recipies.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import POJO.F_V;
import POJO.Productos;

public class AdaptadorListaFV extends RecyclerView.Adapter<AdaptadorListaFV.MyViewHolder> {

    private Context context;
    private List<F_V> listaF_V;

    public AdaptadorListaFV(Context context, ArrayList<F_V> listaF_V) {
        this.context = context;
        this.listaF_V = listaF_V;
    }

    @NonNull
    @Override
    public AdaptadorListaFV.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vista_listaf_v, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorListaFV.MyViewHolder holder, int position) {
        F_V producto = listaF_V.get(position);

        holder.textNombre.setText(producto.getNombre());
        Picasso.get().load(producto.getImageURL()).into(holder.imageProducto);

        holder.imageProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {

                    // Crear un Intent para iniciar MostrarReceta
                    Intent intent = new Intent(context, MostrarFV.class);
                    intent.putExtra("nombre", producto.getNombre());
                    intent.putExtra("imagenUrl",producto.getImageURL());
                    // Iniciar la actividad MostrarReceta
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return listaF_V.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageProducto;
        private TextView textNombre;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProducto = itemView.findViewById(R.id.imageProducto);
            textNombre = itemView.findViewById(R.id.txtNombre);
        }
    }
}