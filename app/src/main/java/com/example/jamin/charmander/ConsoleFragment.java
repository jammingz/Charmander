package com.example.jamin.charmander;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConsoleFragment extends Fragment {


    public ConsoleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       return inflater.inflate(R.layout.console_fragment, container, false);
    }

    public void setText(String text) {
        ((TextView) getView().findViewById(R.id.console_large)).setText(text);
    }

    public String getText() {
        return ((TextView) getView().findViewById(R.id.console_large)).getText().toString();
    }


}
