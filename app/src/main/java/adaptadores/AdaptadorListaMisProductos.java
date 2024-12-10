package adaptadores;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipies.MostrarFV;
import com.example.recipies.MostrarProducto;
import com.example.recipies.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import POJO.Productos;
import androidx.core.content.ContextCompat;

public class AdaptadorListaMisProductos extends RecyclerView.Adapter<AdaptadorListaMisProductos.MyViewHolder> {

    private Context context;
    private List<Productos> listaproductos;
    private String ubicacion;
    private FirebaseFirestore db;
    private String email;

    // Lista de ubicaciones para mostrar MostrarProducto
    private final List<String> ubicacionesMostrarProducto = Arrays.asList(
            "Abarrotes", "Botanas", "Carnes", "Congelados", "Dulces", "Enlatados",
            "Lacteos y Quesos", "Pescados", "Refrescos", "Salchichoneria", "Vinos y Licores"
    );

    public AdaptadorListaMisProductos(Context context, List<Productos> listaproductos, String ubicacion, String email) {
        this.context = context;
        this.listaproductos = listaproductos;
        this.ubicacion = ubicacion;
        this.db = FirebaseFirestore.getInstance();
        this.email = email;
    }

    @NonNull
    @Override
    public AdaptadorListaMisProductos.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vista_misproductos, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorListaMisProductos.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Productos producto = listaproductos.get(position);
        holder.textNombre.setText(producto.getNombre());
        holder.cantidad.setText(String.valueOf(producto.getCantidad()));
        Picasso.get().load(producto.getImageURL()).into(holder.imageProducto);

        int cantidadActual = Integer.parseInt(holder.cantidad.getText().toString());

        // Cambiar el color de fondo si la cantidad es 0
        if (cantidadActual == 0) {
            holder.fondo.setBackgroundColor(ContextCompat.getColor(context, R.color.IconGray));
            holder.btnmenos.setVisibility(View.INVISIBLE);
        } else {
            holder.fondo.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            holder.btnmenos.setVisibility(View.VISIBLE);
        }

        // Listener para el botón de incrementar cantidad
        holder.btnmas.setOnClickListener(v -> {
            int nuevaCantidad = Integer.parseInt(holder.cantidad.getText().toString()) + 1;

            if (cantidadActual == 0) {
                // Si la cantidad era 0, primero eliminar el producto de la base de datos y SharedPreferences
                db.collection(email + " Productos").document(producto.getNombre() + "_" + ubicacion)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            // Eliminar de SharedPreferences
                            SharedPreferences sharedPreferences = context.getSharedPreferences("productos", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove(producto.getNombre() + "_" + ubicacion);
                            editor.apply();

                            // Mostrar actividad para volver a agregar el producto
                            Intent intent;
                            if (esProductoGeneral(producto.getLocacion())) {
                                intent = new Intent(context, MostrarProducto.class);
                            } else {
                                intent = new Intent(context, MostrarFV.class);
                            }
                            intent.putExtra("nombre", producto.getNombre());
                            intent.putExtra("imagenUrl", producto.getImageURL());
                            context.startActivity(intent);
                        })
                        .addOnFailureListener(e -> Log.e("AdaptadorListaMisProductos", "Error al eliminar el producto de Firestore", e));
            } else {
                holder.cantidad.setText(String.valueOf(nuevaCantidad));
                listaproductos.get(position).setCantidad(String.valueOf(nuevaCantidad));
                actualizarCantidadEnFirestore(producto.getNombre(), nuevaCantidad);

                if (nuevaCantidad > 0) {
                    holder.fondo.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    holder.btnmenos.setVisibility(View.VISIBLE);
                }
            }
        });

        // Listener para el botón de disminuir cantidad
        holder.btnmenos.setOnClickListener(v -> {
            final int[] cantidad = {Integer.parseInt(holder.cantidad.getText().toString())};
            if (cantidad[0] > 0) {
                if (cantidad[0] == 1) {
                    // Mostrar un diálogo de confirmación cuando la cantidad llegue a 0
                    new AlertDialog.Builder(context)
                            .setTitle("Confirmación")
                            .setMessage("¿Seguro que se acabó el producto?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                cantidad[0]--;
                                holder.cantidad.setText(String.valueOf(cantidad[0]));
                                listaproductos.get(position).setCantidad(String.valueOf(cantidad[0]));
                                holder.fondo.setBackgroundColor(ContextCompat.getColor(context, R.color.IconGray));
                                holder.btnmenos.setVisibility(View.INVISIBLE);
                                actualizarCaducidadEnFirestore(producto.getNombre(), 0);
                                actualizarCantidadEnFirestore(producto.getCantidad(), 0);
                                guardarCantidadEnSharedPreferences(producto.getNombre(), 0);
                                notifyDataSetChanged();
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    cantidad[0]--;
                    holder.cantidad.setText(String.valueOf(cantidad[0]));
                    listaproductos.get(position).setCantidad(String.valueOf(cantidad[0]));

                    if (cantidad[0] == 0) {
                        holder.fondo.setBackgroundColor(ContextCompat.getColor(context, R.color.IconGray));
                        holder.btnmenos.setVisibility(View.INVISIBLE);

                    }
                }
            }
        });
    }

    // Método para reorganizar la lista y mover elementos con cantidad 0 al final
    public void reorganizarLista() {
        List<Productos> productosTemp = new ArrayList<>(listaproductos);
        listaproductos.clear();

        for (Productos producto : productosTemp) {
            if (Integer.parseInt(producto.getCantidad()) > 0) {
                listaproductos.add(producto);
            }
        }

        for (Productos producto : productosTemp) {
            if (Integer.parseInt(producto.getCantidad()) == 0) {
                listaproductos.add(producto);
            }
        }

        notifyDataSetChanged();
    }

    // Método para actualizar la lista de productos
    public void actualizarLista(List<Productos> nuevaLista) {
        listaproductos.clear();
        listaproductos.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listaproductos.size();
    }

    public List<Productos> getListaproductos() {
        return listaproductos;
    }

    // ViewHolder para el RecyclerView
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageProducto;
        private TextView textNombre;
        private TextView cantidad;
        private ImageButton btnmenos, btnmas;
        private LinearLayout fondo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProducto = itemView.findViewById(R.id.imageProducto);
            textNombre = itemView.findViewById(R.id.txtNombre);
            cantidad = itemView.findViewById(R.id.editTextNumber);
            btnmas = itemView.findViewById(R.id.btnmas);
            btnmenos = itemView.findViewById(R.id.btnmenos);
            fondo = itemView.findViewById(R.id.fondo);
        }
    }

    // Método para verificar si es un producto general o una fruta/verdura
    private boolean esProductoGeneral(String locacion) {
        return ubicacionesMostrarProducto.contains(locacion);
    }

    // Método para eliminar el producto de SharedPreferences
    private void eliminarProductoDeSharedPreferences(String nombreProducto) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("productos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String claveUnica = nombreProducto + "_" + ubicacion;

        if (sharedPreferences.contains(claveUnica)) {
            editor.remove(claveUnica);
            editor.apply();
            Log.d("AdaptadorListaMisProductos", "Cantidad eliminada de SharedPreferences para: " + nombreProducto);
        }
    }

    // Método para actualizar la caducidad en Firestore
    private void actualizarCaducidadEnFirestore(String nombreProducto, int caducidad) {
        db.collection(email + " Productos")
                .document(nombreProducto + "_" + ubicacion)
                .update("Caducidad", caducidad)
                .addOnSuccessListener(aVoid -> Log.d("AdaptadorListaMisProductos", "Caducidad actualizada en Firestore para: " + nombreProducto))
                .addOnFailureListener(e -> Log.e("AdaptadorListaMisProductos", "Error al actualizar caducidad en Firestore para: " + nombreProducto, e));
    }

    // Método para actualizar la cantidad en Firestore
    private void actualizarCantidadEnFirestore(String nombreProducto, int nuevaCantidad) {
        db.collection(email + " Productos")
                .document(nombreProducto + "_" + ubicacion)
                .update("Cantidad", String.valueOf(nuevaCantidad))
                .addOnSuccessListener(aVoid -> Log.d("AdaptadorListaMisProductos", "Cantidad actualizada en Firestore para: " + nombreProducto))
                .addOnFailureListener(e -> Log.e("AdaptadorListaMisProductos", "Error al actualizar cantidad en Firestore para: " + nombreProducto, e));
    }

    // Método para guardar la cantidad en SharedPreferences
    private void guardarCantidadEnSharedPreferences(String nombreProducto, int cantidad) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("productos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String claveUnica = nombreProducto + "_" + ubicacion;
        editor.putInt(claveUnica, cantidad);
        editor.apply();
        Log.d("AdaptadorListaMisProductos", "Cantidad guardada en SharedPreferences para: " + nombreProducto);
    }
}