package org.kaleta.accountant;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.kaleta.accountant.data.DataSource;
import org.kaleta.accountant.data.Transaction;
import org.kaleta.accountant.dialog.EditTransactionDialog;

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

    public class TransactionViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        private Transaction transaction;

        public TransactionViewHolder(View v) {
            super(v);
        }

        public void setTransaction(Transaction transaction) {
            this.transaction = transaction;

            ((TextView) itemView.findViewById(R.id.trDate)).setText(transaction.getDate());
            ((TextView) itemView.findViewById(R.id.trAmount)).setText(transaction.getAmount());
            if (!transaction.getDebit().isEmpty()) {
                ((TextView) itemView.findViewById(R.id.trDebit))
                        .setText(Service.getDebitAccount(transaction.getDebit()).toString());
            } else {
                ((TextView) itemView.findViewById(R.id.trDebit))
                        .setText("undefined");
            }
            if (!transaction.getCredit().isEmpty()) {
                ((TextView) itemView.findViewById(R.id.trCredit))
                        .setText(Service.getCreditAccount(transaction.getCredit()).toString());
            } else {
                ((TextView) itemView.findViewById(R.id.trCredit))
                        .setText("undefined");
            }

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            EditTransactionDialog builder = new EditTransactionDialog(itemView.getContext(), transaction);
            AlertDialog dialog = builder.create();
            dialog.show();
            builder.createValidator(dialog.getButton(AlertDialog.BUTTON_POSITIVE));
            builder.enableCancellation(dialog.getButton(AlertDialog.BUTTON_NEGATIVE));
            return true;
        }
    }
}
