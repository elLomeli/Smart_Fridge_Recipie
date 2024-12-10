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

import LISTAS.StepData;

public class adaptadorPasos extends RecyclerView.Adapter<adaptadorPasos.ViewHolder> {

    private List<StepData> stepDataList;

    public adaptadorPasos(List<StepData> stepDataList) {
        this.stepDataList = stepDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.vista_steps, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StepData stepData = stepDataList.get(holder.getAdapterPosition());

        // Establecer el texto del EditText con el valor almacenado en el objeto StepData
        holder.nombre.setText(stepData.getPaso());

        // Limpiar cualquier TextWatcher previo para evitar problemas de duplicación
        if (holder.textWatcher != null) {
            holder.nombre.removeTextChangedListener(holder.textWatcher);
        }

        // Agregar un TextWatcher para actualizar el objeto StepData cuando cambie el texto en el EditText
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Cuando el texto cambie, actualiza el objeto StepData en la lista
                stepData.setPaso(charSequence.toString()); // Actualiza el texto en el objeto StepData
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };

        holder.textWatcher = textWatcher; // Guardar la referencia al TextWatcher para referencia
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
        return stepDataList.size();
    }

    public void setStepDataList(List<StepData> stepDataList) {
        // Limpia la lista existente y agrega los nuevos datos
        this.stepDataList.clear();
        notifyDataSetChanged();
    }

    public void addStep(StepData newStep) {
        // Agrega el nuevo paso al final de la lista
        StepData copiedStep = new StepData(newStep.getPaso());
        stepDataList.add(copiedStep);
        notifyDataSetChanged(); // Notificar al adaptador sobre el cambio
    }


    public void removeItem(int position) {
        stepDataList.remove(position); // Eliminar el elemento en la posición actual
        notifyDataSetChanged(); // Notificar al adaptador sobre el cambio
    }


    public List<StepData> getStepDataList() {
        return stepDataList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        EditText nombre;
        TextView numero;
        ImageButton borrar;
        TextWatcher textWatcher; // Almacena el TextWatcher para referencia

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inicialización de vistas
            nombre = itemView.findViewById(R.id.vistaPaso);
            numero = itemView.findViewById(R.id.numero_de_paso);
            borrar = itemView.findViewById(R.id.btn_delete_Step);
        }
    }
}
