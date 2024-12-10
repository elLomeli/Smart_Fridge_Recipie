package com.example.recipies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import io.reactivex.rxjava3.annotations.Nullable;

public class PlanAlimenticio extends AppCompatActivity {
    private int perfilActual;
    private FloatingActionButton agregarPerfilButton, editarPerfilButton,borrarPerfiles,cancelar;
    private LinearLayout perfil1, perfil2, perfil3, perfil4;
    private TextView textoPerfil1, textoPerfil2, textoPerfil3, textoPerfil4;
    private ImageView imagen1, imagen2, imagen3, imagen4;
    private ImageView editar1,editar2,editar3,editar4;
    private SharedPreferences sharedPreferences,sharedPrefs;
    private CheckBox ck1, ck2, ck3, ck4;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String email;
    private static final int FORMULARIO_REQUEST_CODE = 1; // Puedes elegir cualquier número
    private boolean modoEdicion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_alimenticio);

        // Evitar que la pantalla se apague
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Obtener referencias a los elementos del layout
        agregarPerfilButton = findViewById(R.id.agregarPerfil);
        editarPerfilButton = findViewById(R.id.EditarPerfiles);
        borrarPerfiles = findViewById(R.id.borrarPerfil);
        cancelar = findViewById(R.id.cancelar);
        perfil1 = findViewById(R.id.perfil1);
        perfil2 = findViewById(R.id.perfil2);
        perfil3 = findViewById(R.id.perfil3);
        perfil4 = findViewById(R.id.perfil4);
        textoPerfil1 = findViewById(R.id.textoPerfil1);
        textoPerfil2 = findViewById(R.id.textoPerfil2);
        textoPerfil3 = findViewById(R.id.textoPerfil3);
        textoPerfil4 = findViewById(R.id.textoPerfil4);
        imagen1 = findViewById(R.id.imagen1);
        imagen2 = findViewById(R.id.imagen2);
        imagen3 = findViewById(R.id.imagen3);
        imagen4 = findViewById(R.id.imagen4);
        editar1 = findViewById(R.id.editar1);
        editar2 = findViewById(R.id.editar2);
        editar3 = findViewById(R.id.editar3);
        editar4 = findViewById(R.id.editar4);
        ck1 = findViewById(R.id.check1);
        ck2 = findViewById(R.id.check2);
        ck3 = findViewById(R.id.check3);
        ck4 = findViewById(R.id.check4);

        // Inicializar Firestore y Firebase Storage
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        sharedPreferences = getSharedPreferences("perfiles", MODE_PRIVATE);
        sharedPrefs = getSharedPreferences("correo", Context.MODE_PRIVATE);
        email = sharedPrefs.getString("email", null);

        // Restaurar el estado de los perfiles
        perfil1.setVisibility(sharedPreferences.getInt("perfil1Visibility", View.GONE));
        perfil2.setVisibility(sharedPreferences.getInt("perfil2Visibility", View.GONE));
        perfil3.setVisibility(sharedPreferences.getInt("perfil3Visibility", View.GONE));
        perfil4.setVisibility(sharedPreferences.getInt("perfil4Visibility", View.GONE));

        // Restaurar el valor de perfilActual
        perfilActual = sharedPreferences.getInt("perfilActual", 1);

        // Configurar el botón para agregar perfiles
        agregarPerfilButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Buscar el próximo perfil disponible
                int perfilDisponible = buscarProximoPerfilDisponible();

                // Abrir el formulario con el próximo perfil disponible
                abrirFormulario(String.valueOf(perfilDisponible), obtenerLayoutPerfil(perfilDisponible));
            }
        });

        // Configurar clic en el botón "Editar Perfil"
        editarPerfilButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modoEdicion = !modoEdicion; // Cambia el estado del modo de edición
                configurarVisibilidadCheckboxes();
                borrarPerfiles.setVisibility(modoEdicion ? View.VISIBLE : View.GONE);
                cancelar.setVisibility(modoEdicion ? View.VISIBLE : View.GONE);

                // Desactiva o activa los clics en perfiles según el modo de edición
                if (modoEdicion) {
                    desactivarClicsEnPerfiles();
                } else {
                    activarClicsEnPerfiles();
                }
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                borrarPerfiles.setVisibility(View.GONE);
                cancelar.setVisibility(View.GONE);
                ck1.setVisibility(View.GONE);
                ck2.setVisibility(View.GONE);
                ck3.setVisibility(View.GONE);
                ck4.setVisibility(View.GONE);
                editar1.setVisibility(View.GONE);
                editar2.setVisibility(View.GONE);
                editar3.setVisibility(View.GONE);
                editar4.setVisibility(View.GONE);

                // Restaura el estado de los clics en perfiles al modo normal
                activarClicsEnPerfiles();
            }
        });

        borrarPerfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlanAlimenticio.this);
                builder.setTitle("Confirmar eliminación");
                builder.setMessage("¿Estás seguro de que deseas eliminar los perfiles seleccionados?");
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Realizar la eliminación de perfiles
                        borrarPerfilesSeleccionados();
                        // Restaura el estado de los clics en perfiles al modo normal
                        activarClicsEnPerfiles();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancelar la acción
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });


        // Configurar clics en botones de edición
        editar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirFormulario("1");
            }
        });
        editar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirFormulario("2");
            }
        });
        editar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirFormulario("3");
            }
        });
        editar4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirFormulario("4");
            }
        });
        activarClicsEnPerfiles();
    }
    private void activarClicsEnPerfiles() {
        perfil1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirRecetas("1");
            }
        });
        perfil2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirRecetas("2");
            }
        });
        perfil3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirRecetas("3");
            }
        });
        perfil4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirRecetas("4");
            }
        });

        quitareditar();
    }

    private void desactivarClicsEnPerfiles() {
        perfil1.setOnClickListener(null);
        perfil2.setOnClickListener(null);
        perfil3.setOnClickListener(null);
        perfil4.setOnClickListener(null);
        modoEdicion = !modoEdicion;
    }
    private int buscarProximoPerfilDisponible() {
        if (perfil1.getVisibility() == View.GONE) {
            return 1;
        } else if (perfil2.getVisibility() == View.GONE) {
            return 2;
        } else if (perfil3.getVisibility() == View.GONE) {
            return 3;
        } else if (perfil4.getVisibility() == View.GONE) {
            return 4;
        }

        // Si todos los perfiles están visibles, regresa el siguiente después del perfil actual
        return (perfilActual % 4) + 1;
    }

    private LinearLayout obtenerLayoutPerfil(int numeroPerfil) {
        switch (numeroPerfil) {
            case 1:
                return perfil1;
            case 2:
                return perfil2;
            case 3:
                return perfil3;
            case 4:
                return perfil4;
            default:
                return perfil1; // Devuelve el perfil1 por defecto si algo sale mal
        }
    }
    void quitareditar(){
        borrarPerfiles.setVisibility(View.GONE);
        cancelar.setVisibility(View.GONE);
        ck1.setVisibility(View.GONE);
        ck2.setVisibility(View.GONE);
        ck3.setVisibility(View.GONE);
        ck4.setVisibility(View.GONE);
        editar1.setVisibility(View.GONE);
        editar2.setVisibility(View.GONE);
        editar3.setVisibility(View.GONE);
        editar4.setVisibility(View.GONE);
        // Desseleccionar los checkboxes
        ck1.setChecked(false);
        ck2.setChecked(false);
        ck3.setChecked(false);
        ck4.setChecked(false);
    }
    private void abrirFormulario(String perfil) {
        Intent intent = new Intent(PlanAlimenticio.this, FormularioPerfiles.class);
        intent.putExtra("perfil", perfil);
        intent.putExtra("modificar",true);
        startActivityForResult(intent, FORMULARIO_REQUEST_CODE);
        // No necesitas llamar cargarDatosEnInterfaz aquí
        quitareditar();
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Cargar datos de perfiles al reanudar la actividad
        cargarDatosPerfil("1");
        cargarDatosPerfil("2");
        cargarDatosPerfil("3");
        cargarDatosPerfil("4");

        // Configurar la visibilidad del botón "Agregar Perfil"
        if (perfil1.getVisibility() == View.VISIBLE &&
                perfil2.getVisibility() == View.VISIBLE &&
                perfil3.getVisibility() == View.VISIBLE &&
                perfil4.getVisibility() == View.VISIBLE) {
            // Si los cuatro perfiles están visibles, ocultar el botón "Agregar Perfiles"
            agregarPerfilButton.setVisibility(View.GONE);
        } else {
            // Si hay al menos un perfil que no está visible, mostrar el botón "Agregar Perfiles"
            agregarPerfilButton.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        // Guardar el estado de los perfiles en SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("perfil1Visibility", perfil1.getVisibility());
        editor.putInt("perfil2Visibility", perfil2.getVisibility());
        editor.putInt("perfil3Visibility", perfil3.getVisibility());
        editor.putInt("perfil4Visibility", perfil4.getVisibility());

        // Guardar el valor de perfilActual
        editor.putInt("perfilActual", perfilActual);
        editor.apply();
    }

    private void abrirFormulario(String perfil, LinearLayout layoutPerfil) {
        Intent intent = new Intent(PlanAlimenticio.this, FormularioPerfiles.class);
        intent.putExtra("perfil", perfil);
        intent.putExtra("modificar",false);
        startActivityForResult(intent, FORMULARIO_REQUEST_CODE);
        // No necesitas llamar cargarDatosEnInterfaz aquí
        layoutPerfil.setVisibility(View.VISIBLE);
        perfilActual++;

        borrarPerfiles.setVisibility(View.GONE);
        cancelar.setVisibility(View.GONE);
        ck1.setVisibility(View.GONE);
        ck2.setVisibility(View.GONE);
        ck3.setVisibility(View.GONE);
        ck4.setVisibility(View.GONE);
        editar1.setVisibility(View.GONE);
        editar2.setVisibility(View.GONE);
        editar3.setVisibility(View.GONE);
        editar4.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FORMULARIO_REQUEST_CODE && resultCode == RESULT_OK) {
            // Obtiene el perfil actual del intent de retorno
            String perfil = data.getStringExtra("perfil");
            // Carga los datos en la interfaz
        }
        activarClicsEnPerfiles();
    }

    private void abrirRecetas(String perfil) {
        Intent intent = new Intent(PlanAlimenticio.this, Viewpager_PlanHorarios.class);
        intent.putExtra("perfil", perfil);
        startActivity(intent);
    }

    private void configurarVisibilidadCheckboxes() {
        // Configurar la visibilidad de los checkboxes solo para los perfiles mostrados
        ck1.setVisibility(perfil1.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
        ck2.setVisibility(perfil2.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
        ck3.setVisibility(perfil3.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
        ck4.setVisibility(perfil4.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
        editar1.setVisibility(perfil1.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
        editar2.setVisibility(perfil2.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
        editar3.setVisibility(perfil3.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
        editar4.setVisibility(perfil4.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
    }

    private void cargarDatosPerfil(String perfil) {
        // Obtener la referencia al documento en Firestore
        DocumentReference docRef = db.collection(email).document("perfil " + perfil);

        // Obtener los datos del documento
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Obtener los datos del documento
                    String apodo = documentSnapshot.getString("apodo");
                    String urlImagen = documentSnapshot.getString("url_imagen");

                    // Puedes obtener otros datos de la misma manera
                    // Asignar los datos a los TextViews correspondientes
                    asignarDatosAPerfil(Integer.parseInt(perfil), apodo);

                    // Cargar la imagen en el ImageView correspondiente
                    cargarImagenEnInterfaz(Integer.parseInt(perfil), urlImagen);
                } else {
                    // El documento no existe
                    Log.d("TAG", "El documento no existe");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Manejar errores
                Log.d("TAG", "Error al obtener el documento", e);
            }
        });
    }
    private void asignarDatosAPerfil(int numeroPerfil, String apodo) {
        switch (numeroPerfil) {
            case 1:
                textoPerfil1.setText(apodo);
                break;
            case 2:
                textoPerfil2.setText(apodo);
                break;
            case 3:
                textoPerfil3.setText(apodo);
                break;
            case 4:
                textoPerfil4.setText(apodo);
                break;
        }
    }

    private void cargarImagenEnInterfaz(int numeroPerfil, String urlImagen) {
        // Puedes usar la biblioteca que prefieras para cargar imágenes desde URL
        // Aquí un ejemplo utilizando Glide
        switch (numeroPerfil) {
            case 1:
                cargarImagenConGlide(urlImagen, imagen1);
                break;
            case 2:
                cargarImagenConGlide(urlImagen, imagen2);
                break;
            case 3:
                cargarImagenConGlide(urlImagen, imagen3);
                break;
            case 4:
                cargarImagenConGlide(urlImagen, imagen4);
                break;
        }
    }

    private void cargarImagenConGlide(String urlImagen, ImageView imageView) {
        Glide.with(this)
                .load(urlImagen)
                .into(imageView);
    }
    private void borrarPerfilesSeleccionados() {
        // Verificar si se seleccionó algún perfil para borrar
        if (ck1.isChecked()) {
            perfil1.setVisibility(View.GONE);
            ck1.setVisibility(View.GONE);
            editar1.setVisibility(View.GONE);
            borrarPerfilConImagen("1");
        }
        if (ck2.isChecked()) {
            perfil2.setVisibility(View.GONE);
            ck2.setVisibility(View.GONE);
            editar2.setVisibility(View.GONE);
            borrarPerfilConImagen("2");
        }
        if (ck3.isChecked()) {
            perfil3.setVisibility(View.GONE);
            ck3.setVisibility(View.GONE);
            editar3.setVisibility(View.GONE);
            borrarPerfilConImagen("3");
        }
        if (ck4.isChecked()) {
            perfil4.setVisibility(View.GONE);
            ck4.setVisibility(View.GONE);
            editar4.setVisibility(View.GONE);
            borrarPerfilConImagen("4");
        }
        // Mostrar nuevamente el botón "Agregar Perfil"
        agregarPerfilButton.setVisibility(View.VISIBLE);
        quitareditar();
    }



    private void borrarPerfilConImagen(String perfil) {
        // Obtener la referencia al documento en Firestore
        DocumentReference docRef = db.collection(email).document("perfil " + perfil);

        // Obtener los datos del documento para obtener la URL de la imagen
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Obtener la URL de la imagen
                    String urlImagen = documentSnapshot.getString("url_imagen");

                    // Llamar a la función para borrar el documento y la imagen en Firestore
                    borrarPerfilEnFirestoreYAlmacenamiento(perfil, urlImagen);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Manejar errores
                Log.d("TAG", "Error al obtener el documento", e);
            }
        });
    }

    private void borrarPerfilEnFirestoreYAlmacenamiento(String perfil, String urlImagen) {
        // Obtener la referencia al documento en Firestore
        DocumentReference docRef = db.collection(email).document("perfil " + perfil);

        // Borrar el documento en Firestore
        docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Documento borrado exitosamente
                Log.d("TAG", "Documento borrado: perfil " + perfil);

                // Borrar la imagen del almacenamiento de Firebase
                String imageName = getImageNameFromUrl(urlImagen);
                if (imageName != null) {
                    StorageReference storageRef = storage.getReference().child("logos/" + imageName);
                    storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Imagen en el almacenamiento borrada exitosamente
                            Log.d("TAG", "Imagen en el almacenamiento borrada: " + imageName);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Manejar errores
                            Log.e("TAG", "Error al borrar la imagen en el almacenamiento", e);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Manejar errores
                Log.e("TAG", "Error al borrar el documento", e);
            }
        });

        borrarCalificacionesDePerfil(perfil);
        borrarCaloriasPerfil(perfil);
        borrarBotonEmpezarDesayunoDePerfil(perfil);
        borrarBotonEmpezarComidaDePerfil(perfil);
        borrarBotonEmpezarCenaDePerfil(perfil);
        borrarSpinnerPerfil(perfil);
    }



    // Método para borrar la calorias del SharedPreferences
    private void borrarCaloriasPerfil(String perfilId) {
        SharedPreferences sharedPreferences = getSharedPreferences("CaloriasRestantes" + perfilId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Esto eliminará todas las calificaciones asociadas a este perfil
        editor.apply();
    }

    // Método para borrar la calificación del SharedPreferences
    public void borrarCalificacionesDePerfil(String perfilId) {
        SharedPreferences sharedPreferences = getSharedPreferences("CalificacionesPlan_" + perfilId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Esto eliminará todas las calificaciones asociadas a este perfil
        editor.apply();
    }

    public void borrarBotonEmpezarDesayunoDePerfil(String perfilId) {
        SharedPreferences prefs = getSharedPreferences("EmpezarRecetaDesayuno" + perfilId, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public void borrarBotonEmpezarComidaDePerfil(String perfilId) {
        SharedPreferences prefs = getSharedPreferences("EmpezarRecetaComida" + perfilId, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public void borrarBotonEmpezarCenaDePerfil(String perfilId) {
        SharedPreferences prefs = getSharedPreferences("EmpezarRecetaCena" + perfilId, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
    private void borrarSpinnerPerfil(String perfilId) {
        SharedPreferences prefs = getSharedPreferences("spinner" + perfilId,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
    private String getImageNameFromUrl(String imageUrl) {
        int startIndex = imageUrl.indexOf("logos%2F") + 8;
        int endIndex = imageUrl.indexOf("?alt");

        if (startIndex >= 0 && endIndex >= 0) {
            return imageUrl.substring(startIndex, endIndex);
        }
        return null;
    }
}
