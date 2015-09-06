package com.example.jamin.charmander;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListFragment;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class RoutesFragment extends ListFragment {
    private CustomAdapter mAdapter;

    public RoutesFragment() {
        // Required empty public constructor
        mAdapter = new CustomAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes, container, false);

        setListAdapter(mAdapter);
        return view;
    }

    public class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Integer getItem(int position) {
            return 0;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                view = inflater.inflate(R.layout.list_row_routes, parent, false); // We inflate the row layout
            }

            TextView textview = (TextView) view.findViewById(R.id.routes_title);
            textview.setText("Yolo");

            return view;
        }
    }
}