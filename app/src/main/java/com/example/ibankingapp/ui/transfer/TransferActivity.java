package com.example.ibankingapp.ui.transfer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivityTransferBinding;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.ui.home.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class TransferActivity extends AppCompatActivity {
    private ActivityTransferBinding transferBinding;
    private Customer currentCustomer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transferBinding = ActivityTransferBinding.inflate(getLayoutInflater());
        setContentView(transferBinding.getRoot());


        transferBinding.fabHome.setOnClickListener(v->{
            startActivity(new Intent(this, HomeActivity.class));
        });

        loadCurrentCustomer();
        setupReciverLookup();
    }

    private void loadCurrentCustomer(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("customers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc->{
                    if (doc.exists()){
                        currentCustomer = doc.toObject(Customer.class);
                        if (currentCustomer != null) {
                            transferBinding.tvSourceBalance.setText(String.valueOf(currentCustomer.getBalance()));
                        }
                    }
                });
    }

    private void setupReciverLookup(){
        transferBinding.edtRecipientAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String accountNumberValue = s.toString().trim();
                if (accountNumberValue.isEmpty()){
                    transferBinding.tvRecipientName.setText("");
                    return;
                }
                lookupRecipient(accountNumberValue);

            }
        });
    }

    private void lookupRecipient(String accountNumber) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("customers")
                .whereEqualTo("accountNumber", accountNumber)
                .limit(1)
                .get()
                .addOnSuccessListener(query->{
                    if (!query.isEmpty()) {
                        Customer recipient = query.getDocuments().get(0).toObject(Customer.class);
                        transferBinding.tvRecipientName.setText(recipient.getFullName());
                    } else {
                        transferBinding.tvRecipientName.setText("No recipient found");
                    }
                })
                .addOnFailureListener(e->{
                    transferBinding.tvRecipientName.setText("Error: " + e.getMessage());
                });
    }
}
