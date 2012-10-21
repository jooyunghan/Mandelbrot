package com.jooyunghan.mandelbrot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends Activity {

	protected static final String TAG = "MainActivity";
	private RadioGroup buttons;
	private ViewGroup views;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		buttons = (RadioGroup) findViewById(R.id.buttons);
		views = (ViewGroup) findViewById(R.id.views);

		buttons.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Log.d(TAG, "check" + checkedId);
				for (int i = 0; i < views.getChildCount(); i++) {
					View button = buttons.getChildAt(i);
					View view = views.getChildAt(i);
					if (button.getId() == checkedId) {
						view.setVisibility(View.VISIBLE);
					} else {
						view.setVisibility(View.GONE);
					}
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
