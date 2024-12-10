package POJO;

public class F_V {

    String Nombre;
    String CodigoBarras;
    String Num_Unidades;
    String Gramos;
    String ImageURL;

    public F_V(){ }

    public F_V(String nombre, String codigoBarras, String num_Unidades, String gramos, String imageURL) {
        Nombre = nombre;
        CodigoBarras = codigoBarras;
        Num_Unidades = num_Unidades;
        Gramos = gramos;
        ImageURL = imageURL;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getCodigoBarras() {
        return CodigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        CodigoBarras = codigoBarras;
    }

    public String getNum_Unidades() {
        return Num_Unidades;
    }

    public void setNum_Unidades(String num_Unidades) {
        Num_Unidades = num_Unidades;
    }

    public String getGramos() {
        return Gramos;
    }

    public void setGramos(String gramos) {
        Gramos = gramos;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }
}
