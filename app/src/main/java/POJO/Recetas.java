package POJO;

import java.util.List;



public class Recetas {
    public String titulo;
    public String imagenUrl;

    public Recetas() {}

    public Recetas(String titulo,String imagenUrl) {
        this.titulo = titulo;
        this.imagenUrl = imagenUrl;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public String getImagenUrl() {
        return imagenUrl;
    }


}

