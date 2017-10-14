package me.pagar.mposandroidexample.listeners;

import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import me.pagar.mposandroid.localtransactionsdb.Transaction;

public class RefundListener implements AdapterView.OnItemLongClickListener
{
	private ArrayList<Transaction> transactionList;

	public RefundListener(ArrayList<Transaction> transactionList)
	{
		this.transactionList = transactionList;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		view.setSelected(true);

		Transaction transaction = transactionList.get(position);

		try {
			StrictMode.ThreadPolicy policy =
				new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);

			String query = "api_key=ak_test_NQEfPH4ktp7c9Zb0bpi1u1XkjpFCTH";

			URL url = new URL(
				"https://api.pagar.me/1/transactions/"
				+ transaction.localTransactionId + "/refund"
			);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setReadTimeout(10000);

			Writer writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(query);
			writer.flush();
			writer.close();

			int status = connection.getResponseCode();
			String message = connection.getResponseMessage();

			HashMap t = new ObjectMapper().readValue(connection.getInputStream(), HashMap.class);

			Log.d("refund", "[" + status + "]" + message);

			for(Object k : t.keySet())
			{
				Log.d("refund answer", "[" + k + "]" + t.get(k));
			}


		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}
}

