package adaptadores;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipies.R;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import LISTAS.IngredientData;

public class adaptadorIngredientesMostrar extends RecyclerView.Adapter<adaptadorIngredientesMostrar.ViewHolder> {

    private Context context;
    private List<IngredientData> ingredientesList;
    private Map<String, Integer> productosDisponibles;

    public adaptadorIngredientesMostrar(Context context, List<IngredientData> ingredientesList) {
        this.context = context;
        this.ingredientesList = ingredientesList;
        this.productosDisponibles = new HashMap<>();
    }

    public void actualizarProductosDisponibles(Map<String, Integer> productosDisponibles) {
        this.productosDisponibles = productosDisponibles;
        notifyDataSetChanged();
    }

    public void addIngredient(IngredientData newIngredient) {
        // Realiza una copia profunda del nuevo ingrediente y agrégalo a la lista
        IngredientData copiedIngredient = new IngredientData(newIngredient.getIngrediente());
        ingredientesList.add(copiedIngredient);
        Log.d("adaptadorIngredientes", "Ingrediente agregado: " + newIngredient.getIngrediente());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.vista_ingredientes_mostrar, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IngredientData ingrediente = ingredientesList.get(position);
        holder.numero.setText(String.valueOf(position + 1));
        holder.nombre.setText(ingrediente.getIngrediente());

        // Normalizar el nombre del ingrediente
        String ingredienteNormalizado = normalizarTexto(ingrediente.getIngrediente());
        Log.d("adaptadorIngredientes", "Procesando ingrediente: " + ingredienteNormalizado);

        // Determinar si el ingrediente está disponible en productos
        boolean disponible = verificarDisponibilidadProducto(ingredienteNormalizado);

        if (disponible) {
            holder.nombre.setTextColor(Color.GREEN);  // Disponible
            Log.d("adaptadorIngredientes", "Producto encontrado en inventario: " + ingredienteNormalizado);
        } else {
            holder.nombre.setTextColor(Color.RED);  // No disponible
            Log.d("adaptadorIngredientes", "Producto no disponible en inventario: " + ingredienteNormalizado);
        }
    }

    @Override
    public int getItemCount() {
        return ingredientesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre;
        TextView numero;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.vistaIngrediente);
            numero = itemView.findViewById(R.id.numeroingrediente);
        }
    }

    private boolean verificarDisponibilidadProducto(String ingredienteNormalizado) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        int umbralSimilitud = 1;

        // Recorrer todos los productos disponibles para verificar coincidencias
        for (String producto : productosDisponibles.keySet()) {
            String productoNormalizado = normalizarTexto(obtenerPrimeraPalabra(producto));

            // Comparar el ingrediente con el producto disponible
            if (ingredienteNormalizado.equals(productoNormalizado) ||
                    ingredienteNormalizado.contains(productoNormalizado) ||
                    productoNormalizado.contains(ingredienteNormalizado)) {
                Log.d("adaptadorIngredientes", "Coincidencia exacta o parcial encontrada: " + ingredienteNormalizado + " - " + productoNormalizado);
                return true;
            }

            // Comparación difusa con distancia de Levenshtein
            int distancia = levenshteinDistance.apply(ingredienteNormalizado, productoNormalizado);
            if (distancia <= umbralSimilitud) {
                Log.d("adaptadorIngredientes", "Coincidencia difusa encontrada (distancia " + distancia + "): " + ingredienteNormalizado + " - " + productoNormalizado);
                return true;
            }
        }

        Log.d("adaptadorIngredientes", "No se encontraron coincidencias para el ingrediente: " + ingredienteNormalizado);
        return false;
    }

    // Normalizar texto para facilitar la comparación (quita acentos, hace minúsculas, etc.)
    public static String normalizarTexto(String texto) {
        if (texto == null) return "";
        texto = texto.toLowerCase()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("[ñ]", "n")
                .replaceAll("[^a-zA-Z\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
        return texto;
    }

    // Obtener la primera palabra de un texto
    private String obtenerPrimeraPalabra(String texto) {
        String[] palabras = texto.split(" ");
        return palabras[0];  // Retornar solo la primera palabra
    }
}