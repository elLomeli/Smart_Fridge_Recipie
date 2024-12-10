package LISTAS;

public class StepData {
    private String paso;

    public StepData() {
        // Constructor vacío requerido para Firebase
    }

    public StepData(String paso) {
        this.paso = paso;
    }

    public String getPaso() {
        return paso;
    }

    public void setPaso(String paso) {
        this.paso = paso;
    }
}