package com.dev.umieplayer.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dev.umieplayer.R;

import java.util.ArrayList;

public class GenrePicherAdapter extends RecyclerView.Adapter<GenrePicherAdapter.MyViewHolder> {

    private String[] genres = {"Art Punk" ,"Alternative Rock","College Rock","Emotional Hardcore (emo / emocore)","Experimental Rock","Folk Punk","Grunge","Goth / Gothic Rock","Hardcore Punk"
            ,"Hard Rock","Indie Rock","Lo-fi","New Wave","Progressive Rock","Punk","Steampunk","Acoustic Blues","African Blues","British Blues","Classic Blues","Country Blues"
            ,"Folk Blues","Electric Blues","Jazz Blues","Piano Blues","Ballet","Classical","Cantata","Baroque","Avant-Garde","Concerto","Expressionist","Minimalism"
            ,"Opera","Symphony","Sonata","Orchestral","Parody Music","TV Themes","Country Rock","Club / Club Dance","Dubstep","Deep House","Electro House","Glitch Hop"
            ,"Garage","Grime","Hardcore","Hardcore","Techno","Speedcore","Trance","Ambient","Crunk","Drum & Bass","Darkcore","Darkstep"
            ,"Drumfunk","Electro","Electronic Rock","Electropunk","New Rave","Industrial","Hip-Hop/Rap","Freestyle Rap","New School Hip Hop","Old School Rap","Death Industrial","Electro-Industrial"
            ,"J-Pop","Jazz","Avant-Garde Jazz","K-Pop","Latin","Samba","Europop","Progressive","R&B/Soul","Rock","Gothic Metal","Hair Metal"
            ,"Hard Rock","Math Rock","Math Metal","Metal Core","Progressive Metal","Rock & Roll","Thrash Metal","Metal","Post Punk","Traditional Folk","Soundtrack","A cappella"
            ,"Doo-wop","Coldwave","Nerdcore","Dubstep"};

    private View.OnClickListener onClickListener;
    private String selected;

    private ArrayList<String> selectedGenres;

    public GenrePicherAdapter(View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
        initSelected();
    }

    private void initSelected() {
        selectedGenres = new ArrayList<>();
        for(int i=0;i<genres.length;i++){
            selectedGenres.add(genres[i]);
        }
    }

    public String getSelected() {
        return selected;
    }

    public void filter(Editable s) {
        String request = s.toString();
        if(request=="") {
            initSelected();
            notifyDataSetChanged();
        }else {
            selectedGenres = new ArrayList<>();
            for (int i = 0; i < genres.length; i++) {
                if (genres[i].toLowerCase().contains(request.toLowerCase()))
                    selectedGenres.add(genres[i]);
            }
            notifyDataSetChanged();
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        public TextView text;

        public MyViewHolder(View view) {
            super(view);
            text = view.findViewById(R.id.text);
        }

    }

    public GenrePicherAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view fill
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_genres_adapter, parent, false);
        final GenrePicherAdapter.MyViewHolder vh = new GenrePicherAdapter.MyViewHolder(view);
        return vh;
    }

    public void onBindViewHolder(@NonNull final GenrePicherAdapter.MyViewHolder holder, int position) {
        holder.text.setText(selectedGenres.get(position));
        holder.text.setOnClickListener(v -> {
            selected = (String) holder.text.getText();
            onClickListener.onClick(v);
        });
    }

    @Override
    public int getItemCount() {
        return selectedGenres.size();
    }
}
