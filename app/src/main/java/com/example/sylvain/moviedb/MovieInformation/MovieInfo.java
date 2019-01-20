package com.example.sylvain.moviedb.MovieInformation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sylvain.moviedb.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.sylvain.moviedb.MovieInformation.MovieInfoDatabaseHelper.KEY_ID;
import static com.example.sylvain.moviedb.MovieInformation.MovieInfoDatabaseHelper.TABLE_NAME;

public class MovieInfo extends AppCompatActivity {

    public AppCompatActivity getActivity() {
        return this;
    }

    private Toolbar movieToolbar;

    private ListView savedMovies;
    private EditText searchText;
    private Button searchBtn, statisticsBtn;
    private ProgressBar progressBar;
    private ArrayList<String> movieArray;

    private MovieAdaptor movieAdaptor;
    private Context ctx;
    private MovieInfoDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    private String movieTitle;
    private Bitmap moviePosterBitmap;
    private AsyncMovieInfoAPI query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_info);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        movieToolbar = findViewById(R.id.movieToolbar);
        movieToolbar.setTitle(R.string.movie);
        setSupportActionBar(movieToolbar);

        savedMovies = (ListView)findViewById(R.id.movieView);
        searchText = (EditText)findViewById(R.id.movieText);
        searchBtn = (Button)findViewById(R.id.movieBtn);
        searchBtn.setText(R.string.movie_search_btn);
        statisticsBtn = (Button)findViewById(R.id.statisticsBtn);
        statisticsBtn.setText(R.string.movie_stats_btn);
        movieArray = new ArrayList<>();
        progressBar = (ProgressBar)findViewById(R.id.movieProgressBar);

        LayoutInflater inflater = MovieInfo.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.movie_info_search_layout, null);

        ctx = this;
        movieAdaptor = new MovieAdaptor(ctx);
        savedMovies.setAdapter(movieAdaptor);

        dbHelper = new MovieInfoDatabaseHelper(ctx);
        db = dbHelper.getWritableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            movieArray.add(cursor.getString(cursor.getColumnIndex(MovieInfoDatabaseHelper.KEY_TITLE)));
            cursor.moveToNext();
        }

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movieTitle = searchText.getText().toString();
                query = new AsyncMovieInfoAPI();
                query.execute();
            }
        });

        savedMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle infoToPass = new Bundle();

                cursor.moveToPosition(position);
                infoToPass.putBoolean("saved", true);
                infoToPass.putString("title", cursor.getString(cursor.getColumnIndex(MovieInfoDatabaseHelper.KEY_TITLE)));
                infoToPass.putString("year", cursor.getString(cursor.getColumnIndex(MovieInfoDatabaseHelper.KEY_YEAR)));
                infoToPass.putString("rated", cursor.getString(cursor.getColumnIndex(MovieInfoDatabaseHelper.KEY_RATED)));
                infoToPass.putString("runtime", cursor.getString(cursor.getColumnIndex(MovieInfoDatabaseHelper.KEY_RUNTIME))+ " " + ctx.getString(R.string.movie_min));
                infoToPass.putString("actors", cursor.getString(cursor.getColumnIndex(MovieInfoDatabaseHelper.KEY_ACTORS)));
                infoToPass.putString("plot", cursor.getString(cursor.getColumnIndex(MovieInfoDatabaseHelper.KEY_PLOT)));

                Intent intent = new Intent(MovieInfo.this, MovieInfoDetails.class);
                intent.putExtras(infoToPass);
                startActivity(intent);
                getActivity().finish();
            }
        });

        savedMovies.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MovieInfo.this);
                builder.setMessage(R.string.movie_confirm_delete);
                builder.setTitle(R.string.movie_delete);

                builder.setPositiveButton(R.string.movie_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMovie(id);
                        Toast.makeText(getActivity(), R.string.movie_deleted_toast, Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton(R.string.movie_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                return false;
            }
        });

        if (movieArray.size() == 0) {
            statisticsBtn.setVisibility(View.INVISIBLE);
        }
        statisticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean statsOrNot = true;

                cursor = db.rawQuery("SELECT MIN(CAST(" + MovieInfoDatabaseHelper.KEY_YEAR + " AS INT)" + ") " + "FROM " + MovieInfoDatabaseHelper.TABLE_NAME, null);
                cursor.moveToFirst();
                String minYear = cursor.getString(0);

                cursor = db.rawQuery("SELECT " + MovieInfoDatabaseHelper.KEY_TITLE + " FROM " + MovieInfoDatabaseHelper.TABLE_NAME + " WHERE " + MovieInfoDatabaseHelper.KEY_YEAR + " = (SELECT MIN(CAST(" + MovieInfoDatabaseHelper.KEY_YEAR + " AS INT)" + ") FROM " + MovieInfoDatabaseHelper.TABLE_NAME + ")", null);
                cursor.moveToFirst();
                String minYearTitle = cursor.getString(0);

                cursor = db.rawQuery("SELECT MAX(CAST(" + MovieInfoDatabaseHelper.KEY_YEAR + " AS INT)" + ") " + "FROM " + MovieInfoDatabaseHelper.TABLE_NAME,null);
                cursor.moveToFirst();
                String maxYear = cursor.getString(0);

                cursor = db.rawQuery("SELECT " + MovieInfoDatabaseHelper.KEY_TITLE + " FROM " + MovieInfoDatabaseHelper.TABLE_NAME + " WHERE " + MovieInfoDatabaseHelper.KEY_YEAR + " = (SELECT MAX(CAST(" + MovieInfoDatabaseHelper.KEY_YEAR + " AS INT)" + ") FROM " + MovieInfoDatabaseHelper.TABLE_NAME + ")", null);
                cursor.moveToFirst();
                String maxYearTitle = cursor.getString(0);

                cursor = db.rawQuery("SELECT AVG(CAST(" + MovieInfoDatabaseHelper.KEY_YEAR + " AS INT)" +") " + "FROM " + MovieInfoDatabaseHelper.TABLE_NAME,null);
                cursor.moveToFirst();
                String avgYear = cursor.getString(0);

                cursor = db.rawQuery("SELECT MIN(CAST(" + MovieInfoDatabaseHelper.KEY_RUNTIME + " AS INT)" + ") " + "FROM " + MovieInfoDatabaseHelper.TABLE_NAME,null);
                cursor.moveToFirst();
                String minRuntime = cursor.getString(0);

                cursor = db.rawQuery("SELECT " + MovieInfoDatabaseHelper.KEY_TITLE + " FROM " + MovieInfoDatabaseHelper.TABLE_NAME + " WHERE " + MovieInfoDatabaseHelper.KEY_RUNTIME + " = (SELECT MIN(CAST(" + MovieInfoDatabaseHelper.KEY_RUNTIME + " AS INT)" + ") FROM " + MovieInfoDatabaseHelper.TABLE_NAME + ")", null);
                cursor.moveToFirst();
                String minRuntimeTitle = cursor.getString(0);

                cursor = db.rawQuery("SELECT MAX(CAST(" + MovieInfoDatabaseHelper.KEY_RUNTIME + " AS INT)" + ") " + "FROM " + MovieInfoDatabaseHelper.TABLE_NAME,null);
                cursor.moveToFirst();
                String maxRuntime = cursor.getString(0);

                cursor = db.rawQuery("SELECT " + MovieInfoDatabaseHelper.KEY_TITLE + " FROM " + MovieInfoDatabaseHelper.TABLE_NAME + " WHERE " + MovieInfoDatabaseHelper.KEY_RUNTIME + " = (SELECT MAX(CAST(" + MovieInfoDatabaseHelper.KEY_RUNTIME + " AS INT)" + ") FROM " + MovieInfoDatabaseHelper.TABLE_NAME + ")", null);
                cursor.moveToFirst();
                String maxRuntimeTitle = cursor.getString(0);

                cursor = db.rawQuery("SELECT AVG(CAST(" + MovieInfoDatabaseHelper.KEY_RUNTIME + " AS INT)" + ") " + "FROM " + MovieInfoDatabaseHelper.TABLE_NAME,null);
                cursor.moveToFirst();
                String avgRuntime = cursor.getString(0);

                Bundle infoToPass = new Bundle();
                infoToPass.putBoolean("statsOrNot", statsOrNot);
                infoToPass.putString("minYear", minYear);
                infoToPass.putString("minYearTitle", minYearTitle);
                infoToPass.putString("maxYear", maxYear);
                infoToPass.putString("maxYearTitle", maxYearTitle);
                infoToPass.putString("avgYear", avgYear);
                infoToPass.putString("minRuntime", minRuntime);
                infoToPass.putString("minRuntimeTitle", minRuntimeTitle);
                infoToPass.putString("maxRuntime", maxRuntime);
                infoToPass.putString("maxRuntimeTitle", maxRuntimeTitle);
                infoToPass.putString("avgRuntime", avgRuntime);

                Intent intent = new Intent(MovieInfo.this, MovieInfoDetails.class);
                intent.putExtras(infoToPass);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.movie_info_help:
                AlertDialog.Builder builder = new AlertDialog.Builder(MovieInfo.this);
                builder.setMessage(ctx.getString(R.string.movie_created_by)+ "\n" + ctx.getString(R.string.movie_activity_version) + "\n" + ctx.getString(R.string.movie_instructions_main));
                builder.setTitle(R.string.movie_help);

                builder.setPositiveButton(R.string.movie_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movie_info_toolbar_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    public void deleteMovie(long id) {
        db.delete(TABLE_NAME, KEY_ID + "=" + id, null);
        movieArray.clear();
        cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        movieAdaptor.notifyDataSetChanged();

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            movieArray.add(cursor.getString(cursor.getColumnIndex(MovieInfoDatabaseHelper.KEY_TITLE)));
            cursor.moveToNext();
        }

        if(movieArray.size() == 0) {
            statisticsBtn.setVisibility(View.INVISIBLE);
        }

    }

    private void launchActivity(Class act){
        Intent intent = new Intent(MovieInfo.this, act);
        startActivity(intent);
        getActivity().finish();
    }

    private class MovieAdaptor extends ArrayAdapter<String> {
        public MovieAdaptor(Context ctx) {super(ctx, 0);}

        public int getCount() {return movieArray.size();}

        public String getItem(int position) {return movieArray.get(position);}

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = MovieInfo.this.getLayoutInflater();

            View result = inflater.inflate(R.layout.movie_info_list_layout, null);

            ImageView poster = (ImageView)result.findViewById(R.id.moviePosterImage);
            TextView title = (TextView)result.findViewById(R.id.movieTitleText);
            title.setText(getItem(position).replace("~", "/"));
            FileInputStream fis = null;
            try {
                cursor.moveToPosition(position);
                fis = ctx.openFileInput(cursor.getString(cursor.getColumnIndex(MovieInfoDatabaseHelper.KEY_TITLE)) + ".jpg");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            moviePosterBitmap = BitmapFactory.decodeStream(fis);
            poster.setImageBitmap(moviePosterBitmap);
            return result;
        }

        public long getItemId(int position) {
            cursor.moveToPosition(position);
            return cursor.getLong(cursor.getColumnIndex(KEY_ID));
        }
    }

    public class AsyncMovieInfoAPI extends AsyncTask<String, Integer, String> {

        private String response, title, year, rated, runtime, actors, plot, poster;
        private String urlString = "http://www.omdbapi.com/?t=" + movieTitle.replaceAll(" ", "-") + "&r=xml&apikey=e440ee78";
        private Bitmap moviePoster;
        HttpUtils httpUtils = new HttpUtils();
        private String posterUrl;

        @Override
        protected String doInBackground(String... args) {

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream urlResponse = urlConnection.getInputStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(urlResponse, "UTF-8");

                while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                    switch (xpp.getEventType()) {
                        case XmlPullParser.START_TAG:
                            String name = xpp.getName();
                            if (name.equals("root")) {
                                response = xpp.getAttributeValue(null, "response");
                                if (response.equals("False")) {
                                    return "";
                                }
                            } else if (name.equals("movie")) {
                                title = xpp.getAttributeValue(null, "title").replace("/", "~");
                                year = xpp.getAttributeValue(null, "year");
                                publishProgress(25);
                                rated = xpp.getAttributeValue(null, "rated");
                                runtime = xpp.getAttributeValue(null, "runtime");
                                publishProgress(50);
                                actors = xpp.getAttributeValue(null, "actors");
                                plot = xpp.getAttributeValue(null, "plot");
                                poster = xpp.getAttributeValue(null, "poster");
                                publishProgress(75);
                            }
                            Log.i("read XML tag:", name);
                            break;

                        case XmlPullParser.TEXT:
                            break;
                    }
                    xpp.next();
                }

                posterUrl = poster;
                poster = title + ".jpg";
                if (fileExistance(poster)) {
                    FileInputStream fis = null;
                    try {
                        fis = openFileInput(poster);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    moviePoster = BitmapFactory.decodeStream(fis);
                    Log.i("Looking For Filename:", poster);
                    Log.i("Image Found: ", "locally");
                } else {
                    moviePoster = httpUtils.getImage(posterUrl);
                    FileOutputStream outputStream = openFileOutput(poster, Context.MODE_PRIVATE);
                    moviePoster.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    Log.i("Looking For Filename:", poster);
                    Log.i("Image Found: ", "download");
                }
                publishProgress(100);

            } catch (Exception e) {
                Log.i("Exception", e.getMessage());
            }

            return "";
        }


        public boolean fileExistance(String fname){
            File file = getBaseContext().getFileStreamPath(fname);
            return file.exists();
        }

        @Override
        public void onProgressUpdate(Integer...value) {
            progressBar.setVisibility(View.VISIBLE);

            for(int i = 0; i < value.length; i++)
                progressBar.setProgress(value[i]);
        }

        @Override
        public void onPostExecute(String result) {
            progressBar.setVisibility(View.INVISIBLE);

            Bundle infoToPass = new Bundle();
            infoToPass.putString("title", title);
            infoToPass.putString("year", year);
            infoToPass.putString("rated", rated);
            infoToPass.putString("runtime", runtime);
            infoToPass.putString("actors", actors);
            infoToPass.putString("plot", plot);

            if (response != null) {
                if (response.equals("True")) {
                    Intent intent = new Intent(MovieInfo.this, MovieInfoDetails.class);
                    intent.putExtras(infoToPass);
                    searchText.setText("");
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    Snackbar.make(searchBtn, R.string.movie_not_found_snackbar, Snackbar.LENGTH_SHORT).show();
                    searchText.setText("");
                }
            }
        }
    }
}
