package com.example.ibankingapp.viewModel.customer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.ibankingapp.entity.BankEntity;
import com.example.ibankingapp.entity.InterbankAccountEntity;
import com.example.ibankingapp.repository.InterbankTransferRepository;

import java.util.List;

public class InterbankTransferViewModel extends ViewModel {
    private final InterbankTransferRepository repository;

    public InterbankTransferViewModel(InterbankTransferRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<BankEntity>> getBanks() {
        return repository.getBanks();
    }
    public LiveData<InterbankAccountEntity> getInterbankAccount(String bankCode, String bankNumber){
        return repository.getInterbankAccount(bankCode, bankNumber);
    }
    public LiveData<Boolean> transferInterbank(
            String senderUid,
            String interbankDocId,
            double amount
    ) {
        return repository.transferInterbank(senderUid, interbankDocId, amount);
    }


}
