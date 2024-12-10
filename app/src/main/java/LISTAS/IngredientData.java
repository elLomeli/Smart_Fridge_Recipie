package LISTAS;

public class IngredientData {
    private String ingrediente;

    public IngredientData() {
        // Constructor vacío requerido para Firebase
    }

    public IngredientData(String ingrediente) {
        this.ingrediente = ingrediente;
    }

    public String getIngrediente() {
        return ingrediente;
    }

    public void setIngrediente(String ingrediente) {
        this.ingrediente = ingrediente;
    }
}
