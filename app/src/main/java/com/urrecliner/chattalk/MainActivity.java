package com.urrecliner.chattalk;

import static com.urrecliner.chattalk.Vars.aBar;
import static com.urrecliner.chattalk.Vars.mActivity;
import static com.urrecliner.chattalk.Vars.mContext;
import static com.urrecliner.chattalk.Vars.topTabs;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.urrecliner.chattalk.Sub.Permission;
import com.urrecliner.chattalk.Sub.SnackBar;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    int count = 0;
    Vars vars = null;
    SubFunc subFunc = null;
    public static Utils utils = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
            Permission.ask(this, this, info);
        } catch (Exception e) {
            Log.e("Permission", "No Permission " + e);
        }

// If you have access to the external storage, do whatever you need
        if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
        }

        if (!isNotificationAllowed(this.getPackageName())) {
            Toast.makeText(getApplicationContext(), "Allow permission on Android notification", Toast.LENGTH_LONG).show();
            Intent intListener = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intListener);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            //permissions not granted -> request them
            requestPermissions(new String[]{Manifest.permission.WRITE_SETTINGS}, 6562);
        } else {
            Toast.makeText(getApplicationContext(), " permission OK", Toast.LENGTH_LONG).show();
            //permissions are granted - do your stuff here :)
        }

        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName())) {        //ask for permission
            Log.w("Permission","required "+getPackageName());
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }

        mContext = this;
        mActivity = this;

        aBar = getSupportActionBar();

        topTabs = findViewById(R.id.tab_layout);
        topTabs.addTab(topTabs.newTab().setText("Tables"));
        topTabs.addTab(topTabs.newTab().setText("Que"));
        topTabs.addTab(topTabs.newTab().setText("Save"));
        topTabs.addTab(topTabs.newTab().setText("Stock"));
        topTabs.addTab(topTabs.newTab().setText("Alerts"));
        topTabs.addTab(topTabs.newTab().setText("Chats"));
        topTabs.setTabGravity(TabLayout.GRAVITY_FILL);

        ViewPager2 viewPager2 = findViewById(R.id.pager2);
        FragmentStateAdapter pagerAdapter = new PagerAdapter(this);
        viewPager2.setAdapter(pagerAdapter);
        viewPager2.setCurrentItem(1);
        viewPager2.setPageTransformer(new ZoomOutPageTransformer());

        topTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.reload_all_tables) {
            new OptionTables().readAll();
            AlertTable.readFile("Main");
            new SnackBar().show("All Table","Reloaded");
        }
        return false;
    }

    @Override
    protected void onResume() {
        count = 0;
        if (subFunc == null) {
            vars = new Vars();
            vars.set(mContext, "onResume");
            subFunc = new SubFunc();
        }
        if (utils == null)
            utils = new Utils();
        aBar = getSupportActionBar();
        aBar.setIcon(R.mipmap.chat_talk_mini);
        WifiMonitor.init(mContext);

        new Timer().schedule(new TimerTask() {
            public void run() {
                Intent updateIntent = new Intent(mContext, NotificationService.class);
                updateIntent.putExtra("isUpdate", true);
                mActivity.startService(updateIntent);
            }
        }, 20);

        new Timer().schedule(new TimerTask() {
            public void run () {
                Log.w("MainActivity","Count = "+count++);
            }
        }, 60000*300, 60000*300);
        super.onResume();
    }

    private boolean isNotificationAllowed(String packageName) {
        Set<String> listenerSet = NotificationManagerCompat.getEnabledListenerPackages(this);

        for (String pkg : listenerSet) {
            if (pkg != null && pkg.equals(packageName))
                return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w("MainActivity","onDestroy()");
    }
}