package com.example.ibankingapp.ui.transfer.transaction;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ibankingapp.R;
import com.example.ibankingapp.utils.TransactionDisplay;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<TransactionDisplay> transactions = new ArrayList<>();
    private OnTransactionClickListener listener;
    private final String currentAccount;

    public TransactionAdapter(String currentAccount) {
        this.currentAccount = currentAccount;
    }

    public interface OnTransactionClickListener {
        void onClick(TransactionDisplay transaction);
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public void setTransactions(List<TransactionDisplay> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(transactions.get(position));
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecipientName, tvTransactionTime, tvTransactionAmount;
        ImageView ivTransactionIcon;

        public ViewHolder(@NonNull View itemView, TransactionAdapter adapter) {
            super(itemView);
            tvRecipientName = itemView.findViewById(R.id.tvRecipientName);
            tvTransactionTime = itemView.findViewById(R.id.tvTransactionTime);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            ivTransactionIcon = itemView.findViewById(R.id.ivTransactionIcon);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && adapter.listener != null) {
                    adapter.listener.onClick(adapter.transactions.get(pos));
                }
            });
        }

        public void bind(TransactionDisplay t) {
            if (t == null || t.getTransaction() == null) return;

            tvRecipientName.setText(t.getRecipientName());

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
            tvTransactionTime.setText(sdf.format(new Date(t.getTransaction().getTimestamp())));

            NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String amountStr = moneyFormat.format(t.getTransaction().getAmount());

            boolean isMoneyIn = t.getTransaction().getToAccountNumber().equals(currentAccount);

            if (isMoneyIn) {
                tvTransactionAmount.setText("+" + amountStr);
                tvTransactionAmount.setTextColor(Color.parseColor("#4CAF50")); // Xanh
                ivTransactionIcon.setImageResource(R.drawable.ic_wallet);
            } else {
                tvTransactionAmount.setText("-" + amountStr);
                tvTransactionAmount.setTextColor(Color.parseColor("#F44336")); // Đỏ
                ivTransactionIcon.setImageResource(R.drawable.ic_transfer_24);
            }
        }
    }
}