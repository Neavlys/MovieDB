package com.example.sylvain.moviedb.MovieInformation;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sylvain.moviedb.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MovieInfoFragment extends Fragment {
    public MovieInfoFragment() {}

    private Button saveMovie;
    private Context ctx;
    private MovieInfoDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private Boolean searchedOrSaved, statsOrNot;
    private Boolean sameTitle;
    private String title, year, rated, runtime, actors, plot;
    private String minRuntimeStr, minRuntimeTitleStr, maxRuntimeStr, maxRuntimeTitleStr, avgRuntimeStr,
                    minYearStr, minYearTitleStr, maxYearStr, maxYearTitleStr, avgYearStr;
    private Bitmap poster;
    private View screen;
    private TextView movieTitle, movieYear, movieRated, movieRuntime, movieActors, moviePlot;
    private TextView minRuntime, maxRuntime, avgRuntime, minYear, maxYear, avgYear;
    private ImageView moviePoster;
    private ArrayList<String> titleArrayList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ctx = this.getContext();
        dbHelper = new MovieInfoDatabaseHelper(ctx);
        db = dbHelper.getWritableDatabase();
        titleArrayList = new ArrayList<>();
        cursor = db.rawQuery("SELECT " + MovieInfoDatabaseHelper.KEY_TITLE + " FROM " + MovieInfoDatabaseHelper.TABLE_NAME , null);

        final Bundle infoToPass = getArguments();

        statsOrNot = infoToPass.getBoolean("statsOrNot");
        if (statsOrNot) {
            screen = inflater.inflate(R.layout.movie_info_stats_layout, null);
            minRuntime = screen.findViewById(R.id.minRuntime);
            maxRuntime = screen.findViewById(R.id.maxRuntime);
            avgRuntime = screen.findViewById(R.id.avgRuntime);
            minYear = screen.findViewById(R.id.minYear);
            maxYear = screen.findViewById(R.id.maxYear);
            avgYear = screen.findViewById(R.id.avgYear);

            minRuntimeStr = infoToPass.getString("minRuntime");
            minRuntimeTitleStr = infoToPass.getString("minRuntimeTitle");
            maxRuntimeStr = infoToPass.getString("maxRuntime");
            maxRuntimeTitleStr = infoToPass.getString("maxRuntimeTitle");
            avgRuntimeStr = infoToPass.getString("avgRuntime");
            minYearStr = infoToPass.getString("minYear");
            minYearTitleStr = infoToPass.getString("minYearTitle");
            maxYearStr = infoToPass.getString("maxYear");
            maxYearTitleStr = infoToPass.getString("maxYearTitle");
            avgYearStr = infoToPass.getString("avgYear");

            minRuntime.setText(ctx.getString(R.string.movie_shortest) + "\n" + minRuntimeTitleStr.replace("~", "/") + " - " + minRuntimeStr + " min");
            maxRuntime.setText(ctx.getString(R.string.movie_longest) + "\n" + maxRuntimeTitleStr.replace("~", "/") + " - " + maxRuntimeStr + " min");
            avgRuntime.setText(ctx.getString(R.string.movie_average) + " " + avgRuntimeStr + " min\n");
            minYear.setText(ctx.getString(R.string.movie_oldest) + "\n" + minYearTitleStr.replace("~", "/") + " - " + minYearStr);
            maxYear.setText(ctx.getString(R.string.movie_newest) + "\n" + maxYearTitleStr.replace("~","/") + " - " + maxYearStr);
            avgYear.setText(ctx.getString(R.string.movie_average) + " " + avgYearStr);

        } else {
            screen = inflater.inflate(R.layout.movie_info_search_layout, container, false);
            saveMovie = (Button) screen.findViewById(R.id.saveMovieBtn);
            saveMovie.setText(R.string.movie_save_btn);
            moviePoster = screen.findViewById(R.id.searchedMoviePoster);
            movieTitle = screen.findViewById(R.id.searchedMovieTitle);
            movieYear = screen.findViewById(R.id.searchedMovieYear);
            movieRated = screen.findViewById(R.id.searchedMovieRated);
            movieRuntime = screen.findViewById(R.id.searchedMovieRuntime);
            movieActors = screen.findViewById(R.id.searchedMovieActors);
            moviePlot = screen.findViewById(R.id.searchedMoviePlot);

            searchedOrSaved = infoToPass.getBoolean("saved");
            if (searchedOrSaved) {
                saveMovie.setVisibility(View.INVISIBLE);
            }

            title = infoToPass.getString("title");
            if(fileExistance(title + ".jpg")) {
                FileInputStream fis = null;
                try {
                    fis = ctx.openFileInput(title + ".jpg");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                poster = BitmapFactory.decodeStream(fis);
            }
            year = infoToPass.getString("year");
            rated = infoToPass.getString("rated");
            runtime = infoToPass.getString("runtime");
            actors = infoToPass.getString("actors");
            plot = infoToPass.getString("plot");

            moviePoster.setImageBitmap(poster);
            movieTitle.setText(ctx.getString(R.string.movie_title) + " " + title.replace("~","/"));
            movieYear.setText(ctx.getString(R.string.movie_year) + " " + year);
            movieRated.setText(ctx.getString(R.string.movie_rated) + " " + rated);
            movieRuntime.setText(ctx.getString(R.string.movie_runtime) + " " + runtime + "\n");
            movieActors.setText(ctx.getString(R.string.movie_actors) + "\n" + actors + "\n");
            moviePlot.setText(ctx.getString(R.string.movie_plot) + "\n" + plot.replaceAll("&quot;", "\""));

            saveMovie.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ContentValues cv = new ContentValues();
                    cv.put(MovieInfoDatabaseHelper.KEY_TITLE, title);
                    cv.put(MovieInfoDatabaseHelper.KEY_YEAR, year.split("–")[0]);
                    cv.put(MovieInfoDatabaseHelper.KEY_RATED, rated);
                    cv.put(MovieInfoDatabaseHelper.KEY_RUNTIME, runtime.replace(" min", ""));
                    cv.put(MovieInfoDatabaseHelper.KEY_ACTORS, actors);
                    cv.put(MovieInfoDatabaseHelper.KEY_PLOT, plot.replaceAll("&quot;", "\""));

                    cursor.moveToFirst();
                    while(!cursor.isAfterLast()) {
                        titleArrayList.add(cursor.getString(cursor.getColumnIndex(MovieInfoDatabaseHelper.KEY_TITLE)));
                        cursor.moveToNext();
                    }

                    sameTitle = false;
                    if(titleArrayList.size() >= 0) {
                        for (int i = 0; i < titleArrayList.size(); i++) {
                            if (title.equals(titleArrayList.get(i))) {
                                sameTitle = true;
                            }
                        }
                        if(sameTitle) {
                            Toast.makeText(getActivity(), R.string.movie_already_saved_toast, Toast.LENGTH_SHORT).show();
                        } else if (sameTitle == false){
                            db.insert(MovieInfoDatabaseHelper.TABLE_NAME, "", cv);
                            Toast.makeText(getActivity(), R.string.movie_saved_toast, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), MovieInfo.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    }
                }
            });
        }

        return screen;
    }

    public boolean fileExistance(String fname) {
        File file = getContext().getFileStreamPath(fname);
        return file.exists();
    }
}
