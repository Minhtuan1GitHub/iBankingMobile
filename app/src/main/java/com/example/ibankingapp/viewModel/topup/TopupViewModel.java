package com.example.ibankingapp.viewModel.topup;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ibankingapp.entity.TopupEntity;
import com.example.ibankingapp.repository.TopupRepository;

import java.util.List;

public class TopupViewModel extends AndroidViewModel {

    private final TopupRepository repository;

    public TopupViewModel(@NonNull Application application) {
        super(application);
        repository = new TopupRepository(application);
    }

    public LiveData<List<TopupEntity>> getTopups(String provider){
        return repository.getAllTopups(provider);
    }

    public LiveData<Boolean> topup(String uid, String phone, long price){
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        repository.topup(uid, phone, price, result);
        return result;
    }
}
