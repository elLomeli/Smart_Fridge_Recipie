package adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipies.R;

import java.util.List;

import LISTAS.StepData;

public class adaptadorPasosMostrar extends RecyclerView.Adapter<adaptadorPasosMostrar.ViewHolder> {

    Context context;
    List<StepData> pasosList;

    public adaptadorPasosMostrar(Context context, List<StepData> pasosList) {
        this.context = context;
        this.pasosList = pasosList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.vista_steps_mostrar,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StepData paso = pasosList.get(position);
        holder.nombre.setText(paso.getPaso());
        holder.numero.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount() {
        return pasosList.size();
    }
    // Método para actualizar la lista de pasos
    public void addStep(StepData newStep) {
        // Agrega el nuevo paso al final de la lista
        StepData copiedStep = new StepData(newStep.getPaso());
        pasosList.add(copiedStep);
        notifyDataSetChanged(); // Notificar al adaptador sobre el cambio
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre;
        TextView numero;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inicialización de vistas
            nombre = itemView.findViewById(R.id.vistaPaso);
            numero = itemView.findViewById(R.id.numero_de_paso);
        }
    }
}
