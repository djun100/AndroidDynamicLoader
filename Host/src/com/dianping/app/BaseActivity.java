package com.dianping.app;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;

public class BaseActivity extends Activity {

	/**如果是该application本身的上下文，则调用MyApplication的urlMap(intent)，否则不进行处理
	 * @param intent
	 * @return
	 */
	public Intent urlMap(Intent intent) {
		Application app = getApplication();
		if (app instanceof MyApplication) {
			return ((MyApplication) app).urlMap(intent);
		} else {
			return intent;
		}
	}

	/**
	 * 对intent进行预处理
	 */
	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		intent = urlMap(intent);
		super.startActivityForResult(intent, requestCode);
	}
    /**
     * 对intent进行预处理
     */
	@Override
	public void startActivityFromFragment(Fragment fragment, Intent intent,
			int requestCode) {
		intent = urlMap(intent);
		super.startActivityFromFragment(fragment, intent, requestCode);
	}

	public void startActivity(String urlSchema) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlSchema)));
	}

	public void startActivityForResult(String urlSchema, int requestCode) {
		startActivityForResult(
				new Intent(Intent.ACTION_VIEW, Uri.parse(urlSchema)),
				requestCode);
	}
}
