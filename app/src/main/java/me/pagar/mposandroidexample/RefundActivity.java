package me.pagar.mposandroidexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import me.pagar.mposandroid.localtransactionsdb.Searcher;
import me.pagar.mposandroid.localtransactionsdb.Transaction;
import me.pagar.mposandroid.localtransactionsdb.TransactionStorage;
import me.pagar.mposandroidexample.listeners.RefundListener;


public class RefundActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment);

		ListView listView = (ListView) findViewById(R.id.listView);

		TransactionStorage storage = new TransactionStorage(this);

		Searcher searcher = storage.getSearcher();

		ArrayList<Transaction> transactionList = searcher.run();
		ArrayList<TransactionView> transactionViewList = new ArrayList<>();

		for(Transaction transaction : transactionList)
		{
			transactionViewList.add(new TransactionView(transaction));
		}

		listView.setAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, transactionViewList));

		RefundListener refundListener = new RefundListener(this, transactionList);
		listView.setOnItemLongClickListener(refundListener);
	}

	public class TransactionView
	{
		private Transaction transaction;

		TransactionView(Transaction transaction)
		{
			this.transaction = transaction;
		}

		@Override
		public String toString() {
			return amount2String(transaction.amount)
				+ "\n" + dateToString(transaction.createdAt)
				+ "\n" + transaction.firstDigits + "******" + transaction.lastDigits;
		}

		private String amount2String(int amount)
		{
			String pattern = "R$ 0.00";
			DecimalFormat format = new DecimalFormat(pattern);
			return format.format(amount / 100f);
		}

		private String dateToString(Date date)
		{
			try {
				String pattern = "yyyy-MM-dd HH:mm:ss";
				DateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
				format.setTimeZone(TimeZone.getTimeZone("GMT"));

				return format.format(date);
			} catch (Exception e) {
				return date.toString();
			}
		}
	}

}

