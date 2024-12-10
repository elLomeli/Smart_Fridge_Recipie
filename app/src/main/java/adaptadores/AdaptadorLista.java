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

import com.example.recipies.MostrarProducto;
import com.example.recipies.MostrarReceta;
import com.example.recipies.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import POJO.Productos;

public class AdaptadorLista extends RecyclerView.Adapter<AdaptadorLista.MyViewHolder> {

    private Context context;
    private List<Productos> listaproductos;

    public AdaptadorLista(Context context, List<Productos> listaproductos) {
        this.context = context;
        this.listaproductos = listaproductos;
    }

    @NonNull
    @Override
    public AdaptadorLista.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vista_listaproductos, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorLista.MyViewHolder holder, int position) {
        Productos producto = listaproductos.get(position);

        holder.textNombre.setText(producto.getNombre());
        Picasso.get().load(producto.getImageURL()).into(holder.imageProducto);

        holder.imageProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {

                    // Crear un Intent para iniciar MostrarReceta
                    Intent intent = new Intent(context, MostrarProducto.class);
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
        return listaproductos.size();
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