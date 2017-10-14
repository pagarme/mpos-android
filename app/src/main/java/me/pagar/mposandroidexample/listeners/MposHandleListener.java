package me.pagar.mposandroidexample.listeners;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import me.pagar.mposandroid.Mpos;
import me.pagar.mposandroid.MposListener;
import me.pagar.mposandroid.MposPaymentResult;
import me.pagar.mposandroid.PaymentMethod;

class MposHandleListener implements MposListener
{
	private Context context;
	private Mpos mpos;
	private int amount;

	MposHandleListener(Context context, Mpos mpos, int amount)
	{
		this.context = context;
		this.mpos = mpos;
		this.amount = amount;
	}

	public void bluetoothConnected() {
		Log.d("Abecs", "Bluetooth connected.");
		mpos.initialize();
	}

	public void bluetoothDisconnected() {
		Log.d("Abecs", "Bluetooth disconnected.");
	}

	public void bluetoothErrored(int error) {
		Log.d("Abecs", "Received bluetooth error");
	}

	public void receiveInitialization() {
		Log.d("Abecs", "receive initialization!");
		try {
			mpos.downloadEMVTablesToDevice(true);
		} catch (Exception e) {
			Log.d("Abecs", "Got error in initialization and table update " + e.getMessage());
		}
	}

	public void receiveNotification(String notification) {
		Log.d("Abecs", "Got Notification " + notification);
	}

	@Deprecated
	public void receiveOperationCompleted() {
		Log.d("Abecs", "Completed");
	}

	public void receiveTableUpdated(boolean loaded) {
		Log.d("Abecs", "received table updated loaded = " + loaded);

		mpos.payAmount(amount, null, PaymentMethod.CreditCard);
	}

	public void receiveFinishTransaction() {
		Log.d("Abecs", "Finished transaction");
		mpos.close("TRANSACAO APROVADA");
	}

	public void receiveClose() {
		Log.d("Abecs", "Receive close");
		mpos.closeConnection();
	}

	public void receiveCardHash(String cardHash, MposPaymentResult result) {
		Log.d("Abecs", "Card Hash is " + cardHash);
		Log.d("Abecs", "Card Brand is " + result.cardBrand);
		Log.d("Abecs", "FD = " + result.cardFirstDigits + " LD = " + result.cardLastDigits);
		Log.d("Abecs", "ONL = " + result.isOnline);

		localTransactionId = result.localTransactionId;

		String query = "api_key=ak_test_NQEfPH4ktp7c9Zb0bpi1u1XkjpFCTH&amount=" + amount + "&card_hash=" + cardHash;

		try {
			URL url = new URL("https://api.pagar.me/1/transactions");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setReadTimeout(10000);

			Writer writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(query);
			writer.flush();
			writer.close();

			int status = connection.getResponseCode();
			String ss = connection.getResponseMessage();

			Log.d("API response", "[" + status + "] " + ss);

			HashMap t = new ObjectMapper().readValue(connection.getInputStream(), HashMap.class);

			Log.d("Abecs", "ACR CODE " + t.get("acquirer_response_code"));
			Log.d("Abecs", "EMV RESPONSE " + t.get("card_emv_response"));
			Log.d("Abecs", "PIN MODE " + t.get("is_pin_online"));

			String captureMethod = (String) t.get("capture_method");
			boolean isEmv = captureMethod.equals("emv");

			if (isEmv) {
				mpos.finishTransaction(true, Integer.parseInt((String) t.get("acquirer_response_code")), (String) t.get("card_emv_response"));
			}else{
				mpos.close("TRANSACAO APROVADA");
			}
		} catch (Exception e) {
			e.printStackTrace();
			refund();
			mpos.close("OOPS");
		}
	}

	public void receiveError(int error) {
		refund();
		Log.d("ABECS", "Received error " + error);
	}

	private String localTransactionId = "";

	private void refund() {
		if (!localTransactionId.isEmpty()) {
			RefundListener.refund(context, localTransactionId);
		}
	}

	public void receiveOperationCancelled() {
		Log.d("ABECS", "Cancel");
	}
}
