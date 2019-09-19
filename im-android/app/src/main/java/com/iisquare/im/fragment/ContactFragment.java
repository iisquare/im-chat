package com.iisquare.im.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iisquare.im.R;
import com.iisquare.im.adapter.ContactAdapter;
import com.iisquare.im.core.FragmentBase;

public class ContactFragment extends FragmentBase {

    private RecyclerView contacts;
    private ContactAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = getActivity();
        contacts = activity.findViewById(R.id.contacts);
        contacts.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new ContactAdapter(activity);
        contacts.setAdapter(adapter);
    }
}
