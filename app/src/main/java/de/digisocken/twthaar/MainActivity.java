package de.digisocken.twthaar;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.UserTimeline;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    public ListView mListView;
    public ArrayList<Tweet> tweetArrayList;
    public TweetAdapter adapter;
    public ImageButton button;
    public EditText editText;
    public RelativeLayout searchBox;
    public Stack< String > history;

    private String iniQuery;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_toggleSearch:
                if (searchBox.getVisibility() == View.GONE) {
                    try {
                        ActionBar ab = getSupportActionBar();
                        if(ab != null) ab.setElevation(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    searchBox.setVisibility(View.VISIBLE);
                } else {
                    try {
                        ActionBar ab = getSupportActionBar();
                        if(ab != null) ab.setElevation(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    searchBox.setVisibility(View.GONE);
                }
                break;
            case R.id.action_flattr:
                Intent intentFlattr = new Intent(Intent.ACTION_VIEW, Uri.parse(TwthaarApp.FLATTR_LINK));
                startActivity(intentFlattr);
                break;
            case R.id.action_project:
                Intent intentProj= new Intent(Intent.ACTION_VIEW, Uri.parse(TwthaarApp.PROJECT_LINK));
                startActivity(intentProj);
                break;
            case R.id.action_preferences:
                Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            ActionBar ab = getSupportActionBar();
            if(ab != null) {
                ab.setDisplayShowHomeEnabled(true);
                ab.setHomeButtonEnabled(true);
                ab.setDisplayUseLogoEnabled(true);
                ab.setLogo(R.mipmap.ic_launcher);
                ab.setTitle(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
                ab.setElevation(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchBox = (RelativeLayout) findViewById(R.id.searchBox);
        mListView = (ListView) findViewById(R.id.list);
        button = (ImageButton) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editText);
        iniQuery = TwthaarApp.mPreferences.getString("STARTUSERS", getString(R.string.defaultStarts));
        editText.setText(iniQuery);
        button.setOnClickListener(new SearchListener(this, ""));

        tweetArrayList = new ArrayList<>();
        history = new Stack<>();
        history.push(iniQuery);
        adapter = new TweetAdapter(this, tweetArrayList);
        mListView.setAdapter(adapter);

        button.callOnClick();
    }


    public void sortTweetArrayList() {
        Collections.sort(tweetArrayList, new Comparator<Tweet>() {
            @Override
            public int compare(Tweet t2, Tweet t1) {
                try {
                    Date parsedDate1 = TwthaarApp.formatIn.parse(t1.createdAt);
                    Date parsedDate2 = TwthaarApp.formatIn.parse(t2.createdAt);
                    return parsedDate1.compareTo(parsedDate2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (history.size() > 0) {
            editText.setText(history.pop());
            button.callOnClick();
        } else {
            history.push(iniQuery);
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(String.format(getString(R.string.closing), getString(R.string.app_name)))
                    .setMessage(getString(R.string.sureToClose))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
    }
}
