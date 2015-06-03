package com.qmovie.qmovie.data;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class UpdateDataTask
{
    private static final String LOG_TAG = UpdateDataTask.class.getSimpleName();

    private static final String BASE_CINEMA_CITY_URL     = "http://m.cinema-city.co.il";
    private static final String MOVIES_URL               = BASE_CINEMA_CITY_URL + "/refreshParam";
    private static final String MOVIE_DETAIL_URL         = BASE_CINEMA_CITY_URL + "/dispArrange";
    private static final String CINEMA_CITY_BASE_PIC_URL = "http://ccil-media.internet-bee.com/Feats/med/";

    private static final String CATEGORIES             = "cats";
    private static final String MOVIE_LIST_IN_CATEGORY = "FC";
    private static final int    MOVIES_CATEGORY        = 6;
    private static final String MOVIE_ID               = "ex";
    private static final String NAME                   = "n";
    private static final String YEAR                   = "y_ds";
    private static final String PIC_URL                = "fn";
    private static final String AGE_LIMIT              = "rn";
    private static final String MOVIE_LENGTH           = "len";
    private static final String SHOWS_DATES            = "sortBDATE";
    private static final String THEATER_ID             = "id";
    private static final String MOVIE_THEATERS         = "sortSites";
    private static final String SHOW_DATE              = "bd";
    private static final String SHOW_HOUR              = "dt";
    private static final String SHOWS_HOURS            = "sortHour";
    public static final  String MOVIE_DETAILS          = "schedFeat";

    private static final int EMPTY_MOVIE_DETAIL_VALUE = 0;

    private static final List<NameValuePair> ALL_MOVIES_AND_PERFORMANCES_URL_PARAMS = new ArrayList<>();

    static
    {
        ALL_MOVIES_AND_PERFORMANCES_URL_PARAMS.add(new BasicNameValuePair("refreshFlg", "1"));
        ALL_MOVIES_AND_PERFORMANCES_URL_PARAMS
                .add(new BasicNameValuePair("timeStamp", String.valueOf(System.currentTimeMillis())));
    }

    public static final String BASE_MOVIE_SUMMARY_AND_TRAILER_URL = "http://www.cinema-city.co.il/featureInfo";

    public static void updateMovieData(Context context)
    {
        try
        {
            Log.d(LOG_TAG, "Requesting movie list");
            JSONObject allMoviesAndPerformances = getJSONObjectWithPostConnection(MOVIES_URL,
                                                                                  ALL_MOVIES_AND_PERFORMANCES_URL_PARAMS);
            Log.d(LOG_TAG, "Movie list received");

            List<ContentValues> showsToInsert = new ArrayList<>();

            JSONArray allMovies = allMoviesAndPerformances.getJSONArray(CATEGORIES).getJSONObject(MOVIES_CATEGORY)
                    .getJSONArray(MOVIE_LIST_IN_CATEGORY);

            insertAllMovies(context, allMoviesAndPerformances);

            for (int i = 0; i < allMovies.length(); i++)
            {
                try
                {
                    Long movieID = allMovies.getLong(i);
                    Log.d(LOG_TAG, "Getting " + movieID + " info. " + i + "/" + allMovies.length());

                    // If the movieID is a movieID and not a performance
                    if (jsonArrayContains(allMovies, String.valueOf(movieID)) && isMoveIsNotPerform(allMoviesAndPerformances,
                                                                                                    movieID))
                    {
                        JSONArray theaters = getMovieDetails(null, null, movieID).getJSONArray(MOVIE_THEATERS);
                        for (int j = 1; j < theaters.length(); j++)
                        {
                            Log.d(LOG_TAG, "Getting " + movieID + " theaters");

                            JSONObject theater = theaters.getJSONObject(j);
                            long theaterID = theater.getLong(THEATER_ID);
                            context.getContentResolver()
                                    .insert(MovieContract.TheaterEntry.CONTENT_URI, getTheaterContentValues(theater));

                            JSONArray showsDates = getMovieDetails(null, theaterID, movieID).getJSONArray(SHOWS_DATES);
                            for (int k = 1; k < showsDates.length(); k++)
                            {
                                Log.d(LOG_TAG, "Getting " + movieID + " show date");

                                JSONArray showsHours = getMovieDetails(showsDates.getJSONObject(k).getString(SHOW_DATE),
                                                                       theaterID,
                                                                       movieID).getJSONArray(SHOWS_HOURS);
                                for (int l = 0; l < showsHours.length(); l++)
                                {
                                    ContentValues showContentValues = getShowContentValues(showsHours.optJSONObject(l),
                                                                                           movieID,
                                                                                           theaterID);
                                    showsToInsert.add(showContentValues);
                                }

                                context.getContentResolver().bulkInsert(MovieContract.ShowEntry.CONTENT_URI,
                                                                        showsToInsert.toArray(new ContentValues[showsToInsert
                                                                                .size()]));
                                showsToInsert.clear();
                            }
                        }
                    }
                } catch (JSONException e)
                {
                    Log.v(LOG_TAG, "Couldn't load movie number " + i);
                }
            }
            Log.d(LOG_TAG, "Done receiving information from internet");
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private static JSONObject getMovieDetails(@Nullable String date, @Nullable Long theaterId, @Nullable Long movieId)
    {
        return getJSONObjectWithPostConnection(MOVIE_DETAIL_URL, getMovieDetailsUrlParams(date, theaterId, movieId));
    }

    private static ContentValues getShowContentValues(JSONObject show, long movieId, long theaterID) throws JSONException
    {
        ContentValues values = new ContentValues();

        values.put(MovieContract.ShowEntry.COLUMN_SHOW_DATE, show.getString(SHOW_HOUR));
        values.put(MovieContract.ShowEntry.COLUMN_MOVIE_KEY, movieId);
        values.put(MovieContract.ShowEntry.COLUMN_THEATER_KEY, theaterID);

        return values;
    }

    private static ContentValues getTheaterContentValues(JSONObject theater) throws JSONException
    {
        ContentValues values = new ContentValues();

        values.put(MovieContract.TheaterEntry._ID, theater.getString(THEATER_ID));
        values.put(MovieContract.TheaterEntry.COLUMN_THEATER_NAME, theater.getString(NAME));
        values.put(MovieContract.TheaterEntry.COLUMN_COORD_LAT, 0);
        values.put(MovieContract.TheaterEntry.COLUMN_COORD_LONG, 0);

        return values;
    }

    private static void insertAllMovies(Context context, JSONObject allMoviesAndPerformances) throws JSONException
    {
        JSONObject movie;
        JSONArray moviesArray = allMoviesAndPerformances.getJSONArray(MOVIE_DETAILS);
        ArrayList<ContentValues> moviesToInsert = new ArrayList<>();

        for (int i = 0; i < moviesArray.length(); i++)
        {
            movie = moviesArray.getJSONObject(i);

            if (movie != null)
            {
                String category = getCategory(allMoviesAndPerformances.getJSONArray(CATEGORIES), movie.getString(MOVIE_ID));
                if (!category.equals(""))
                {
                    String picUrl = CINEMA_CITY_BASE_PIC_URL + movie.getString(PIC_URL);
                    int movieId = movie.getInt(MOVIE_ID);
                    ContentValues movieContentValues = MovieContract.MovieEntry.createMovieContentValues(movieId,
                                                                                                         movie.getString(NAME),
                                                                                                         movie.getInt(YEAR),
                                                                                                         category,
                                                                                                         getMovieSummary(
                                                                                                                 movie.getString(
                                                                                                                         MOVIE_ID)),
                                                                                                         picUrl,
                                                                                                         movie.getString(
                                                                                                                 AGE_LIMIT),
                                                                                                         movie.getInt(
                                                                                                                 MOVIE_LENGTH),
                                                                                                         "trailer dummy");
                    moviesToInsert.add(movieContentValues);
                }
            }
        }

        int rowsInserted = context.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI,
                                                                   moviesToInsert.toArray(new ContentValues[moviesToInsert
                                                                           .size()]));
        Log.v(LOG_TAG, rowsInserted + " movies inserted");
    }

    private static boolean isMoveIsNotPerform(JSONObject allMoviesAndPerformances, Long movieId) throws JSONException
    {
        JSONObject movie = null;
        JSONArray moviesArray = allMoviesAndPerformances.getJSONArray(MOVIE_DETAILS);

        for (int i = 0; i < moviesArray.length(); i++)
        {
            if (moviesArray.getJSONObject(i).getString(MOVIE_ID).equals(String.valueOf(movieId)))
            {
                movie = moviesArray.getJSONObject(i);
            }
        }

        return movie != null;
    }

    private static String getMovieSummary(String movieId)
    {
        HttpURLConnection urlConnection = null;

        try
        {
            Uri uri = Uri.parse(BASE_MOVIE_SUMMARY_AND_TRAILER_URL);

            URL url = new URL(uri.buildUpon().appendQueryParameter("featureCode", movieId).build().toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            String response = getResponse(urlConnection);
            return Jsoup.parse(response).getElementsByAttributeValue("class", "feature_synopsis").text();

        } catch (JSONException | IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
        }
        return "";
    }

    private static List<NameValuePair> getMovieDetailsUrlParams(@Nullable String date, @Nullable Long theaterId,
                                                                @Nullable Long movieId)
    {
        List<NameValuePair> movieDetailsUrlParams = new ArrayList<>();

        if (date == null)
        {
            date = String.valueOf(EMPTY_MOVIE_DETAIL_VALUE);
        }
        if (theaterId == null)
        {
            theaterId = (long) EMPTY_MOVIE_DETAIL_VALUE;
        }
        if (movieId == null)
        {
            movieId = (long) EMPTY_MOVIE_DETAIL_VALUE;
        }


        movieDetailsUrlParams.add(new BasicNameValuePair("sortSiteFlg", "true"));
        movieDetailsUrlParams.add(new BasicNameValuePair("featFlg", "false"));
        movieDetailsUrlParams.add(new BasicNameValuePair("bdFlg", "true"));
        movieDetailsUrlParams.add(new BasicNameValuePair("dtFlg", "true"));
        movieDetailsUrlParams.add(new BasicNameValuePair("userDateBD", date));
        movieDetailsUrlParams.add(new BasicNameValuePair("userSiteID", String.valueOf(theaterId)));
        movieDetailsUrlParams.add(new BasicNameValuePair("catIndx", "0"));
        movieDetailsUrlParams.add(new BasicNameValuePair("feaureCode", String.valueOf(movieId)));

        return movieDetailsUrlParams;
    }

    private static String getCategory(JSONArray categories, String movieId) throws JSONException
    {
        String category = "";

        for (int i = 0; i < categories.length(); i++)
        {
            if (i != 6 && i != 5 && i != 4 && i != 3)
            {
                JSONObject currentCategory = categories.getJSONObject(i);
                if (jsonArrayContains(currentCategory.getJSONArray(MOVIE_LIST_IN_CATEGORY), movieId))
                {
                    String categoryName = currentCategory.getString(NAME);
                    category = category.isEmpty() ? categoryName : category + ", " + categoryName;
                }
            }
        }

        return category;
    }

    private static boolean jsonArrayContains(JSONArray jsonArray, String item) throws JSONException
    {
        for (int i = 0; i < jsonArray.length(); i++)
        {
            if (jsonArray.getString(i).equals(item))
            {
                return true;
            }
        }
        return false;
    }

    private static JSONObject getJSONObjectWithPostConnection(String urlString, List<NameValuePair> params)
    {
        HttpURLConnection urlConnection = null;


        try
        {
            URL url = new URL(urlString);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);


            setParameters(urlConnection, params);

            urlConnection.connect();

            return new JSONObject(getResponse(urlConnection));

        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (JSONException e)
        {
            return new JSONObject();
        } finally
        {
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }

        }

        return new JSONObject();
    }

    private static String getResponse(HttpURLConnection urlConnection) throws IOException, JSONException
    {
        BufferedReader reader = null;

        try
        {
            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null)
            {
                // Nothing to do.
                return "";
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null)
            {
                // It does make debugging a *lot* easier if you print out the completed
                // buffer for debugging with new lines.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0)
            {
                // Stream was empty.  No point in parsing.
                return "";
            }

            return buffer.toString();
        } finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                } catch (final IOException e)
                {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private static void setParameters(HttpURLConnection urlConnection, List<NameValuePair> params) throws IOException
    {
        OutputStream os = urlConnection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(getQuery(params));
        writer.flush();
        writer.close();
        os.close();
    }

    private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                result.append("&");
            }

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
