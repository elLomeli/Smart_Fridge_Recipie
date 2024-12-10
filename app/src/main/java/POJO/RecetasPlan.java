package POJO;

import java.util.List;

public class RecetasPlan {
    public String titulo;
    public String imagenUrl;

    public String id;
    public String numero_Personas;
    public String vegetariana;
    public String horario;
    public String calorias;
    private int caloriasNumerico;
    public List<String> ingredientes; // Cambiado a List<String>
    public List<String> pasos;
    public RecetasPlan() {}

    public RecetasPlan(String titulo, String imagenUrl, String id, String numero_Personas, String vegetariana, String horario, String calorias, List<String> ingredientes, List<String> pasos) {
        this.titulo = titulo;
        this.imagenUrl = imagenUrl;
        this.id = id;
        this.numero_Personas = numero_Personas;
        this.vegetariana = vegetariana;
        this.horario = horario;
        this.calorias = calorias;
        this.ingredientes = ingredientes;
        this.pasos = pasos;
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

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumero_Personas() {
        return numero_Personas;
    }

    public void setNumero_Personas(String numero_Personas) {
        this.numero_Personas = numero_Personas;
    }

    public String getVegetariana() {
        return vegetariana;
    }

    public void setVegetariana(String vegetariana) {
        this.vegetariana = vegetariana;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getCalorias() {
        return calorias;
    }

    public void setCalorias(String calorias) {
        this.calorias = calorias;
    }

    public List<String> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<String> ingredientes) {
        this.ingredientes = ingredientes;
    }

    public List<String> getPasos() {
        return pasos;
    }

    public void setPasos(List<String> pasos) {
        this.pasos = pasos;
    }

    public int getCaloriasNumerico() {
        return caloriasNumerico;
    }

    // Método setter para establecer la versión numérica de calorias
    public void setCaloriasNumerico(int caloriasNumerico) {
        this.caloriasNumerico = caloriasNumerico;
    }
}

