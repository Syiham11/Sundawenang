package com.example.pattimura.sundawenang.Fragment;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.pattimura.sundawenang.R;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class Aspirasi extends Fragment {
    Fragment fragment;

    public Aspirasi() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_aspirasi, container, false);
        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.floatingActionButton);
        ImageView cover = (ImageView) v.findViewById(R.id.imageView5);

        Picasso.with(Aspirasi.this.getContext()).load(R.drawable.aspirasi).fit().into(cover);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment = new TambahAspirasi();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.mainframe, fragment);
                ft.commit();
            }
        });

        return v;
    }

}
