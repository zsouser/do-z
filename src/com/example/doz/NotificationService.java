package com.example.doz;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.*;
import android.widget.Toast;

public class NotificationService extends Service {
	
	private NotificationManager manager;
	public final static int ID = 12345;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void onCreate() {
		super.onCreate();
		manager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
	}
	
	public void onDestroy() {
		super.onDestroy();
		manager.cancel(ID);
		Toast.makeText(this,"Cancelled notification",0).show();
	}
	
	public void onStart(Intent intent, int startId) {
		Intent i = new Intent(getApplicationContext(),MainActivity.class);
		String text = "NO TEXT";
		if (intent != null && intent.getExtras() != null && intent.hasExtra("text")) 
			text = intent.getExtras().getString("text");
		Notification.Builder b = new Notification.Builder(this).setContentTitle("Do something!").setContentText(text).setSmallIcon(R.drawable.ic_launcher);
		if (intent != null && intent.getExtras() != null && intent.hasExtra("id"))
			manager.notify(intent.getExtras().getInt("id"),b.build());
	}
}
