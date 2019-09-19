package com.iisquare.im.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.iisquare.im.R;
import com.iisquare.util.DPUtil;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private ArrayNode data;
    private Context context;

    public ContactAdapter(Context context) {
        this.context = context;
        data = DPUtil.arrayNode();
        for (int i = 0; i < 100; i++) {
            data.add(String.valueOf(i));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.index_contact_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonNode item = data.get(position);
        holder.textContent.setText(item.asText());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProfile;
        TextView textUserId, textContent;

        public ViewHolder(View view) {
            super(view);
            imgProfile = view.findViewById(R.id.imgProfile);
            textUserId = view.findViewById(R.id.textUserId);
            textContent = view.findViewById(R.id.textContent);
        }
    }

}
