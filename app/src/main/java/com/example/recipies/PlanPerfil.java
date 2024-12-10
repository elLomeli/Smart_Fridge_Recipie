package com.example.recipies;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.annotations.Nullable;

public class PlanPerfil extends Fragment {

    private String id,email;
    private FirebaseFirestore db;
    private TextView Nombre,Edad,Estatura,Peso,IMC,tmb,rest;
    private String nombre ;
    private String edad ;
    private String estatura;
    private String peso ;
    private String sexo ;
    private ArrayAdapter<CharSequence> PESO;
    private Spinner metas;
    private String textoSeleccionado;
    private ImageView aceptable;
    private PlanPerfilViewModel viewModel;
    private Button guardarMeta;
    private TextView aceptabletxt;

    public PlanPerfil() {}


    private static class PlanSeleccionado {
        String textoSeleccionado;
        long fechaSeleccionadaMillis;

        public PlanSeleccionado(String textoSeleccionado, long fechaSeleccionadaMillis) {
            this.textoSeleccionado = textoSeleccionado;
            this.fechaSeleccionadaMillis = fechaSeleccionadaMillis;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getString("perfil");
        }
        // Inicializar el ViewModel
        viewModel = new ViewModelProvider(this).get(PlanPerfilViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan_perfil, container, false);

        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPrefs = requireActivity().getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);

        Nombre =view.findViewById(R.id.textoPerfilNombre);
        Edad = view.findViewById(R.id.textoPerfilEdad);
        Estatura = view.findViewById(R.id.textoPerfilEstatura);
        Peso = view.findViewById(R.id.textoPerfilPeso);
        IMC = view.findViewById(R.id.textoPerfilIMC);
        metas = view.findViewById(R.id.pesoSpinner);
        aceptable = view.findViewById(R.id.aceptableimc);
        aceptabletxt = view.findViewById(R.id.aceptableimctxt);
        tmb = view.findViewById(R.id.textoPerfilTMB);
        rest = view.findViewById(R.id.textoPerfilCaloriasFaltantes);
        guardarMeta = view.findViewById(R.id.guardarMeta);


        //spinner
        PESO = ArrayAdapter.createFromResource(getContext(),R.array.Plan, android.R.layout.simple_spinner_item);
        PESO.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        metas.setAdapter(PESO);
        metas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Obtener el texto seleccionado
                textoSeleccionado = parentView.getItemAtPosition(position).toString();

                cargarDatosDesdeFirestore();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Este método se llama cuando no hay ningún elemento seleccionado.
            }
        });

        guardarMeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Confirmar");
                builder.setMessage("¿Está seguro de que desea guardar la meta?");
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Acciones a realizar si el usuario confirma (Sí)
                        PlanSeleccionado planSeleccionado = new PlanSeleccionado(textoSeleccionado, System.currentTimeMillis());
                        guardarSeleccionEnPrefs(planSeleccionado);
                        metas.setEnabled(false);
                        guardarMeta.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Meta guardada correctamente", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Acciones a realizar si el usuario cancela (No)
                        dialog.dismiss(); // Cerrar el diálogo
                    }
                });
                builder.create().show(); // Mostrar el AlertDialog
            }
        });

        return view;
    }

    private void guardarSeleccionEnPrefs(PlanSeleccionado planSeleccionado) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("spinner" + id,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("textoSeleccionado", planSeleccionado.textoSeleccionado);
        editor.putLong("fechaSeleccionada", planSeleccionado.fechaSeleccionadaMillis);
        editor.apply();
    }


    private PlanSeleccionado obtenerSeleccionDesdePrefs() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("spinner" + id,Context.MODE_PRIVATE);
        String textoSeleccionado = prefs.getString("textoSeleccionado", null);
        long fechaSeleccionadaMillis = prefs.getLong("fechaSeleccionada", 0); // 0 es el valor predeterminado si no se ha guardado ninguna fecha
        return new PlanSeleccionado(textoSeleccionado, fechaSeleccionadaMillis);
    }


    @Override
    public void onStart() {
        super.onStart();

        // Obtener la selección almacenada
        PlanSeleccionado planSeleccionado = obtenerSeleccionDesdePrefs();

        // Establecer el Spinner con la selección almacenada
        if (planSeleccionado.textoSeleccionado != null) {
            int posicionSeleccionada = PESO.getPosition(planSeleccionado.textoSeleccionado);
            metas.setSelection(posicionSeleccionada);
        } else {
            metas.setEnabled(true);
        }

        // Verificar si ha pasado una semana desde la última selección
        if (haPasadoUnaSemana(planSeleccionado.fechaSeleccionadaMillis)) {
            // Habilitar el Spinner
            metas.setEnabled(true);
            guardarMeta.setVisibility(View.VISIBLE);
        } else {
            // Deshabilitar el Spinner
            metas.setEnabled(false);
            guardarMeta.setVisibility(View.GONE);
        }

        cargarDatosDesdeFirestore();
    }

    private boolean haPasadoUnaSemana(long fechaSeleccionadaMillis) {
        if (fechaSeleccionadaMillis == 0) {
            return true; // Si no hay fecha de selección, asumimos que ha pasado una semana
        }

        long unaSemanaMillis = 7 * 24 * 60 * 60 * 1000; // 7 días en milisegundos
        long tiempoActual = System.currentTimeMillis();
        return (tiempoActual - fechaSeleccionadaMillis) > unaSemanaMillis;
    }

    public static PlanPerfil newInstance(String id) {
        PlanPerfil fragment = new PlanPerfil();
        Bundle args = new Bundle();
        args.putString("perfil", id);
        fragment.setArguments(args);
        return fragment;
    }

    private void cargarDatosDesdeFirestore() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("CaloriasRestantes" + id,Context.MODE_PRIVATE);
        float caloriasDesayuno = prefs.getFloat("caloriasDesayuno", 0.0f);
        float caloriasComida = prefs.getFloat("caloriasComida", 0.0f);
        float caloriasCena = prefs.getFloat("caloriasCena", 0.0f);
        DocumentReference docRef = db.collection(email).document("perfil " + id);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Obtener datos del documento
                nombre = documentSnapshot.getString("nombre");
                edad = documentSnapshot.getString("edad");
                estatura = documentSnapshot.getString("estatura");
                peso = documentSnapshot.getString("peso");
                sexo = documentSnapshot.getString("sexo");

                // Asignar datos a los EditText
                Nombre.setText(nombre);
                Edad.setText(edad);
                Estatura.setText(estatura);
                Peso.setText(peso);
                try {
                    // Convertir el valor de String a int
                    int KG = Integer.parseInt(peso);
                    int CM = Integer.parseInt(estatura);
                    int AÑOS = Integer.parseInt(edad);
                    double EJERCICIO = 1.2;

                    // Calcular el IMC (Índice de Masa Corporal)
                    float estaturaMetros = (float) (CM / 100.0);
                    float imc = KG / (estaturaMetros * estaturaMetros);

                    // Establecer la imagen y el color según el IMC
                    establecerImagenColorIMC(imc);

                    // Formatear el IMC con un decimal
                    String imcFormateado = String.format("%.1f", imc);
                    IMC.setText(imcFormateado);



                    // Calcular la TMB y las calorías
                    double TMB = calcularTMB(sexo, KG, CM, AÑOS);
                    float calorias = calcularCalorias(TMB, EJERCICIO, textoSeleccionado);
                    float caloriasrestantes = calorias - caloriasDesayuno - caloriasComida - caloriasCena;

                    // Guardar valor
                    SharedPreferences prefsUs = requireActivity().getSharedPreferences("CaloriasUsuario" + id,Context. MODE_PRIVATE);
                    SharedPreferences.Editor editorUs = prefsUs.edit();
                    editorUs.putFloat("calorias", calorias);
                    editorUs.apply();

                    String caloriasFormateado = String.format("%.0f", calorias);

                    // Mostrar las calorías en el TextView
                    tmb.setText(caloriasFormateado);
                    // Actualizar el valor en el ViewModel
                    viewModel.setCaloriasRestantes(caloriasrestantes);
                } catch (NumberFormatException e) {
                    // Manejar la excepción si el valor no es un número entero válido
                    System.err.println("Error al convertir peso o estatura a entero: " + e.getMessage());
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error al obtener datos de Firestore", e);
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observar cambios en el valor de caloriasRestantes y actualizar rest automáticamente
        viewModel.getCaloriasRestantesLiveData().observe(getViewLifecycleOwner(), caloriasRestantes -> {
            String caloriasRestantesFormateado = String.format("%.0f", caloriasRestantes);
            rest.setText(caloriasRestantesFormateado);
        });
    }

    private void establecerImagenColorIMC(float imc) {
        if (imc <= 19) {
            aceptable.setImageResource(R.drawable.cancel);
            aceptable.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.Caliente));
            aceptabletxt.setText("Estas por debajo de tu peso");
        } else if (imc >= 20 && imc <= 24) {
            aceptable.setImageResource(R.drawable.aceptar);
            aceptable.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.Perfecto));
            aceptabletxt.setText("Estas dentro de tu peso");
        } else if (imc >= 25 && imc < 30) {
            aceptable.setImageResource(R.drawable.cancel);
            aceptable.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.Aceptable));
            aceptabletxt.setText("Estas un poco arriba de tu peso ideal");
        } else {
            aceptable.setImageResource(R.drawable.cancel);
            aceptable.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.Caliente));
            aceptabletxt.setText("Estas muy por encima de tu peso");
        }
    }

    private double calcularTMB(String sexo, int KG, int CM, int AÑOS) {
        if (AÑOS >= 13 && AÑOS < 19) {
            return sexo.equals("HOMBRE") ? 88.362 + (13.397 * KG) + (4.799 * CM) - (5.677 * AÑOS) : 447.593 + (9.247 * KG) + (3.098 * CM) - (4.330 * AÑOS);
        } else if (AÑOS >= 19 && AÑOS < 60) {
            return sexo.equals("HOMBRE") ? (10 * KG) + (6.25 * CM) - (5 * AÑOS) + 5 : (10 * KG) + (6.25 * CM) - (5 * AÑOS) - 161;
        } else {
            tmb.setText("El rango de edad ya no es óptimo para dar asistencia en nutrición");
            return 0.0;
        }
    }
    private float calcularCalorias(double tmb, double ejercicio, String textoSeleccionado) {
        if (textoSeleccionado.equals("Mantener Peso")) {
            return (float) (tmb * ejercicio);
        } else if (textoSeleccionado.equals("Subir Peso")) {
            return (float) (tmb * ejercicio * 1.15);
        } else if (textoSeleccionado.equals("Bajar Peso")) {
            return (float) (tmb * ejercicio * .8496);
        } else {
            return 0.0f; // Manejar un caso por defecto o mostrar un mensaje de error según sea necesario
        }
    }
}