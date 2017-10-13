package me.pagar.mposandroidexample.listeners;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;

import me.pagar.mposandroid.Mpos;
import me.pagar.mposandroidexample.Logger;

public class LongClickListener implements AdapterView.OnItemLongClickListener
{
	private Context context;
	private ArrayList<BluetoothDevice> abecsList;
	private Logger logger;

	public LongClickListener(Context context, ArrayList<BluetoothDevice> abecsList, Logger logger)
	{
		this.context = context;
		this.abecsList = abecsList;
		this.logger = logger;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		view.setSelected(true);
		BluetoothDevice device = abecsList.get(position);
		logger.Log("Abecs", "SELECTED DEVICE " + device.getName());

		try {
			final Mpos mpos = new Mpos(abecsList.get(position), "ek_test_f9cws0bU9700VqWE4UDuBlKLbvX4IO", context);

			int count = mpos.getTransactionsSearcher().run().size();

			String counter = "Transaction count: " + count;
			logger.Log("Abecs", counter);
			Toast.makeText(context, counter, Toast.LENGTH_LONG).show();

			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}

