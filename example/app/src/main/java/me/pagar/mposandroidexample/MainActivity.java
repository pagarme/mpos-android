package me.pagar.mposandroidexample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import me.pagar.mposandroid.Mpos;
import me.pagar.mposandroid.MposListener;
import me.pagar.mposandroid.MposPaymentResult;
import me.pagar.mposandroid.PaymentMethod;

public class MainActivity extends AppCompatActivity {

	private ListView listView;
	private ArrayList<String> mDeviceList = new ArrayList<String>();
	private ArrayList<BluetoothDevice> abecsList = new ArrayList<BluetoothDevice>();
	private BluetoothAdapter mBluetoothAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemClickListener(new ClickListener());

		// FIXO -- PERMISSÕES
		int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
				MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.startDiscovery();

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);

	}

	class ClickListener implements AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
			view.setSelected(true);
			BluetoothDevice device = abecsList.get(position);
			Log.d("Abecs", "SELECTED DEVICE " + device.getName());

			try {
				final int amount = 321;

				final Mpos mpos = new Mpos(abecsList.get(position), "ek_test_f9cws0bU9700VqWE4UDuBlKLbvX4IO", getApplicationContext());

				mpos.addListener(new MposListener() {
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

					// DEPRECATED NÃO USE NÃO MANDE OS CLIENTES USAREM
					public void receiveOperationCompleted() {
						Log.d("Abecs", "Operation Completed!");
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


						String query = "api_key=ak_test_NQEfPH4ktp7c9Zb0bpi1u1XkjpFCTH&amount=" + amount + "&card_hash=" + cardHash;

						try {
							//POST original do Bob
							URL url = new URL("https://api.pagar.me/1/transactions");
							HttpURLConnection connection = (HttpURLConnection) url.openConnection();
							//Set to POST
							connection.setDoOutput(true);
							connection.setRequestMethod("POST");
							connection.setReadTimeout(10000);
							Writer writer = new OutputStreamWriter(connection.getOutputStream());
							writer.write(query);
							writer.flush();
							writer.close();
							int status = connection.getResponseCode();
							String ss = connection.getResponseMessage();

							Log.d("API response: ", ss);

							HashMap t = new ObjectMapper().readValue(connection.getInputStream(), HashMap.class);

							Log.d("Abecs", "ACR CODE " + (String) t.get("acquirer_response_code"));
							Log.d("Abecs", "EMV RESPONSE " + (String) t.get("card_emv_response"));
							Log.d("Abecs", "PIN MODE " + (String) t.get("is_pin_online"));

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
						Log.d("ABECS", "Received error " + error);
					}

					public void receiveOperationCancelled() {
						Log.d("ABECS", "Cancel");
					}
				});

				Log.d("Abecs", "Telling to initialize");
				mpos.openConnection(false);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	// CÓDIGO BLUETOOTH -- FIXO
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				abecsList.add(device);
				mDeviceList.add(device.getName() + "\n" + device.getAddress());
				listView.setAdapter(new ArrayAdapter<String>(context,
						android.R.layout.simple_list_item_1, mDeviceList));
				listView.setOnItemClickListener(new ClickListener());
			}
		}
	};

}
