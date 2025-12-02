package com.example.ibankingapp.ui.transfer.transaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ibankingapp.R;
import com.example.ibankingapp.repository.CustomerRepository;
import com.example.ibankingapp.utils.TransactionDisplay;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    List<TransactionDisplay> transactions = new ArrayList<>();
    OnTransactionClickListener listener;

    private String currentAccount;

    public TransactionAdapter(String currentAccount) {
        this.currentAccount = currentAccount;
    }

    public TransactionAdapter() {
        this.currentAccount = null;
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
        return transactions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecipientName, tvRecipientAccount, tvTransactionTime, tvTransactionAmount;
        ImageView ivTransactionIcon;

        public ViewHolder(@NonNull View itemView, TransactionAdapter adapter) {
            super(itemView);

            tvRecipientName = itemView.findViewById(R.id.tvRecipientName);
            tvRecipientAccount = itemView.findViewById(R.id.tvRecipientAccount);
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



            tvRecipientName.setText(t.getRecipientName());
            tvRecipientAccount.setText("STK: " + t.getTransaction().getToAcountNumber());

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
            tvTransactionTime.setText(sdf.format(t.getTransaction().getTimestamp()));

            NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvTransactionAmount.setText(money.format(t.getTransaction().getAmount()));

            if (t.getTransaction().getToAcountNumber().equals(currentAccount)){
                tvTransactionAmount.setTextColor(itemView.getResources().getColor(R.color.green));
            }else {
                tvTransactionAmount.setTextColor(itemView.getResources().getColor(R.color.red));

            }



            ivTransactionIcon.setImageResource(R.drawable.ic_send_money_24);
        }
    }
}
