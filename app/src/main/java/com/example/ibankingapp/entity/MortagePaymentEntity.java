package com.example.ibankingapp.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "mortgage_payments")
public class MortagePaymentEntity {
    @PrimaryKey
    @NonNull
    private String paymentId;

    private String mortgageId;
    private String period;        // yyyy-MM
    private long dueDate;

    private double amount;
    private double paidAmount;

    private Long paidAt;          // null nếu chưa trả
    private String status;        // PAID | UNPAID | OVERDUE

    public MortagePaymentEntity() {}

    @NonNull
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(@NonNull String paymentId) {
        this.paymentId = paymentId;
    }

    public String getMortgageId() {
        return mortgageId;
    }

    public void setMortgageId(String mortgageId) {
        this.mortgageId = mortgageId;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Long getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Long paidAt) {
        this.paidAt = paidAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
