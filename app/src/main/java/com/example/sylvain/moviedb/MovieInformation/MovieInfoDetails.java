package com.example.sylvain.moviedb.MovieInformation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.sylvain.moviedb.R;

public class MovieInfoDetails extends AppCompatActivity {

    public AppCompatActivity getActivity() {
        return this;
    }

    private Context ctx;
    private Toolbar movieToolbar;
    private boolean searchedOrSaved = false;
    private boolean statsOrNot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_info_details);

        ctx = this;

        movieToolbar = findViewById(R.id.movieToolbar);
        movieToolbar.setTitle(R.string.movie);
        setSupportActionBar(movieToolbar);

        Bundle infoToPass = getIntent().getExtras();

        searchedOrSaved = infoToPass.getBoolean("saved");
        statsOrNot = infoToPass.getBoolean("statsOrNot");

        MovieInfoFragment fragment = new MovieInfoFragment();
        fragment.setArguments(infoToPass);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.addToBackStack("nothing really matters.");
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MovieInfoDetails.this, MovieInfo.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.movie_info_help:
                AlertDialog.Builder builder = new AlertDialog.Builder(MovieInfoDetails.this);
                if(searchedOrSaved) {
                    builder.setMessage(ctx.getString(R.string.movie_created_by) + "\n" + ctx.getString(R.string.movie_activity_version) + "\n" + ctx.getString(R.string.movie_instructions_saved_stats));
                } else if (statsOrNot){
                    builder.setMessage(ctx.getString(R.string.movie_created_by) + "\n" + ctx.getString(R.string.movie_activity_version) + "\n" + ctx.getString(R.string.movie_instructions_saved_stats));
                } else {
                    builder.setMessage(ctx.getString(R.string.movie_created_by) + "\n" + ctx.getString(R.string.movie_activity_version) + "\n" + ctx.getString(R.string.movie_instructions_search));
                }
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

    private void launchActivity(Class act){
        Intent intent = new Intent(MovieInfoDetails.this, act);
        startActivity(intent);
        getActivity().finish();
    }
}
