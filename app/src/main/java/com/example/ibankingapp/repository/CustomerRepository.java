package com.example.ibankingapp.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ibankingapp.data.dao.CustomerDao;
import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.entity.CustomerEntity;
import com.example.ibankingapp.entity.TransactionEntity;
import com.example.ibankingapp.model.Customer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CustomerRepository {

    private final CustomerDao customerDao;
    private final FirebaseFirestore firestore;
    private final java.util.concurrent.Executor executor = Executors.newSingleThreadExecutor();
    private final TransactionRepository transactionRepository;
    public interface OnCustomerNameListener {
        void onNameFetched(String name);
    }

    public CustomerRepository(Context context){
        customerDao = AppDatabase.getInstance(context).customerDao();
        firestore = FirebaseFirestore.getInstance();
        transactionRepository = new TransactionRepository(AppDatabase.getInstance(context).transactionDao());
    }

    // ------------------------------------------------------
    // INSERT
    // ------------------------------------------------------
    public void insert(CustomerEntity customer) {
        executor.execute(() -> {
            // Lấy uid từ FirebaseAuth
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            customer.setId(uid); // uid = id Firestore

            // Lưu Room
            customerDao.insertCustomer(customer);

            // Lưu Firestore với document id = uid
            firestore.collection("customers")
                    .document(uid)
                    .set(customer)
                    .addOnSuccessListener(aVoid -> Log.d("CustomerRepo", "Inserted Firestore doc: " + uid))
                    .addOnFailureListener(e -> Log.e("CustomerRepo", "Insert failed", e));
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
                System.out.println("Customer ID chưa có, không thể update Firestore");
                return;
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
                            entity.setRole(doc.getString("role"));
                            entity.setOtp(doc.getString("otp"));


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
        c.setId(e.getId());
        c.setFullName(e.getFullName());
        c.setAccountNumber(e.getAccountNumber());
        c.setAccountType(e.getAccountType());
        c.setBalance(e.getBalance());
        c.setRole(e.getRole());
        c.setPhone(e.getPhone());
        c.setOtp(e.getOtp());
        return c;
    }

    private CustomerEntity convertToEntity(Customer c) {
        CustomerEntity e = new CustomerEntity();
        e.setId(c.getId());
        e.setFullName(c.getFullName());
        e.setAccountNumber(c.getAccountNumber());
        e.setAccountType(c.getAccountType());
        e.setBalance(c.getBalance());
        e.setRole(c.getRole());
        e.setPhone(c.getPhone());
        e.setOtp(c.getOtp());
        return e;
    }

    public boolean transfer(String from, String to, double amount){
        //return customerDao.transfer(from, to, amount);


        boolean success = customerDao.transfer(from, to, amount);
        if (!success){
            transactionRepository.logTransaction(from, to, amount, "fail", "transfer", "Không đủ số dư");
            return false;
        }

        CustomerEntity sender = customerDao.getCustomerByAccountNumber(from);
        CustomerEntity receiver = customerDao.getCustomerByAccountNumber(to);

        firestore.collection("customers").document(sender.getId()).set(sender);
        firestore.collection("customers").document(receiver.getId()).set(receiver);

        transactionRepository.logTransaction(from, to, amount, "success", "transfer", "Chuyển khoản thành công");



        return true;
    }
    public CustomerEntity getCustomerByAccount(String accountNumber) {
        return customerDao.getCustomerByAccount(accountNumber);
    }

    public LiveData<Customer> getCustomerByUid(String uid) {
        MutableLiveData<Customer> data = new MutableLiveData<>();

        firestore.collection("customers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        data.setValue(doc.toObject(Customer.class));
                    }
                });

        return data;
    }

    public void deposit(String uid, double amount){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference customerRef = db.collection("customers").document(uid);
        DocumentReference savingRef = db.collection("savingAccounts").document(uid);

        db.runTransaction(transaction->{
            DocumentSnapshot customerSnapshot = transaction.get(customerRef);
            DocumentSnapshot savingSnapshot = transaction.get(savingRef);
            double walletBalance = customerSnapshot.getDouble("balance");
            double savingBalance = savingSnapshot.getDouble("balance");
            if (walletBalance < amount){
                throw new RuntimeException("Không đủ số dư");
            }
            transaction.update(customerRef, "balance", walletBalance - amount);
            transaction.update(savingRef, "balance", savingBalance + amount);

            transaction.set(
                    db.collection("transactions").document(), new TransactionEntity()
            );
            return null;

        });

    }

    public void withdraw(String uid, double amoumt){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference customerRef = db.collection("customers").document(uid);
        DocumentReference savingRef = db.collection("savingAccounts").document(uid);

        db.runTransaction(transaction->{
            DocumentSnapshot customerSnapshot = transaction.get(customerRef);
            DocumentSnapshot savingSnapshot = transaction.get(savingRef);

            double walletBalance = customerSnapshot.getDouble("balance");
            double savingBalance = savingSnapshot.getDouble("balance");

            if (savingBalance < amoumt) {
                throw new RuntimeException("Không đủ số dư");
            }
            transaction.update(customerRef, "balance", walletBalance + amoumt);
            transaction.update(savingRef, "balance", savingBalance - amoumt);




            transaction.set(
                    db.collection("transactions").document(), new TransactionEntity()
            );
            return null;


        });
    }

    public LiveData<Boolean> verifyPin(String uid, String pin){
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        firestore.collection("customers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()){
                        result.setValue(false);
                        return;
                    }
                    String pinValue = doc.getString("otp");
                    result.setValue(pin.equals(pinValue));
                })
                .addOnFailureListener(e -> result.setValue(false));
        return result;
    }

    // --------------------------------------------------------
    // WALLET DEPOSIT (Nạp tiền vào ví chính qua VNPay)
    // --------------------------------------------------------
    public void walletDeposit(String uid, double amount, OnTransactionComplete callback) {
        executor.execute(() -> {
            firestore.collection("customers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onFailure("Không tìm thấy thông tin tài khoản");
                        return;
                    }

                    // Lấy số dư hiện tại
                    Object balanceObj = documentSnapshot.get("balance");
                    double currentBalance = 0;
                    if (balanceObj instanceof Number) {
                        currentBalance = ((Number) balanceObj).doubleValue();
                    }

                    // Cộng tiền vào tài khoản
                    double newBalance = currentBalance + amount;
                    String accountNumber = documentSnapshot.getString("accountNumber");

                    // Cập nhật Firestore
                    firestore.collection("customers")
                        .document(uid)
                        .update("balance", newBalance)
                        .addOnSuccessListener(aVoid -> {
                            // Cập nhật Room database
                            executor.execute(() -> {
                                CustomerEntity customer = customerDao.getCustomerByAccountNumber(accountNumber);
                                if (customer != null) {
                                    customer.setBalance(newBalance);
                                    customerDao.updateCustomer(customer);
                                }
                            });

                            // Lưu lịch sử giao dịch
                            transactionRepository.logTransaction(
                                "VNPAY",
                                accountNumber,
                                amount,
                                "success",
                                "deposit",
                                "Nạp tiền qua VNPay thành công"
                            );

                            callback.onSuccess(accountNumber, newBalance);
                        })
                        .addOnFailureListener(e -> callback.onFailure("Lỗi khi cập nhật số dư: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi khi lấy thông tin tài khoản: " + e.getMessage()));
        });
    }

    // --------------------------------------------------------
    // WALLET WITHDRAW (Rút tiền từ ví chính)
    // --------------------------------------------------------
    public void walletWithdraw(String uid, double amount, OnTransactionComplete callback) {
        executor.execute(() -> {
            firestore.collection("customers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onFailure("Không tìm thấy thông tin tài khoản");
                        return;
                    }

                    // Lấy số dư hiện tại
                    Object balanceObj = documentSnapshot.get("balance");
                    double currentBalance = 0;
                    if (balanceObj instanceof Number) {
                        currentBalance = ((Number) balanceObj).doubleValue();
                    }

                    // Kiểm tra số dư
                    if (currentBalance < amount) {
                        callback.onFailure("Số dư không đủ để thực hiện giao dịch");
                        return;
                    }

                    // Trừ tiền từ tài khoản
                    double newBalance = currentBalance - amount;
                    String accountNumber = documentSnapshot.getString("accountNumber");

                    // Cập nhật Firestore
                    firestore.collection("customers")
                        .document(uid)
                        .update("balance", newBalance)
                        .addOnSuccessListener(aVoid -> {
                            // Cập nhật Room database
                            executor.execute(() -> {
                                CustomerEntity customer = customerDao.getCustomerByAccountNumber(accountNumber);
                                if (customer != null) {
                                    customer.setBalance(newBalance);
                                    customerDao.updateCustomer(customer);
                                }
                            });

                            // Lưu lịch sử giao dịch
                            transactionRepository.logTransaction(
                                accountNumber,
                                "CASH_WITHDRAW",
                                amount,
                                "success",
                                "withdraw",
                                "Rút tiền thành công"
                            );

                            callback.onSuccess(accountNumber, newBalance);
                        })
                        .addOnFailureListener(e -> callback.onFailure("Lỗi khi thực hiện giao dịch: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi khi lấy thông tin tài khoản: " + e.getMessage()));
        });
    }

    // --------------------------------------------------------
    // CALLBACK INTERFACE
    // --------------------------------------------------------
    public interface OnTransactionComplete {
        void onSuccess(String accountNumber, double newBalance);
        void onFailure(String errorMessage);
    }

}
