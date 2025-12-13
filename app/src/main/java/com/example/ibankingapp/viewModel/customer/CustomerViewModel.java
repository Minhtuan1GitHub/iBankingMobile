package com.example.ibankingapp.viewModel.customer;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.repository.CustomerRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.Executors;

public class CustomerViewModel extends AndroidViewModel {

    private final CustomerRepository repository;
    private final MutableLiveData<List<Customer>> customers = new MutableLiveData<>();

    public CustomerViewModel(@NonNull Application application) {
        super(application);
        repository = new CustomerRepository(application);

        // Lắng nghe Firestore → cập nhật Room
        repository.listenFirestoreChanges();

        // Load từ Room khi ViewModel khởi tạo
        loadCustomers();
    }

    // --------------------------------------------------------
    // GET ALL CUSTOMERS (Room)
    // --------------------------------------------------------
    public LiveData<List<Customer>> getCustomers() {
        return customers;
    }

    private void loadCustomers() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Customer> list = repository.getAllCustomers();
            customers.postValue(list);
        });
    }

    // --------------------------------------------------------
    // GET ONE BY ACCOUNT NUMBER
    // --------------------------------------------------------
    public LiveData<Customer> getCustomerByAccountNumber(String accountNumber) {
        return repository.getCustomerByAccountNumber(accountNumber);
    }

    // --------------------------------------------------------
    // UPDATE CUSTOMER
    // --------------------------------------------------------
    public void updateCustomer(Customer customer) {
        repository.updateCustomer(customer);
    }

    public LiveData<Boolean> transfer(String from, String to, double amount){
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean success = repository.transfer(from, to, amount); // thực hiện transfer
            if (success){
                loadCustomers(); // nếu bạn muốn refresh data
            }
            result.postValue(success);
        });
        return result;
    }

    public LiveData<Boolean> hasSavingAccount(String customerId){
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("savingAccounts")
                .whereEqualTo("customer_id", customerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        result.postValue(!task.getResult().isEmpty());
                    } else {
                        result.postValue(false);
                    }
                });
        return result;
    }
}
