package com.marlin.android.app;

import com.marlin.android.app.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ShareActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sharetab);

		Button sb = (Button) findViewById(R.id.shareButton);
		sb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent shareIntent = new Intent(
						android.content.Intent.ACTION_SEND);

				shareIntent.setType("text/plain");
				shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"Be part of Mobile Crowd Test!");
				String shareMessage = "http://www.mobilecrowdtest.com";
				shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						shareMessage);
				startActivity(Intent.createChooser(shareIntent, "Share With"));

			}
		});

		Button eb = (Button) findViewById(R.id.emailButton);
		eb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				emailIntent.setType("plain/text");
				// emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				// new String[] { getResources().getString(
				// R.string.email_address) });
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"Be part of Mobile Crowd Test!");
				emailIntent.putExtra(
				         Intent.EXTRA_TEXT,
				         Html.fromHtml(new StringBuilder()
				             .append("<p><b>Help improve the mobile web...Allow your mobile device to contribute to the Mobile Crowd Test project!</b></p>")
				             .append("<small><p>Your device will help measure when the mobile web is fast - and when it is slow.  It will allow the Mobile Crowd Test project to analyze which variables are influencing the performance of the mobile web all over the world.  Your device will help answer a question that is easy to ask, but very hard to answer - what matters most when delivering a mobile web page?  The only way to achieve an accurate answer is to take measurements in real world scenarios – in your pocket and on the go!</p></small>")
				             .append("<small><p>What do you have to do to make this happen?  Absolutely nothing - that’s the best part!  Your device will do it automatically.  The Mobile Crowd Test app will occasionally run in the background for less than a minute. When it runs, nothing will appear on your screen.  Your device will simply generate a few data points – at that unique moment in time – about the speed of the mobile web.  The data points captured from your device will be combined with lots of other data points from other devices to create a big picture view of the mobile web’s speed.  As the crowd grows, much will be learned about how the mobile web performs – and we intend to share what we learn, empowering you with the results!</p></small>")
				             .append("<small><p>Please note that the data contributed by your device is completely ANONYMOUS.  We do NOT capture any personal information about you.  We do not care about you or who you are! ☺  We only care about the speed of the mobile web - as measured from your device!</p></small>")
				             .append("<small><p>Within the app, you are able to view your operating system version, device model, available memory, battery status, strength of signal, as well as view when the most recent tests were conducted.</p></small>")
				             .append("<small><p>The impact on your battery and data plan by this app is very small.  It is the same as if you visited a couple of extra web pages each day.</p></small>")
				             .append("<small><p>You can discontinue participating in the Mobile Crowd Test project at any time by simply deleting the app.  This will permanently remove the Mobile Crowd Test software from your phone.</p></small>")
				              .append("<small><p>Thank you for doing your part to improve the mobile web!</p></small>")
				             .append("Visit <a>http://www.mobilecrowdtest.com</a> to download the app!")
				             .toString())
						);

				startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			}
		});
	}
}
