package com.dev.umieplayer.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dev.umieplayer.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    EditText search_view;
    LinearLayout id;
    TextView search_text;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        search_view = v.findViewById(R.id.search_view);
        id = v.findViewById(R.id.id);
        search_text = v.findViewById(R.id.search_text);
        id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_view.animate().translationY(0).scaleX(1f).scaleY(1f).setDuration(500).start();
                search_text.animate().alpha(1).setDuration(350).start();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(search_view.getWindowToken(), 0);
            }
        });
        //translationY(-910)
        search_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    search_view.animate().y(0).scaleX(1.2f).scaleY(1.2f).setDuration(500).start();
                search_text.animate().alpha(0).setDuration(350).start();
            }
        });
        search_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_view.animate().y(0).scaleX(1.2f).scaleY(1.2f).setDuration(500).start();
                search_text.animate().alpha(0).setDuration(350).start();
            }
        });

        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        search_view.animate().translationY(0).scaleX(1f).scaleY(1f).setDuration(500).start();
        search_text.animate().alpha(1).setDuration(500).start();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search_view.getWindowToken(), 0);

    }

    @Override
    public void onPause() {
        super.onPause();
        search_view.animate().translationY(0).scaleX(1f).scaleY(1f).setDuration(500).start();
        search_text.animate().alpha(1).setDuration(500).start();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search_view.getWindowToken(), 0);


    }
    public void backAnim(){
        search_view.animate().translationY(0).scaleX(1f).scaleY(1f).setDuration(500).start();
        search_text.animate().alpha(1).setDuration(500).start();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search_view.getWindowToken(), 0);
    }


}
