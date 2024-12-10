package POJO;

// Clase para representar los productos
public class Product {
    private String nombre;
    private double temperaturaMinima;
    private double temperaturaMaxima;
    private double humedadMinima;
    private double humedadMaxima;
    private double tiempoMinimo;
    private double tiempoMaximo;

    // Getters y Setters

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getTemperaturaMinima() {
        return temperaturaMinima;
    }

    public void setTemperaturaMinima(double temperaturaMinima) {
        this.temperaturaMinima = temperaturaMinima;
    }

    public double getTemperaturaMaxima() {
        return temperaturaMaxima;
    }

    public void setTemperaturaMaxima(double temperaturaMaxima) {
        this.temperaturaMaxima = temperaturaMaxima;
    }

    public double getHumedadMinima() {
        return humedadMinima;
    }

    public void setHumedadMinima(double humedadMinima) {
        this.humedadMinima = humedadMinima;
    }

    public double getHumedadMaxima() {
        return humedadMaxima;
    }

    public void setHumedadMaxima(double humedadMaxima) {
        this.humedadMaxima = humedadMaxima;
    }

    public double getTiempoMinimo() {
        return tiempoMinimo;
    }

    public void setTiempoMinimo(double tiempoMinimo) {
        this.tiempoMinimo = tiempoMinimo;
    }

    public double getTiempoMaximo() {
        return tiempoMaximo;
    }

    public void setTiempoMaximo(double tiempoMaximo) {
        this.tiempoMaximo = tiempoMaximo;
    }
}