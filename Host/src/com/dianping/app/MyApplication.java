package com.dianping.app;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;

import org.json.JSONObject;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.dianping.loader.LoaderActivity;
import com.dianping.loader.MainActivity;
import com.dianping.loader.MyClassLoader;
import com.dianping.loader.RepositoryManager;
import com.dianping.loader.model.FileSpec;
import com.dianping.loader.model.FragmentSpec;
import com.dianping.loader.model.SiteSpec;

public class MyApplication extends Application {
    public static final String PRIMARY_SCHEME = "app";
    private static final String TAG = MyApplication.class.getSimpleName();
    private static MyApplication instance;
    private RepositoryManager repoManager;

    public static MyApplication instance() {
        if (instance == null) {
            throw new IllegalStateException("Application has not been created");
        }

        return instance;
    }

    public MyApplication() {
        instance = this;
    }

    public RepositoryManager repositoryManager() {
        if (repoManager == null) {
            repoManager = new RepositoryManager(this);
        }
        return repoManager;
    }

    /**获取files/repo/site.txt文件
     * @return
     */
    public SiteSpec readSite() {
        File dir = new File(getFilesDir(), "repo");
        File local = new File(dir, "site.txt");
        if (local.length() > 0) {
            try {
                FileInputStream fis = new FileInputStream(local);
                byte[] bytes = new byte[fis.available()];
                int l = fis.read(bytes);
                fis.close();
                String str = new String(bytes, 0, l, "UTF-8");
                JSONObject json = new JSONObject(str);
                return new SiteSpec(json);
            } catch (Exception e) {
                Log.w("loader", "fail to load site.txt from " + local, e);
            }
        }
        return new SiteSpec("empty.0", "0", new FileSpec[0], new FragmentSpec[0]);
    }

    public Intent urlMap(Intent intent) {
        do {
            // already specify a class, no need to map url
            //指定过目标组件，则不进行处理
            if (intent.getComponent() != null)
                break;

            // only process my scheme uri
            Uri uri = intent.getData();
            //若无uri信息，则不进行处理
            if (uri == null)
                break;
            if (uri.getScheme() == null)
                break;
            //scheme部分不为app不进行处理
            if (!(PRIMARY_SCHEME.equalsIgnoreCase(uri.getScheme())))
                break;
            // 检查并增加_site信息
            SiteSpec site = null;
            //有“_site”部分则取出值
            if (intent.hasExtra("_site")) {
                site = intent.getParcelableExtra("_site");
            }
            //如果site部分为空则重新赋值，并添加到intent
            if (site == null) {
                site = readSite();
                intent.putExtra("_site", site);
            }

            // i'm responsible，intent增加目标activity组件
            intent.setClass(this, LoaderActivity.class);
            // Gets the encoded host from the authority for this URI. For
            // example, if the authority is "bob@google.com", this method will
            // return "google.com".
            //host是后半部分
            //  "fragments": [
            //{
            //  "host": "bitmapfun",
            String host = uri.getHost();
            if (TextUtils.isEmpty(host))
                break;
            host = host.toLowerCase(Locale.US);
            FragmentSpec fragment = site.getFragment(host);
            if (fragment == null)
                break;
/*              "fragments": [
            {
                "host": "bitmapfun",
                "name": "com.example.android.bitmapfun.ui.ImageGridFragment",
                "code": "sample.bitmapfun.20130629.1"
              },*/
            //目标“_fragment”信息存入intent
            intent.putExtra("_fragment", fragment.name());

            // class loader
            ClassLoader classLoader;
            if (TextUtils.isEmpty(fragment.code())) {
                classLoader = getClassLoader();
            } else {
                intent.putExtra("_code", fragment.code());
                FileSpec fs = site.getFile(fragment.code());
                if (fs == null)
                    break;
                classLoader = MyClassLoader.getClassLoader(site, fs);
                if (classLoader == null)
                    break;
            }

            intent.setClass(this, MainActivity.class);
        } while (false);

        return intent;
    }

    @Override
    public void startActivity(Intent intent) {
        Log.e(TAG, TAG + ":startActivity()");
        intent = urlMap(intent);
        super.startActivity(intent);
    }

}
