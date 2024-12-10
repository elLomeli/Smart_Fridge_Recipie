package adaptadores;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipies.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import LISTAS.IngredientData;

public class adaptadorIngredientes extends RecyclerView.Adapter<adaptadorIngredientes.ViewHolder> {

    private List<IngredientData> ingredientDataList;

    public adaptadorIngredientes(List<IngredientData> ingredientDataList) {
        this.ingredientDataList = ingredientDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.vista_ingredientes, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IngredientData ingredientData = ingredientDataList.get(holder.getAdapterPosition());

        // Establecer el texto del EditText con el valor almacenado en el objeto IngredientData
        holder.nombre.setText(ingredientData.getIngrediente());

        // Limpiar cualquier TextWatcher previo para evitar problemas de duplicación
        if (holder.textWatcher != null) {
            holder.nombre.removeTextChangedListener(holder.textWatcher);
        }

        // Agregar un TextWatcher para actualizar el objeto IngredientData cuando cambie el texto en el EditText
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Cuando el texto cambie, actualiza el objeto IngredientData en la lista
                ingredientData.setIngrediente(charSequence.toString()); // Actualiza el texto en el objeto IngredientData
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };

        holder.textWatcher = textWatcher; // Guardar la referencia al TextWatcher para eliminarlo si es necesario
        holder.nombre.addTextChangedListener(textWatcher);

        int numeroIdentificador = holder.getAdapterPosition() + 1;
        holder.numero.setText(String.valueOf(numeroIdentificador));

        holder.borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(holder.getAdapterPosition()); // Eliminar la vista en la posición actual
            }
        });
    }

    @Override
    public int getItemCount() {
        return ingredientDataList.size();
    }

    public void setIngredientDataList(List<IngredientData> ingredientDataList) {
        // Limpia la lista existente y agrega los nuevos datos
        this.ingredientDataList.clear();
        notifyDataSetChanged();
    }

    public void addIngredient(IngredientData newIngredient) {
        // Realiza una copia profunda del nuevo ingrediente y agrégalo a la lista
        IngredientData copiedIngredient = new IngredientData(newIngredient.getIngrediente());
        ingredientDataList.add(copiedIngredient);
        notifyDataSetChanged(); // Notificar al adaptador sobre el cambio
    }


    public void removeItem(int position) {
        ingredientDataList.remove(position); // Eliminar el elemento en la posición actual
        notifyDataSetChanged(); // Notificar al adaptador sobre el cambio
    }
    public List<IngredientData> getIngredientDataList() {
        return ingredientDataList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        EditText nombre;
        TextView numero;
        ImageButton borrar;
        TextWatcher textWatcher; // Almacena el TextWatcher para referencia

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inicialización de vistas
            nombre = itemView.findViewById(R.id.vistaIngrediente);
            numero = itemView.findViewById(R.id.numeroingrediente);
            borrar = itemView.findViewById(R.id.btn_delete_ingrediente);
        }
    }
}
