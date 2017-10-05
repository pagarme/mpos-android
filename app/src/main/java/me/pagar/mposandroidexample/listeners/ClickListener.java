package me.pagar.mposandroidexample.listeners;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;

import me.pagar.mposandroid.Mpos;
import me.pagar.mposandroidexample.Logger;

public class ClickListener implements AdapterView.OnItemClickListener
{
	private Context context;
	private ArrayList<BluetoothDevice> abecsList;
	private Logger logger;

	public ClickListener(Context context, ArrayList<BluetoothDevice> abecsList, Logger logger)
	{
		this.context = context;
		this.abecsList = abecsList;
		this.logger = logger;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		view.setSelected(true);
		BluetoothDevice device = abecsList.get(position);
		logger.Log("Abecs", "SELECTED DEVICE " + device.getName());

		try {
			final Mpos mpos = new Mpos(abecsList.get(position), "ek_test_f9cws0bU9700VqWE4UDuBlKLbvX4IO", context);

			mpos.addListener(new MposHandleListener(mpos, 321, logger));

			logger.Log("Abecs", "Telling to initialize");
			mpos.openConnection(false);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

