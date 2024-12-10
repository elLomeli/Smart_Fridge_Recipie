package adaptadores;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipies.MostrarReceta;
import com.example.recipies.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import POJO.RecetasUsuario;
public class adaptadorRecetasUsuarios extends RecyclerView.Adapter<adaptadorRecetasUsuarios.MyviewHolder> {

    private Context context;
    private ArrayList<RecetasUsuario> recetasUsuariosArrayList;
    private SharedPreferences favoritosPrefs;
    private SharedPreferences ratingPrefs;
    private final Runnable actualizarFavoritosCallback;

    public adaptadorRecetasUsuarios(Context context, ArrayList<RecetasUsuario> recetasUsuariosArrayList, Runnable actualizarFavoritosCallback) {
        this.context = context;
        this.recetasUsuariosArrayList = recetasUsuariosArrayList;
        this.actualizarFavoritosCallback = actualizarFavoritosCallback;
        favoritosPrefs = context.getSharedPreferences("Favoritos", Context.MODE_PRIVATE);
        ratingPrefs = context.getSharedPreferences("Calificaciones", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.vista_recetas_usuarios, parent, false);
        return new MyviewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyviewHolder holder, int position) {
        RecetasUsuario receta = recetasUsuariosArrayList.get(position);

        // Formatear título en negrita
        SpannableStringBuilder titleBuilder = new SpannableStringBuilder(receta.getTitulo());
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        titleBuilder.setSpan(boldSpan, 0, titleBuilder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        holder.titulo.setText(titleBuilder);

        holder.Np.setText(receta.getNumero_Personas());
        holder.HorarioR.setText("Horario: " + receta.getHorario());
        Picasso.get().load(receta.getImagenUrl()).into(holder.imagen);

        // Obtener y mostrar calificación guardada
        float rating = obtenerCalificacionDePreferencias(receta.getId());
        holder.ratingBar.setOnRatingBarChangeListener(null); // Desvincular el listener antes de cambiar el valor para evitar callbacks innecesarios
        holder.ratingBar.setRating(rating);

        // Configurar el listener de cambios de calificación
        holder.ratingBar.setOnRatingBarChangeListener((ratingBar, newRating, fromUser) -> {
            if (fromUser) { // Asegurarse de que el cambio provenga de la interacción del usuario
                guardarCalificacion(receta.getId(), newRating);
                holder.itemView.post(actualizarFavoritosCallback); // Actualización en tiempo real en ambas pantallas
            }
        });

        // Configurar favorito
        final boolean[] esFavorito = {esFavorito(receta.getId())};
        holder.favoriteIcon.setImageResource(esFavorito[0] ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

        // Click para actualizar estado de favorito
        holder.favoriteIcon.setOnClickListener(v -> {
            esFavorito[0] = !esFavorito[0];
            guardarEstadoFavorito(receta.getId(), esFavorito[0]);
            holder.favoriteIcon.setImageResource(esFavorito[0] ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            holder.itemView.post(actualizarFavoritosCallback); // Actualización en tiempo real en ambas pantallas
        });

        // Click para abrir detalle de la receta
        holder.receta.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                RecetasUsuario recetaSeleccionada = recetasUsuariosArrayList.get(currentPosition);

                Intent intent = new Intent(context, MostrarReceta.class);
                intent.putExtra("id", recetaSeleccionada.getId());
                intent.putExtra("imagenUrl", recetaSeleccionada.getImagenUrl());
                context.startActivity(intent);
            }
        });
    }

    private void guardarEstadoFavorito(String id, boolean esFavorito) {
        favoritosPrefs.edit().putBoolean(id, esFavorito).apply();
    }

    public boolean esFavorito(String id) {
        return favoritosPrefs.getBoolean(id, false);
    }

    private void guardarCalificacion(String id, float calificacion) {
        ratingPrefs.edit().putFloat(id, calificacion).apply();
    }

    private float obtenerCalificacionDePreferencias(String id) {
        return ratingPrefs.getFloat(id, 0.0f);
    }

    @Override
    public int getItemCount() {
        return recetasUsuariosArrayList.size();
    }

    public void actualizarLista(ArrayList<RecetasUsuario> listaActualizada) {
        this.recetasUsuariosArrayList = listaActualizada;
        notifyDataSetChanged();
    }

    public static class MyviewHolder extends RecyclerView.ViewHolder {
        ImageView imagen, favoriteIcon;
        TextView titulo, Np, HorarioR;
        RatingBar ratingBar;
        LinearLayout receta;

        public MyviewHolder(@NonNull View itemView) {
            super(itemView);
            receta = itemView.findViewById(R.id.abrirReceta);
            imagen = itemView.findViewById(R.id.imagenRecetas);
            titulo = itemView.findViewById(R.id.titulorecetas);
            Np = itemView.findViewById(R.id.numPersonasrectas);
            HorarioR = itemView.findViewById(R.id.horariorecetas);
            ratingBar = itemView.findViewById(R.id.ratingBarRecetas);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }
    }
}