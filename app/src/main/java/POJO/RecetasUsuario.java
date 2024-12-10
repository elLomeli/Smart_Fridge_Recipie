package POJO;

import java.util.ArrayList;
import java.util.List;

public class RecetasUsuario {
    private String titulo;
    private String imagenUrl;
    private String id;
    private String numero_Personas;
    private String vegetariana;
    private String horario;
    private String calorias;
    private List<String> ingredientes; // Lista de ingredientes
    private List<String> pasos; // Lista de pasos

    public RecetasUsuario() {
        // Constructor vacío requerido para Firestore
    }

    public RecetasUsuario(String titulo, String imagenUrl, String id, String numero_Personas, String vegetariana, String horario, String calorias, List<String> ingredientes, List<String> pasos) {
        this.titulo = titulo;
        this.imagenUrl = imagenUrl;
        this.id = id;
        this.numero_Personas = numero_Personas;
        this.vegetariana = vegetariana;
        this.horario = horario;
        this.calorias = calorias;
        this.ingredientes = ingredientes != null ? ingredientes : new ArrayList<>();
        this.pasos = pasos != null ? pasos : new ArrayList<>();
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
}
