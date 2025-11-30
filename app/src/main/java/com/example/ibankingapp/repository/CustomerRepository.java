package com.example.ibankingapp.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ibankingapp.data.dao.CustomerDao;
import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.entity.CustomerEntity;
import com.example.ibankingapp.model.Customer;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomerRepository {

    private final CustomerDao customerDao;
    private final FirebaseFirestore firestore;
    private final java.util.concurrent.Executor executor = Executors.newSingleThreadExecutor();

    public CustomerRepository(Context context){
        customerDao = AppDatabase.getInstance(context).customerDao();
        firestore = FirebaseFirestore.getInstance();
    }

    // ------------------------------------------------------
    // INSERT
    // ------------------------------------------------------
    public void insert(CustomerEntity customer){
        executor.execute(() -> {
            customerDao.insertCustomer(customer);

            firestore.collection("customers")
                    .document(customer.getAccountNumber())
                    .set(customer);
        });
    }

    // ------------------------------------------------------
    // GET ALL (Room -> UI)
    // ------------------------------------------------------
    public List<Customer> getAllCustomers() {
        List<CustomerEntity> entities = customerDao.getAllCustomers();
        List<Customer> list = new ArrayList<>();

        for (CustomerEntity e : entities) list.add(convertToModel(e));
        return list;
    }

    // ------------------------------------------------------
    // GET ONE BY ACCOUNT NUMBER (Room -> UI) + LiveData
    // ------------------------------------------------------
    public LiveData<Customer> getCustomerByAccountNumber(String accNumber) {
        MutableLiveData<Customer> liveData = new MutableLiveData<>();

        executor.execute(() -> {
            CustomerEntity e = customerDao.getCustomerByAccountNumber(accNumber);
            if (e != null) {
                liveData.postValue(convertToModel(e));
            }
        });

        return liveData;
    }

    // ------------------------------------------------------
    // UPDATE (Room -> Firestore)
    // ------------------------------------------------------
    public void updateCustomer(Customer customer){
        CustomerEntity entity = convertToEntity(customer);

        new Thread(() -> {
            // Room update
            customerDao.updateCustomer(entity);

            // Kiểm tra id trước khi update Firestore
            String docId = entity.getId();
            if (docId == null || docId.isEmpty()) {
                // Nếu id null, fallback dùng accountNumber
                docId = entity.getAccountNumber();
                entity.setId(docId);
            }

            firestore.collection("customers")
                    .document(docId)
                    .set(entity); // set() để cập nhật, không tạo mới
        }).start();
    }


    // ------------------------------------------------------
    // LISTEN CHANGES FROM FIRESTORE -> UPDATE ROOM
    // ------------------------------------------------------
    public void listenFirestoreChanges() {
        firestore.collection("customers")
//                .whereEqualTo("role", "customer")
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;

                    new Thread(() -> {
                        customerDao.clearAll();

                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            CustomerEntity entity = new CustomerEntity();

                            // Xử lý id
                            Object idObj = doc.get("id");
                            if (idObj != null) {
                                if (idObj instanceof Long) {
                                    entity.setId(String.valueOf(idObj));
                                } else if (idObj instanceof String) {
                                    entity.setId((String) idObj);
                                }
                            } else {
                                // Nếu document không có id, dùng document ID
                                entity.setId(doc.getId());
                            }

                            // Các field khác
                            entity.setFullName(doc.getString("fullName"));
                            entity.setAccountNumber(doc.getString("accountNumber"));
                            entity.setAccountType(doc.getString("accountType"));
                            entity.setPhone(doc.getString("phone"));
                            Object balanceObj = doc.get("balance");
                            if (balanceObj instanceof Number) {
                                entity.setBalance(((Number) balanceObj).doubleValue());
                            }

                            customerDao.insertCustomer(entity);
                        }

                    }).start();
                });
    }


    // ------------------------------------------------------
    // CONVERTERS
    // ------------------------------------------------------
    private Customer convertToModel(CustomerEntity e) {
        Customer c = new Customer();
        c.setFullName(e.getFullName());
        c.setAccountNumber(e.getAccountNumber());
        c.setAccountType(e.getAccountType());
        c.setBalance(e.getBalance());
        c.setPhone(e.getPhone());
        return c;
    }

    private CustomerEntity convertToEntity(Customer c) {
        CustomerEntity e = new CustomerEntity();
        e.setFullName(c.getFullName());
        e.setAccountNumber(c.getAccountNumber());
        e.setAccountType(c.getAccountType());
        e.setBalance(c.getBalance());
        e.setPhone(c.getPhone());
        return e;
    }
}
