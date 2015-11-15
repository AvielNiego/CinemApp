package com.qmovie.qmovie.ui.movieDetail.movieDetailshowsPerTheater;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qmovie.qmovie.R;

import java.util.List;

public class MovieShowsPerTheaterAdapter extends android.support.v7.widget.RecyclerView.Adapter<MovieShowsPerTheaterAdapter.MovieShowsPerTheater>
{

    @Nullable
    private List<String> data;

    @Nullable
    public List<String> getData()
    {
        return data;
    }

    public void setData(@Nullable List<String> data)
    {
        this.data = data;
    }

    @Override
    public MovieShowsPerTheater onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_per_theater_list_item, parent, false);
        return new MovieShowsPerTheater(view);
    }

    @Override
    public void onBindViewHolder(MovieShowsPerTheater holder, int position)
    {
        if(data != null && data.size() > 0)
        {
            holder.showDateTextView.setText(data.get(position));
        }
        else
        {
            // TODO: empty view
        }
    }

    @Override
    public int getItemCount()
    {
        return data != null ? data.size() : 0;
    }

    public class MovieShowsPerTheater extends RecyclerView.ViewHolder
    {
        TextView showDateTextView;

        public MovieShowsPerTheater(View itemView)
        {
            super(itemView);

            showDateTextView = (TextView) itemView.findViewById(R.id.showTimeForTheaterTextView);
        }
    }
}
