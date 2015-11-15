package com.qmovie.qmovie.ui.movieDetail.movieDetailshowsPerTheater;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qmovie.qmovie.R;
import com.qmovie.qmovie.Utilities;

import java.util.List;

public class DatesPerTheaterPerMoviesAdapter extends android.support.v7.widget.RecyclerView.Adapter<DatesPerTheaterPerMoviesAdapter.DatesPerTheaterPerMovieViewHolder>
{
    private List<Long> dates;

    public DatesPerTheaterPerMoviesAdapter(List<Long> dates)
    {
        this.dates = dates;
    }


    @Override
    public DatesPerTheaterPerMovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hours_per_theater_per_movie, parent, false);
        return new DatesPerTheaterPerMovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DatesPerTheaterPerMovieViewHolder holder, int position)
    {
        if (!dates.isEmpty())
        {
            holder.hoursTextView.setText(Utilities.millisToHour(dates.get(position)));
        }
        else
        {
            // TODO empty view
        }
    }

    @Override
    public int getItemCount()
    {
        return dates.size();
    }

    public class DatesPerTheaterPerMovieViewHolder extends RecyclerView.ViewHolder
    {
        TextView hoursTextView;

        public DatesPerTheaterPerMovieViewHolder(View itemView)
        {
            super(itemView);
            hoursTextView = (TextView) itemView.findViewById(R.id.datePerTheaterPerMovieTextView);
        }
    }
}
