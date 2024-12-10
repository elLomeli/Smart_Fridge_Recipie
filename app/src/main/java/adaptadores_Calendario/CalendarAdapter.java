package adaptadores_Calendario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipies.R;

import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {

    private final ArrayList<String> diasDelMes;
    private final OnItemListener onItemListener;
     public CalendarAdapter(ArrayList<String> diasDelMes, OnItemListener onItemListener)
     {
         this.diasDelMes = diasDelMes;
         this.onItemListener = onItemListener;
     }
    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendario_cell, parent ,false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
    holder.diadelMes.setText(diasDelMes.get(position));

    }

    @Override
    public int getItemCount() {
        return diasDelMes.size();
    }

    public interface OnItemListener
    {
        void onItemCLick(int position, String dayText);
    }
}
