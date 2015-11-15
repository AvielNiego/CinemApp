package com.qmovie.qmovie.ui.movieDetail.movieDetailshowsPerTheater;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qmovie.qmovie.R;
import com.qmovie.qmovie.Utilities;
import com.qmovie.qmovie.entities.showsPerDay;
import com.qmovie.qmovie.ui.customComponents.RecycleViewWrapContentEnableLinearLayout;

import java.util.ArrayList;
import java.util.List;

public class MovieShowsPerTheaterAdapter
        extends android.support.v7.widget.RecyclerView.Adapter<MovieShowsPerTheaterAdapter.MovieShowsPerTheater>
{

    private List<showsPerDay> showsPerDays = new ArrayList<>();

    public MovieShowsPerTheaterAdapter(Context context, List<Long> showDatesForTheater)
    {
        readShowDatesForTheater(context, showDatesForTheater);
    }

    private void readShowDatesForTheater(Context context, List<Long> showDatesForTheater)
    {
        Long lastDate = showDatesForTheater.get(0);
        showsPerDay showsPerDay = new showsPerDay(Utilities.getDayName(context, lastDate), new ArrayList<Long>());
        showsPerDay.getHours().add(lastDate);
        for (int i = 1; i < showDatesForTheater.size(); i++)
        {
            if (Utilities.millisToDay(lastDate) != Utilities.millisToDay(showDatesForTheater.get(i)))
            {
                showsPerDays.add(showsPerDay);
                showsPerDay = new showsPerDay(Utilities.getDayName(context, showDatesForTheater.get(i)), new ArrayList<Long>());
            }
            lastDate = showDatesForTheater.get(i);
            showsPerDay.getHours().add(showDatesForTheater.get(i));
        }
        showsPerDays.add(showsPerDay);
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
        if (showsPerDays != null && showsPerDays.size() > 0)
        {
            holder.showDateTextView
                    .setText(showsPerDays.get(position).getDayName());
            holder.datesRecyclerView
                    .setLayoutManager(new RecycleViewWrapContentEnableLinearLayout(holder.datesRecyclerView.getContext(),
                                                                                   LinearLayoutManager.HORIZONTAL,
                                                                                   false));
            holder.datesRecyclerView.setAdapter(new DatesPerTheaterPerMoviesAdapter(showsPerDays.get(position).getHours()));
        }
        else
        {
            // TODO: empty view
        }
    }

    @Override
    public int getItemCount()
    {
        return showsPerDays.size();
    }

    public class MovieShowsPerTheater extends RecyclerView.ViewHolder
    {
        TextView     showDateTextView;
        RecyclerView datesRecyclerView;

        public MovieShowsPerTheater(View itemView)
        {
            super(itemView);

            showDateTextView = (TextView) itemView.findViewById(R.id.showTimeForTheaterDateTextView);
            datesRecyclerView = (RecyclerView) itemView.findViewById(R.id.showTimeForTheaterHourRecyclerView);
        }
    }
}
