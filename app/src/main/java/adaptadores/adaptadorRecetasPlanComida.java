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
import com.example.recipies.MostrarRecetaComida;
import com.example.recipies.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import POJO.RecetasPlan;

public class adaptadorRecetasPlanComida extends RecyclerView.Adapter< adaptadorRecetasPlanComida.MyviewHolder> {

    Context context;
    ArrayList<RecetasPlan> recetasPlanArrayList;
    String perfilId;
    Calendar cal = Calendar.getInstance();
    int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);

    public adaptadorRecetasPlanComida(Context context, ArrayList<RecetasPlan> recetasPlanArrayList, String perfilId) {
        this.context = context;
        this.recetasPlanArrayList = recetasPlanArrayList;
        this.perfilId = perfilId;
    }


    @NonNull
    @Override
    public adaptadorRecetasPlanComida.MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.vista_recetas_plan,parent,false);
        return new MyviewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull adaptadorRecetasPlanComida.MyviewHolder holder, int position) {
        RecetasPlan recetasPlan = recetasPlanArrayList.get(position);
        // Crear un texto en negrita para el título
        SpannableStringBuilder titleBuilder = new SpannableStringBuilder(recetasPlan.titulo);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        titleBuilder.setSpan(boldSpan, 0, titleBuilder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        holder.titulo.setText(titleBuilder);
        holder.calorias.setText(recetasPlan.calorias);
        Picasso.get().load(recetasPlan.getImagenUrl()).into(holder.imagen);
        // Configurar la calificación guardada o la calificación predeterminada
        holder.ratingBar.setRating(getRatingFromSharedPreferences(recetasPlan.id));

        // Establecer el oyente de cambio de calificación
        holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            // Guardar la calificación en SharedPreferences con el identificador único
            saveRatingToSharedPreferences(recetasPlan.id, rating);
        });
        // Establecer el fondo y el listener del OnClickListener aquí
        updateBackgroundAndListener(holder, recetasPlan);

    }


    private void updateBackgroundAndListener(adaptadorRecetasPlanComida.MyviewHolder holder, RecetasPlan recetasPlan) {
        // Establecer el fondo según la hora
        if (hourOfDay >= 13 && hourOfDay < 18) {
            holder.receta.setBackgroundResource(R.color.fondoSplash);
            holder.receta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        RecetasPlan recetaSeleccionada = recetasPlanArrayList.get(position);

                        // Crear un Intent para iniciar MostrarReceta
                        Intent intent = new Intent(context, MostrarRecetaComida.class);
                        intent.putExtra("id", recetaSeleccionada.id);
                        intent.putExtra("perfil", perfilId);
                        intent.putExtra("imagenUrl", recetaSeleccionada.getImagenUrl());
                        intent.putExtra("empezar", true);
                        // Iniciar la actividad MostrarReceta
                        context.startActivity(intent);
                    }
                }
            });
        } else {
            holder.receta.setBackgroundResource(R.color.IconGray);
            holder.receta.setOnClickListener(null);
        }
    }
    // Método para guardar la calificación en SharedPreferences
    private void saveRatingToSharedPreferences(String id, float rating) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CalificacionesPlan_" + perfilId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(id, rating);
        editor.apply();
    }

    // Método para obtener la calificación de SharedPreferences
    private float getRatingFromSharedPreferences(String id) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CalificacionesPlan_" + perfilId, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat(id, 0.0f);  // 0.0f es el valor predeterminado si no se encuentra la calificación
    }

    @Override
    public int getItemCount() {
        return recetasPlanArrayList.size();
    }
    public class MyviewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView titulo;
        TextView calorias;
        RatingBar ratingBar;
        LinearLayout receta;
        public MyviewHolder(@NonNull View itemView) {
            super(itemView);
            receta = itemView.findViewById(R.id.abrirReceta);
            imagen = itemView.findViewById(R.id.imagenRecetas);
            titulo = itemView.findViewById(R.id.titulorecetas);
            calorias =itemView.findViewById(R.id.caloriasrecetas);
            ratingBar = itemView.findViewById(R.id.ratingBarRecetas);
        }
    }
}
