package com.example.ibankingapp.viewModel.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class LoginViewModel extends ViewModel {

    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> password = new MutableLiveData<>();

    private MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private FirebaseAuthManager auth = new FirebaseAuthManager();
    public LiveData<Boolean> getLoginResult(){
        return loginSuccess;
    }
    public void onLoginClick(){

        String emailValue = email.getValue() != null ? email.getValue().trim() : "";
        String passwordValue = password.getValue() != null ? password.getValue().trim() : "";

        if (emailValue.isEmpty() || passwordValue.isEmpty()){
            loginSuccess.setValue(false);
            return;
        }

        auth.login(email.getValue(), password.getValue(), task -> {
            loginSuccess.setValue(task.isSuccessful());
        });
    }
}
