package com.example.recipies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import adaptadores_Calendario.CalendarAdapter;

public class Calendario extends AppCompatActivity implements CalendarAdapter.OnItemListener{

    private TextView mes;
    private RecyclerView dias;
    private LocalDate seleccionarFecha;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);
        dias = findViewById(R.id.calendarioRV);
        mes =findViewById(R.id.mesDelAño);
        seleccionarFecha = LocalDate.now();
        ponerMes();
    }

    private void ponerMes() {
        mes.setText(meses(seleccionarFecha));
        ArrayList<String> diasEnElMes = diasArreglo(seleccionarFecha);

        CalendarAdapter calendarAdapter = new CalendarAdapter(diasEnElMes,this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),7);
        dias.setLayoutManager(layoutManager);
        dias.setAdapter(calendarAdapter);

    }

    private ArrayList<String> diasArreglo(LocalDate date) {
        ArrayList<String> diasEnElMesArreglo = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);
        int diasEnELMes = yearMonth.lengthOfMonth();
        LocalDate primeroDelMes = seleccionarFecha.withDayOfMonth(1);

        int diasSemana = primeroDelMes.getDayOfWeek().getValue();
        for(int i = 1 ; i <= 42 ; i++)
        {
            if(i <= diasSemana || i > diasEnELMes + diasSemana)
            {
                diasEnElMesArreglo.add("");
            }
            else
            {
                diasEnElMesArreglo.add(String.valueOf(i - diasSemana));
            }
        }
        return diasEnElMesArreglo;
    }

    private String meses(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }
    public void mesAnterior(View view) {
        seleccionarFecha = seleccionarFecha.minusMonths(1);
        ponerMes();
    }

    public void mesSiguiente(View view) {
        seleccionarFecha = seleccionarFecha.plusMonths(1);
        ponerMes();
    }

    @Override
    public void onItemCLick(int position, String dayText) {
        if(dayText.equals(""))
        {
            String message = "Seleccionaste la Fecha" + dayText + " " + meses(seleccionarFecha);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}