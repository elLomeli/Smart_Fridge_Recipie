package com.example.recipies;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PlanPerfilViewModel extends ViewModel {
    private MutableLiveData<Float> caloriasRestantesLiveData = new MutableLiveData<>();
    public LiveData<Float> getCaloriasRestantesLiveData() {
        return caloriasRestantesLiveData;
    }

    public void setCaloriasRestantes(float caloriasRestantes) {
        caloriasRestantesLiveData.setValue(caloriasRestantes);
    }
}
