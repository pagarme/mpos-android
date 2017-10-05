package me.pagar.mposandroidexample.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import me.pagar.mposandroidexample.Logger;
import me.pagar.mposandroid.Mpos;
import me.pagar.mposandroid.MposListener;
import me.pagar.mposandroid.MposPaymentResult;
import me.pagar.mposandroid.PaymentMethod;

class MposHandleListener implements MposListener
{
	private Mpos mpos;
	private int amount;
	private Logger logger;

	MposHandleListener(Mpos mpos, int amount, Logger logger)
	{
		this.mpos = mpos;
		this.amount = amount;
		this.logger = logger;
	}

	public void bluetoothConnected() {
		logger.Log("Abecs", "Bluetooth connected.");
		mpos.initialize();
	}

	public void bluetoothDisconnected() {
		logger.Log("Abecs", "Bluetooth disconnected.");
	}

	public void bluetoothErrored(int error) {
		logger.Log("Abecs", "Received bluetooth error");
	}

	public void receiveInitialization() {
		logger.Log("Abecs", "receive initialization!");
		try {
			mpos.downloadEMVTablesToDevice(true);
		} catch (Exception e) {
			logger.Log("Abecs", "Got error in initialization and table update " + e.getMessage());
		}
	}

	public void receiveNotification(String notification) {
		logger.Log("Abecs", "Got Notification " + notification);
	}

	@Deprecated
	public void receiveOperationCompleted() {
		logger.Log("Abecs", "Completed");
	}

	public void receiveTableUpdated(boolean loaded) {
		logger.Log("Abecs", "received table updated loaded = " + loaded);

		mpos.payAmount(amount, null, PaymentMethod.CreditCard);
	}

	public void receiveFinishTransaction() {
		logger.Log("Abecs", "Finished transaction");
		mpos.close("TRANSACAO APROVADA");
	}

	public void receiveClose() {
		logger.Log("Abecs", "Receive close");
		mpos.closeConnection();
	}

	public void receiveCardHash(String cardHash, MposPaymentResult result) {
		logger.Log("Abecs", "Card Hash is " + cardHash);
		logger.Log("Abecs", "Card Brand is " + result.cardBrand);
		logger.Log("Abecs", "FD = " + result.cardFirstDigits + " LD = " + result.cardLastDigits);
		logger.Log("Abecs", "ONL = " + result.isOnline);

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

			logger.Log("API response", "[" + status + "] " + ss);

			HashMap t = new ObjectMapper().readValue(connection.getInputStream(), HashMap.class);

			logger.Log("Abecs", "ACR CODE " + t.get("acquirer_response_code"));
			logger.Log("Abecs", "EMV RESPONSE " + t.get("card_emv_response"));
			logger.Log("Abecs", "PIN MODE " + t.get("is_pin_online"));

			Object isPinOnlineContent = t.get("is_pin_online");
			boolean isPinOnline = isPinOnlineContent == null || (Boolean)isPinOnlineContent;

			if (isPinOnline) {
				mpos.finishTransaction(true, Integer.parseInt((String) t.get("acquirer_response_code")), (String) t.get("card_emv_response"));
			}else{
				mpos.close("TRANSACAO APROVADA");
			}
		} catch (Exception e) {
			e.printStackTrace();
			//mpos.finishTransaction(false, 0, null);
			mpos.close("OOPS");
		}
	}

	public void receiveError(int error) {
		logger.Log("ABECS", "Received error " + error);
	}

	public void receiveOperationCancelled() {
		logger.Log("ABECS", "Cancel");
	}
}
