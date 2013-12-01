package com.example.doz;

import android.database.sqlite.*;
import android.content.Context;

public class SQLHelper extends SQLiteOpenHelper {
	public static String DB_NAME = "test4.db";
	public SQLHelper(Context context) {
		super(context, DB_NAME, null, 1);
	}
	
	public void onCreate(SQLiteDatabase database) {
		database.execSQL("CREATE TABLE things ( "
				+ "id integer primary key, "
				+ "description text default 'Empty Task', "
				+ "year integer not null, "
				+ "month integer not null, "
				+ "day integer not null,"
				+ "hours integer not null,"
				+ "minutes integer not null,"
				+ "location integer not null)");
		database.execSQL("CREATE TABLE locations ( "
				+ "id integer primary key autoincrement, "
				+ "lat double not null, "
				+ "lng double not null, "
				+ "name varchar(75) not null"
				+ ")");
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {	}

}
