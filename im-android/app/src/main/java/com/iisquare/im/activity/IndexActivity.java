package com.iisquare.im.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;

import com.iisquare.im.R;
import com.iisquare.im.core.ActivityBase;
import com.iisquare.im.fragment.ContactFragment;
import com.iisquare.im.fragment.SettingFragment;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class IndexActivity extends ActivityBase {

    private Map<String, Map<String, Object>> tabs = new LinkedHashMap<>();

    public IndexActivity() {
        Map<String, Object> item = new HashMap<>();
        item.put("image", R.mipmap.ic_launcher_round);
        item.put("active", R.mipmap.ic_launcher);
        item.put("text", "联系人");
        item.put("fragment", new ContactFragment());
        tabs.put("contact", item);
        item = new HashMap<>();
        item.put("image", R.mipmap.ic_launcher_round);
        item.put("active", R.mipmap.ic_launcher);
        item.put("text", "我");
        item.put("fragment", new SettingFragment());
        tabs.put("setting", item);
    }

    private View tabView(Map<String, Object> item) {
        View view = LayoutInflater.from(this).inflate(R.layout.index_tab_item, null);
        ImageView tabIcon = view.findViewById(R.id.tabImage);
        tabIcon.setImageResource((Integer) item.get("image"));
        TextView tabText = view.findViewById(R.id.tabText);
        tabText.setText(item.get("text").toString());
        return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        GridLayout layout = findViewById(R.id.tab);
        layout.setColumnCount(tabs.size());
        for (Map.Entry<String, Map<String, Object>> entry : tabs.entrySet()) {
            Map<String, Object> item = entry.getValue();
            GridLayout.Spec rowSpan = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            GridLayout.Spec colSpan = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpan, colSpan);
            layout.addView(tabView(item), params);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, (Fragment) tabs.get("contact").get("fragment")).commit();
    }

}
