package com.example.ibankingapp.viewModel.keyc;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ibankingapp.repository.KeycRepository;

public class KeycViewModel extends ViewModel {
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> result = new MutableLiveData<>();
    private final KeycRepository repository = new KeycRepository();
    private Uri faceImage;

    public void setFaceImage(Uri uri){
        this.faceImage = uri;
    }

    public void verifyEkyc(){
        if (faceImage == null){
            result.postValue(false);
            return;
        }

        loading.setValue(true);
        repository.verifyEkyc(faceImage.toString() ,success -> {
            loading.postValue(false);
            result.postValue(success);
        });
    }


    public LiveData<Boolean> getLoading(){
        return loading;
    }

    public LiveData<Boolean> getResult(){
        return result;
    }


}
