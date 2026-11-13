
package com.sk.multitasking;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.text.Editable;
import android.text.TextWatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    LinearLayout root;
    EditText search;
    ListView list;
    ArrayList<ResolveInfo> apps = new ArrayList<>();
    ArrayList<ResolveInfo> filtered = new ArrayList<>();
    BaseAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(10,10,20));
        getWindow().setNavigationBarColor(Color.BLACK);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 22, 18, 10);
        root.setBackgroundColor(Color.rgb(10,10,20));

        TextView title = new TextView(this);
        title.setText("SK MULTITASKING");
        title.setTextColor(Color.CYAN);
        title.setTextSize(25);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your Smart Android Launcher");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(13);
        root.addView(subtitle);

        search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Search apps...");
        search.setHintTextColor(Color.GRAY);
        search.setTextColor(Color.WHITE);
        search.setBackgroundColor(Color.rgb(35,35,50));
        root.addView(search);

        list = new ListView(this);
        root.addView(list, new LinearLayout.LayoutParams(
            -1, 0, 1
        ));

        loadApps();
        setContentView(root);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
            public void onTextChanged(CharSequence s,int st,int before,int count) {
                filterApps(s.toString());
            }
            public void afterTextChanged(Editable e) {}
        });
    }

    void loadApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> result =
            getPackageManager().queryIntentActivities(intent, 0);

        apps.clear();
        for (ResolveInfo r : result) {
            if (!r.activityInfo.packageName.equals(getPackageName())) {
                apps.add(r);
            }
        }

        Collections.sort(apps, (a,b) ->
            a.loadLabel(getPackageManager()).toString()
            .compareToIgnoreCase(
                b.loadLabel(getPackageManager()).toString()
            )
        );

        filtered.clear();
        filtered.addAll(apps);
        showApps();
    }

    void filterApps(String query) {
        filtered.clear();
        for (ResolveInfo app : apps) {
            String name = app.loadLabel(getPackageManager()).toString();
            if (name.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(app);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    void showApps() {
        adapter = new BaseAdapter() {
            public int getCount() { return filtered.size(); }
            public Object getItem(int p) { return filtered.get(p); }
            public long getItemId(int p) { return p; }

            public View getView(int p, View old, android.view.ViewGroup parent) {
                ResolveInfo app = filtered.get(p);

                LinearLayout row = new LinearLayout(MainActivity.this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(12, 12, 12, 12);

                ImageView icon = new ImageView(MainActivity.this);
                try {
                    icon.setImageDrawable(app.loadIcon(getPackageManager()));
                } catch (Exception e) {}

                row.addView(icon, new LinearLayout.LayoutParams(52,52));

                TextView name = new TextView(MainActivity.this);
                name.setText(app.loadLabel(getPackageManager()));
                name.setTextColor(Color.WHITE);
                name.setTextSize(17);
                name.setPadding(18,0,0,0);
                row.addView(name);

                return row;
            }
        };

        list.setAdapter(adapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            ResolveInfo app = filtered.get(position);
            Intent launch = new Intent();
            launch.setClassName(
                app.activityInfo.packageName,
                app.activityInfo.name
            );
            try {
                startActivity(launch);
            } catch (Exception e) {
                Toast.makeText(this, "Cannot open app", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
