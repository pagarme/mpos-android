package me.pagar.mposandroidexample.listeners;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import me.pagar.mposandroid.localtransactionsdb.Transaction;
import me.pagar.mposandroidexample.Config;

public class RefundListener implements AdapterView.OnItemLongClickListener
{
	private Context context;
	private ArrayList<Transaction> transactionList;

	private static final int LIMIT = 3;

	public RefundListener(Context context, ArrayList<Transaction> transactionList)
	{
		this.context = context;
		this.transactionList = transactionList;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		view.setSelected(true);

		Transaction transaction = transactionList.get(position);

		refund(context, transaction.localTransactionId);

		return false;
	}

	static void refund(Context context, String localTransactionId) {
		for (int t = 0; t < LIMIT; t++) {
			boolean madeIt = tryRefund(context, localTransactionId);

			if (madeIt)
				return;
		}

		showMessage(context, "Erro ao fazer estorno");
	}

	private static boolean tryRefund(Context context, String localTransactionId) {
		StrictMode.ThreadPolicy policy =
				new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		String query = "api_key=" + Config.apiKey;
		String uri = "https://api.pagar.me/1/transactions/" + localTransactionId + "/refund";

		try {

			URL url = new URL(uri);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setReadTimeout(10000);

			Writer writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(query);
			writer.flush();
			writer.close();

			int status = connection.getResponseCode();

			int transactionNotRegistered = 404;
			if (status == transactionNotRegistered) {
				return true;
			}

			if (status >= 400) {
				return false;
			}

		} catch (MalformedURLException e) {
			// Retry will not work either
			showMessage(context, "Erro ao fazer estorno: url " + uri + " malformada");
		} catch (ProtocolException ignore) {
			// Retry will not work either
			showMessage(context, "Erro ao fazer estorno");
		} catch (IOException ignore) {
			return false;
		}

		return true;
	}

	private static void showMessage(Context context, String text)
	{
		Log.d("refund-show-message", text);
	}
}

