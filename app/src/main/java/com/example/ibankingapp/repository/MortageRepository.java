package com.example.ibankingapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ibankingapp.data.dao.CustomerDao;
import com.example.ibankingapp.data.dao.MortageDao;
import com.example.ibankingapp.data.dao.MortagePaymentDao;
import com.example.ibankingapp.data.dao.TransactionDao;
import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.entity.CustomerEntity;
import com.example.ibankingapp.entity.MortageEntity;
import com.example.ibankingapp.entity.MortagePaymentEntity;
import com.example.ibankingapp.model.Customer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.YearMonth;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

public class MortageRepository {
    private final MortageDao dao;
    private final MortagePaymentDao paymentDao;
    private final CustomerDao customerDao;
    private final TransactionDao transactionDao;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String uid;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;







    public MortageRepository(MortageDao dao, MortagePaymentDao paymentDao, CustomerDao customerDao, TransactionDao transactionDao, TransactionRepository transactionRepository, CustomerRepository customerRepository) {
        this.dao = dao;
        this.paymentDao = paymentDao;
        this.customerDao = customerDao;
        this.transactionDao = transactionDao;
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
    }

    public void createMortage(MortageEntity mortage) {

        // 1. documentId = customerId (vì mỗi khách chỉ có 1 mortgage)
        String docId = mortage.getCustomerId();

        // 2. GÁN firebaseId NGAY LẬP TỨC
        mortage.setFirebaseId(docId);

        // 3. Nếu chưa set ngày
        if (mortage.getCreatedAt() == 0) {
            long createdAt = System.currentTimeMillis();
            mortage.setCreatedAt(createdAt);
            mortage.setNextPaymentDate(calculateNextPaymentDate(createdAt));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("customerId", mortage.getCustomerId());
        data.put("accountNumber", mortage.getAccountNumber());
        data.put("principal", mortage.getPrincipal());
        data.put("interestRate", mortage.getInterestRate());
        data.put("termMonths", mortage.getTermMonths());
        data.put("createdAt", mortage.getCreatedAt());
        data.put("nextPaymentDate", mortage.getNextPaymentDate());
        data.put("remainingBalance", mortage.getRemainingBalance());

        db.collection("mortgages")
                .document(docId)
                .set(data);

        Executors.newSingleThreadExecutor().execute(() -> {
            dao.insert(mortage);
            double monthlyAmount = calculateMonthlyAmount(mortage);
            createFirstPayment(mortage, monthlyAmount);
        });
    }


    public LiveData<MortageEntity> getMortageByCustomerId(String customerId) {
        return dao.getMortgageByCustomerId(customerId);
    }

    public LiveData<MortagePaymentEntity> getCurrentPayment(String mortgageId) {
        return paymentDao.getCurrentPayment(mortgageId);
    }

    public void syncFromFirestore(String customerId) {
        db.collection("mortgages")
                .document(customerId) // vì mỗi khách hàng chỉ có 1 mortgage
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    MortageEntity mortage = new MortageEntity();
                    mortage.setFirebaseId(snapshot.getId());
                    mortage.setCustomerId(snapshot.getString("customerId"));
                    mortage.setAccountNumber(snapshot.getString("accountNumber"));
                    mortage.setPrincipal(snapshot.getDouble("principal") != null ? snapshot.getDouble("principal") : 0.0);
                    mortage.setInterestRate(snapshot.getDouble("interestRate") != null ? snapshot.getDouble("interestRate") : 0.0);
                    Long termMonthsLong = snapshot.getLong("termMonths");
                    mortage.setTermMonths(termMonthsLong != null ? termMonthsLong.intValue() : 0);
                    mortage.setCreatedAt(snapshot.getLong("createdAt") != null ? snapshot.getLong("createdAt") : System.currentTimeMillis());
                    Long createdAt = snapshot.getLong("createdAt");
                    Long nextPayment = snapshot.getLong("nextPaymentDate");

                    mortage.setCreatedAt(createdAt != null ? createdAt : System.currentTimeMillis());

                    if (nextPayment != null && nextPayment > mortage.getCreatedAt()) {
                        mortage.setNextPaymentDate(nextPayment);
                    } else {
                        mortage.setNextPaymentDate(
                                calculateNextPaymentDate(mortage.getCreatedAt())
                        );
                    }

                    mortage.setRemainingBalance(snapshot.getDouble("remainingBalance") != null ? snapshot.getDouble("remainingBalance") : 0.0);

                    // Insert/update Room
                    Executors.newSingleThreadExecutor().execute(() -> dao.insert(mortage));
                });
    }




    public void updateMortage(MortageEntity mortage) {

        // Nếu đây là trả xong 1 kỳ → cộng thêm 1 tháng
        mortage.setNextPaymentDate(
                calculateNextPaymentDate(mortage.getNextPaymentDate())
        );

        Map<String, Object> data = new HashMap<>();
        data.put("principal", mortage.getPrincipal());
        data.put("interestRate", mortage.getInterestRate());
        data.put("termMonths", mortage.getTermMonths());
        data.put("nextPaymentDate", mortage.getNextPaymentDate()); // ⭐ QUAN TRỌNG

        db.collection("mortgages")
                .document(mortage.getFirebaseId())
                .update(data)
                .addOnSuccessListener(v ->
                        Executors.newSingleThreadExecutor().execute(() ->{
                            dao.insert(mortage);

                        })
                );
    }

    private long calculateNextPaymentDate(long fromDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(fromDate);
        cal.add(Calendar.MONTH, 1);
        return cal.getTimeInMillis();
    }

    public void createFirstPayment(MortageEntity mortage, double monthlyAmount) {

        String paymentId = UUID.randomUUID().toString();

        MortagePaymentEntity payment = new MortagePaymentEntity();
        payment.setPaymentId(paymentId);
        payment.setMortgageId(mortage.getFirebaseId());

        String period = getNextPeriod(mortage.getNextPaymentDate());
        payment.setPeriod(period);


        payment.setDueDate(mortage.getNextPaymentDate());
        payment.setAmount(monthlyAmount);
        payment.setPaidAmount(0);
        payment.setPaidAt(null);
        payment.setStatus("UNPAID");

        Map<String, Object> data = new HashMap<>();
        data.put("mortgageId", payment.getMortgageId());
        data.put("period", payment.getPeriod());
        data.put("dueDate", payment.getDueDate());
        data.put("amount", payment.getAmount());
        data.put("paidAmount", 0);
        data.put("paidAt", null);
        data.put("status", "UNPAID");

        db.collection("mortgage_payments")
                .document(paymentId)
                .set(data);

        Executors.newSingleThreadExecutor().execute(() ->
                paymentDao.insert(payment)
        );
    }

    public void payCurrentPeriod(MortagePaymentEntity payment, MortageEntity mortage, CustomerEntity customer, String accountNumber) {

        payment.setPaidAmount(payment.getAmount());
        payment.setPaidAt(System.currentTimeMillis());
        payment.setStatus("PAID");

        db.collection("mortgage_payments")
                .document(payment.getPaymentId())
                .update(
                        "paidAmount", payment.getPaidAmount(),
                        "paidAt", payment.getPaidAt(),
                        "status", "PAID"
                )
                .addOnSuccessListener(v -> Executors.newSingleThreadExecutor().execute(() -> {

                    paymentDao.insert(payment);

                    double monthlyAmount = calculateMonthlyAmount(mortage);
                    createNextPayment(mortage, monthlyAmount);

                    updateMortage(mortage);
                }));

        db.collection("mortgages")
                .document(mortage.getFirebaseId())
                .update("remainingBalance", mortage.getRemainingBalance() - payment.getAmount())
                .addOnSuccessListener(v-> Executors.newSingleThreadExecutor().execute(() ->{
                    mortage.setRemainingBalance(mortage.getRemainingBalance() - payment.getAmount());
                    dao.insert(mortage);
        }));
        uid =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("customers")
                .document(uid)
                .update("balance", customer.getBalance() - payment.getAmount())
                .addOnSuccessListener(v-> Executors.newSingleThreadExecutor().execute(() -> {
                    customer.setBalance(customer.getBalance() - payment.getAmount());
                    customerDao.insertCustomer(customer);
                }));

        transactionRepository.logTransaction(
                accountNumber,
                mortage.getAccountNumber(),
                payment.getAmount(),
                "SUCCESS",
                "MORTGAGE_PAYMENT",
                "Thanh toán vay kỳ " + payment.getPeriod()
        );



    }


    private String getNextPeriod(long dueDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dueDate);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // MONTH bắt đầu từ 0

        return String.format("%04d-%02d", year, month);
    }

    private double calculateMonthlyAmount(MortageEntity m) {
        double monthlyRate = m.getInterestRate() / 100 / 12;
        return (m.getPrincipal() * monthlyRate) +
                (m.getPrincipal() / m.getTermMonths());
    }

    private void createNextPayment(MortageEntity mortage, double amount) {

        String paymentId = UUID.randomUUID().toString();

        long nextDue = calculateNextPaymentDate(mortage.getNextPaymentDate());

        MortagePaymentEntity payment = new MortagePaymentEntity();
        payment.setPaymentId(paymentId);
        payment.setMortgageId(mortage.getFirebaseId());
        payment.setPeriod(getNextPeriod(nextDue));
        payment.setDueDate(nextDue);
        payment.setAmount(amount);
        payment.setStatus("UNPAID");

        db.collection("mortgage_payments")
                .document(paymentId)
                .set(payment)
                .addOnSuccessListener(v ->
                        Executors.newSingleThreadExecutor().execute(() ->
                                paymentDao.insert(payment)
                        )
                );

    }
}














