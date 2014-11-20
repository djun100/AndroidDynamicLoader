package com.dianping.example.activityloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.GetChars;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import dalvik.system.DexClassLoader;

public class ActivityLoader extends ListActivity {
    private static final String tag = ActivityLoader.class.getSimpleName();
    private List<Map<String, String>> data = new ArrayList<Map<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addItem("[ Launch SampleActivity ]", null);
        addItem("[ Default.apk ]", null);
        try {
            AssetManager asset = getAssets();
            for (String s : asset.list("apks")) {
                addItem(s, "apks/" + s);
            }
        } catch (Exception e) {
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_1,
                new String[] { "title" }, new int[] { android.R.id.text1 });
        setListAdapter(adapter);
    }

    private void addItem(String title, String path) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("title", title);
        map.put("path", path);
        data.add(map);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (position == 0) {
            Intent i = new Intent("com.dianping.intent.action.SAMPLE_ACTIVITY");
            startActivity(i);
            return;
        }
        if (position == 1) {
            MyApplication.CUSTOM_LOADER = null;
            return;
        }
        Map<String, String> item = data.get(position);
        String title = item.get("title");
        String path = item.get("path");

        try {
            File file_dex = copyAsset2Dir(title, path);

            File file_outdex = getDir("outdex", Context.MODE_PRIVATE);
            Log.e(tag," optimized dex path:"+ file_outdex.getAbsolutePath());
            file_outdex.mkdir();
            DexClassLoader dcl = new DexClassLoader(file_dex.getAbsolutePath(), file_outdex.getAbsolutePath(), null,
                    MyApplication.ORIGINAL_LOADER.getParent());
            MyApplication.CUSTOM_LOADER = dcl;

            Toast.makeText(this, title + " loaded, try launch again", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to load " + title, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            MyApplication.CUSTOM_LOADER = null;
        }
    }

    private File copyAsset2Dir(String file_title, String file_path) throws IOException, FileNotFoundException {
        File dex = getDir("dex", Context.MODE_PRIVATE);
        dex.mkdir();
        File file_dex = new File(dex, file_title);
        Log.e(tag, "path:"+file_path+"copy to:"+file_dex.getAbsolutePath());
        InputStream fis = getAssets().open(file_path);
        FileOutputStream fos = new FileOutputStream(file_dex);
        byte[] buffer = new byte[0xFF];
        int len;
        while ((len = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fis.close();
        fos.close();
        return file_dex;
    }
}
