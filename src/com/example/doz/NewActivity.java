package com.example.doz;

import android.os.Bundle;
import java.util.Calendar;
import android.app.*;
import android.view.Menu;
import java.util.ArrayList;
import android.widget.*;
import android.view.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import android.content.*;
import android.database.sqlite.*;
import android.database.*;
public class NewActivity extends Activity {
	
	/**
	 * Input fields
	 */
	public EditText text;
	public DatePicker date;
	public TimePicker time;
	public Spinner spinner;
	public SQLiteDatabase db;
	public int locationId;
	public int selectedIndex;
	public AlarmManager alarmManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new);
		SQLHelper helper = new SQLHelper(this);
		db = helper.getWritableDatabase();
		text = (EditText) findViewById(R.id.edit);
		date = (DatePicker) findViewById(R.id.datePicker);
		time = (TimePicker) findViewById(R.id.timePicker);
		spinner = (Spinner) findViewById(R.id.spinner);
		Bundle extras = savedInstanceState;
		if (getIntent() != null && getIntent().hasExtra("id")) {
			extras = getData(getIntent().getExtras().getInt("id"));
		}
		restoreDateTime(extras);
		restoreText(extras);
		restoreSpinner(extras);
		
	}
	
	public Bundle getData(int id) {
		Cursor c = db.rawQuery("SELECT description, year, month, day, hours, minutes, location FROM things WHERE id = "+id, null);
		Bundle b = new Bundle();
		if (c.moveToFirst()) {
			b.putString("text",c.getString(0));
			b.putInt("year", c.getInt(1));
			b.putInt("month", c.getInt(2));
			b.putInt("day", c.getInt(3));
			b.putInt("hours", c.getInt(4));
			b.putInt("minutes", c.getInt(5));
			b.putInt("location", c.getInt(6));
		}
		return b;
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create, menu);
        return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save:
			if (text.toString().isEmpty()) {
				Toast.makeText(this,(CharSequence)"Please specify the task.",1).show();
			} else {
				save();
				finish();
			}
			return true;
		case R.id.complete:
			Toast.makeText(this, "Good Job!", 10).show();
			delete();
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void delete() {
		if (getIntent() != null && getIntent().hasExtra("id")) {
			db.execSQL("DELETE FROM things WHERE id = "+getIntent().getExtras().getInt("id"));
			cancelAlarm(getIntent().getExtras().getInt("id"));
		}
	}
	
	public void save() {
		Toast.makeText(this,(CharSequence)"Save",10).show();
		// save to database
		if (getIntent() != null && getIntent().hasExtra("id")) {
			db.execSQL("UPDATE things SET "
					+ "description = ?1, "
					+ "day = ?2, "
					+ "month = ?3, "
					+ "year = ?4, "
					+ "hours = ?5, "
					+ "minutes = ?6, "
					+ "location = ?7"
					+ "WHERE id = ?8", new String[] {
							text.getText().toString(),
							""+date.getDayOfMonth(),
							""+date.getMonth(),
							""+date.getYear(),
							""+time.getCurrentHour(),
							""+time.getCurrentMinute(),
							""+locationId,
							""+getIntent().getExtras().getInt("id")
					});
			cancelAlarm(getIntent().getExtras().getInt("id"));
			createAlarm(getIntent().getExtras().getInt("id"));
		} else {
			int id = locationId;
			if (id < 0) id = 0;
			db.execSQL("INSERT INTO things (description, day, month, year, hours, minutes, location) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)",
					 new String[] {
					text.getText().toString(),
					""+date.getDayOfMonth(),
					""+date.getMonth(),
					""+date.getYear(),
					""+time.getCurrentHour(),
					""+time.getCurrentMinute(),
					""+id
			});
			Cursor c = db.rawQuery("SELECT id FROM things ORDER BY id DESC LIMIT 1",null);
			if (c.moveToFirst())
				createAlarm(c.getInt(0));
			else Toast.makeText(this,"No alarm set",0).show();
		}
	}
	
	private void createAlarm(int id) {
		Cursor c = db.rawQuery("SELECT description FROM things WHERE id = "+id,null);
		String text = "NOTHING";
		if (c.moveToFirst()) {
			text = c.getString(0);
		}
		Calendar calendar = Calendar.getInstance();
	     
	      calendar.set(Calendar.MONTH, date.getMonth());
	      calendar.set(Calendar.YEAR, date.getYear());
	      calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
	 
	      calendar.set(Calendar.HOUR_OF_DAY, time.getCurrentHour());
	      calendar.set(Calendar.MINUTE, time.getCurrentMinute());
	      calendar.set(Calendar.SECOND, 0);
	     
	      Intent myIntent = new Intent(NewActivity.this, AlarmReceiver.class);
	      
	      Bundle extras = new Bundle();
	      extras.putInt("id", id);
	      extras.putString("text",text);
	      
	      myIntent.putExtras(extras);
	      PendingIntent pendingIntent = PendingIntent.getBroadcast(NewActivity.this, id, myIntent,PendingIntent.FLAG_ONE_SHOT);
	      alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
	      alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
	    
	}
	
	private void cancelAlarm(int id) {
		Cursor c = db.rawQuery("SELECT description FROM things WHERE id = "+id,null);
		String text = "NOTHING";
		if (c.moveToFirst()) {
			text = c.getString(0);
		}
		alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		Intent myIntent = new Intent(NewActivity.this, AlarmReceiver.class);
	    Bundle extras = new Bundle();
	    extras.putInt("id", id);
	    extras.putString("text", text);
	    myIntent.putExtras(extras);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(NewActivity.this, id, myIntent,PendingIntent.FLAG_ONE_SHOT);
	    alarmManager.cancel(pendingIntent);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("day", date.getDayOfMonth());
		savedInstanceState.putInt("month", date.getMonth());
		savedInstanceState.putInt("year", date.getYear());
		savedInstanceState.putInt("hours", time.getCurrentHour());
		savedInstanceState.putInt("minutes", time.getCurrentMinute());
		savedInstanceState.putString("text",text.getText().toString());
		savedInstanceState.putInt("location",locationId);
	}
	
	@Override 
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent i) {
		if (requestCode == 2) {
			if  (i != null && i.hasExtra("success")) {
				spinner.setSelection(spinner.getAdapter().getCount()-1);
				Toast.makeText(this,(CharSequence)"Created Location",10).show();
			}
			else {
				Toast.makeText(this,(CharSequence)"No Location",10).show();
			}
		}
	}
	
	private void restoreText(Bundle bundle) {
		if (bundle == null) return;
		if (bundle.containsKey("text")) {
			text.setText((CharSequence)bundle.getString("text"));
		}
	}

	
	private void restoreDateTime(Bundle bundle) {
		if (bundle == null) return;
		if (bundle.containsKey("day") && bundle.containsKey("month") && bundle.containsKey("year")) {
			date.updateDate(bundle.getInt("year"), bundle.getInt("month"), bundle.getInt("day"));
		}
		if (bundle.containsKey("hours")) {
				time.setCurrentHour(bundle.getInt("hours"));
		}
		if (bundle.containsKey("minutes")) {
			time.setCurrentMinute(bundle.getInt("minutes"));
		}
	}
	
	private ArrayList<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<Location>();
		locations.add(new Location(-2,"No Location"));
		locations.add(new Location(-1,"Create New"));
		Cursor c = db.rawQuery("SELECT id, name FROM locations", null);
		if (c.moveToFirst()) do {
			locations.add(new Location(c.getInt(0),c.getString(1)));
		} while (c.moveToNext());
		return locations;
	}
	
	private void restoreSpinner(Bundle bundle) {
		spinner.setAdapter(new ArrayAdapter<Location>(this, android.R.layout.simple_spinner_item, getLocations()));
		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() 
		{
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) 
		    {
		    	if (position == 1) {
		    		Intent i = new Intent(NewActivity.this, LocationActivity.class);
		    		startActivityForResult(i,2);
		    	}
		    	//selectedIndex = position;
		    	locationId = ((Location)parentView.getAdapter().getItem(position)).id;
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) 
		    {
		       
		    }
		});
		if (bundle == null) return;
		if (bundle.containsKey("location")) {
			int i = 0;
			for (Location l : getLocations()) {
				if (l.id == bundle.getInt("location")) {
					spinner.setSelection(i);
				}
				i++;
			}
		}
	}
	
	private class Location {
		public final String name;
		public final int id;
		public Location(int id, String name) {
			this.id = id;
			this.name = name;
		}
		public String toString() {
			return name;
		}
	}
}
