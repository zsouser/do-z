package com.example.doz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, NotificationService.class);
		if (intent.getExtras() != null && intent.hasExtra("text") && intent.hasExtra("id")) {
			Bundle extras = new Bundle();
			extras.putString("text",intent.getExtras().getString("text"));
			extras.putInt("id", intent.getExtras().getInt("id"));
			service.putExtras(extras);
		} 

		context.startService(service);
	}
}
