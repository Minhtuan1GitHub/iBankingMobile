package com.example.ibankingapp.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "mortgages")
public class MortageEntity {

    @PrimaryKey
    @NonNull
    private String firebaseId;   // ID Firestore
    private String customerId;   // Khách hàng
    private String accountNumber; // Số tài khoản vay
    private double principal;     // Số tiền vay ban đầu
    private double interestRate;  // Lãi suất năm %
    private int termMonths;       // Thời hạn vay (tháng)
    private long createdAt;       // Ngày tạo khoản vay
    private long nextPaymentDate; // Ngày thanh toán tiếp theo
    private double remainingBalance; // Dư nợ còn lại

    // Constructor
    public MortageEntity(String firebaseId, String customerId, String accountNumber,
                          double principal, double interestRate, int termMonths,
                          long createdAt, long nextPaymentDate, double remainingBalance) {
        this.firebaseId = firebaseId;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.principal = principal;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.createdAt = createdAt;
        this.nextPaymentDate = nextPaymentDate;
        this.remainingBalance = remainingBalance;
    }

    public MortageEntity() {}

    // Getter & Setter
    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public double getPrincipal() { return principal; }
    public void setPrincipal(double principal) { this.principal = principal; }
    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
    public int getTermMonths() { return termMonths; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getNextPaymentDate() { return nextPaymentDate; }
    public void setNextPaymentDate(long nextPaymentDate) { this.nextPaymentDate = nextPaymentDate; }
    public double getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(double remainingBalance) { this.remainingBalance = remainingBalance; }

}
