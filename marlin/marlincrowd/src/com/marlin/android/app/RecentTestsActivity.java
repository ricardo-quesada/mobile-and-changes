package com.marlin.android.app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RecentTestsActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recentteststab);

		populateValues();
	}

	private void populateValues() {
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME,
				0);
		String runHistoryStr = settings.getString(Constants.RUN_HISTORY, "");
		List<String> formattedHistory = new ArrayList<String>();
		if (runHistoryStr != null && runHistoryStr.trim().length() > 0) {
			String[] runHistory = runHistoryStr.split(",");

			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm MM/dd/yyyy");

			for (String item : runHistory) {
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(Long.valueOf(item));
				formattedHistory.add(sdf.format(c.getTime()));
			}
		} else {
			formattedHistory.add("No recent tests.");
		}
		ListView lv = (ListView) findViewById(R.id.recenttestsview);

		lv.setAdapter(new ArrayAdapter<String>(this, R.layout.listitem,
				formattedHistory));

	}

	@Override
	protected void onResume() {
		super.onResume();
		populateValues();
	}
}
