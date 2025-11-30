package com.example.ibankingapp.ui.customerList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ibankingapp.R;
import com.example.ibankingapp.model.Customer;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {

    private List<Customer> customers;
    private OnCustomerClick listener;

    public interface OnCustomerClick {
        void onCustomerClick(Customer customer);
    }

    public CustomerAdapter(List<Customer> customers, OnCustomerClick listener) {
        this.customers = new ArrayList<>(customers);
        this.listener = listener;
    }

    public void update(List<Customer> list) {
        this.customers = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Customer customer = customers.get(position);

        holder.name.setText(customer.getFullName());
        holder.accountNumber.setText(customer.getAccountNumber());

        holder.itemView.setOnClickListener(v -> listener.onCustomerClick(customer));
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, accountNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtCustomerName);
            accountNumber = itemView.findViewById(R.id.txtAccountNumber);
        }
    }
}
