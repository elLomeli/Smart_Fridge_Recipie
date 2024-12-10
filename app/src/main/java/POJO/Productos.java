package POJO;

public class Productos {
    String Nombre;
    String Marca;
    String CodigoBarras;
    String Cantidad;
    String Piezas;
    String Medida;
    String Locacion;
    String ImageURL;

    public Productos(){ }

    public Productos(String nombre, String marca, String codigoBarras, String cantidad, String piezas, String medida, String locacion, String imageURL) {
        Nombre = nombre;
        Marca = marca;
        CodigoBarras = codigoBarras;
        Cantidad = cantidad;
        Piezas = piezas;
        Medida = medida;
        Locacion = locacion;
        ImageURL = imageURL;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getMarca() {
        return Marca;
    }

    public void setMarca(String marca) {
        Marca = marca;
    }

    public String getCodigoBarras() {
        return CodigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        CodigoBarras = codigoBarras;
    }

    public String getCantidad() {
        return Cantidad;
    }

    public void setCantidad(String cantidad) {
        Cantidad = cantidad;
    }

    public String getPiezas() {
        return Piezas;
    }

    public void setPiezas(String piezas) {
        Piezas = piezas;
    }

    public String getMedida() {
        return Medida;
    }

    public void setMedida(String medida) {
        Medida = medida;
    }

    public String getLocacion() {
        return Locacion;
    }

    public void setLocacion(String locacion) {
        Locacion = locacion;
    }
    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }
}
