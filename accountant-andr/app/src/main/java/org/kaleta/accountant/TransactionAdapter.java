package org.kaleta.accountant;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.kaleta.accountant.data.DataSource;
import org.kaleta.accountant.data.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> implements ValueEventListener {
    private List<Transaction> transactionList;

    public TransactionAdapter() {
        transactionList = DataSource.getInstance().getTransactionList();
        DataSource.getInstance().getTransactionRef().addValueEventListener(this);
    }

    @NonNull
    @Override
    public TransactionAdapter.TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.transaction, viewGroup, false);

        return new TransactionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionAdapter.TransactionViewHolder holder, int i) {
        holder.setTransaction(transactionList.get(i));
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        this.notifyDataSetChanged();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder {

        public TransactionViewHolder(View v) {
            super(v);
        }

        public void setTransaction(Transaction transaction) {
            ((TextView) itemView.findViewById(R.id.trDate)).setText(transaction.getDate());
            ((TextView) itemView.findViewById(R.id.trAmount)).setText(transaction.getAmount());
            ((TextView) itemView.findViewById(R.id.trDebit))
                    .setText(Service.getDebitAccount(transaction.getDebit()).toString());
            ((TextView) itemView.findViewById(R.id.trCredit))
                    .setText(Service.getCreditAccount(transaction.getCredit()).toString());
        }
    }
}
