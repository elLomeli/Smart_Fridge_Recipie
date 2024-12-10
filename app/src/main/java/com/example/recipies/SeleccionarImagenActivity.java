package com.example.recipies;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SeleccionarImagenActivity extends AppCompatActivity {
    private GridView gridView;
    private final int[] imagenes = {
            R.drawable.imagen01,
            R.drawable.imagen02,
            R.drawable.imagen03,
            R.drawable.imagen04,
            R.drawable.imagen05,
            R.drawable.imagen06,
            R.drawable.imagen07,
            R.drawable.imagen08,
            R.drawable.imagen09,
            R.drawable.imagen10,
            R.drawable.imagen11,
            R.drawable.imagen12,
            R.drawable.imagen13,
            R.drawable.imagen14,
            R.drawable.imagen15,
            R.drawable.imagen16,
            R.drawable.imagen17,
            R.drawable.imagen18,
            R.drawable.imagen19,
            R.drawable.imagen20,
            R.drawable.imagen21,
            R.drawable.imagen22,
            R.drawable.imagen23,
            R.drawable.imagen24,
            R.drawable.imagen25,
            R.drawable.imagen26,
            R.drawable.imagen27,
            R.drawable.imagen28,
            R.drawable.imagen29,
            R.drawable.imagen30,
            // Agrega aquí más imágenes
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_imagen);

        gridView = findViewById(R.id.gridImagenes);

        ImagenAdapter adapter = new ImagenAdapter();
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Devuelve la imagen seleccionada al formulario principal
                setResult(RESULT_OK, getIntent().putExtra("imagenSeleccionada", imagenes[position]));
                finish();
            }
        });
    }

    private class ImagenAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return imagenes.length;
        }

        @Override
        public Object getItem(int position) {
            return imagenes[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(SeleccionarImagenActivity.this);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(300, 300)); // Ajusta el tamaño según tus necesidades
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(imagenes[position]);
            return imageView;
        }
    }
}