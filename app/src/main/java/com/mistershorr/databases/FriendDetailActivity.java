package com.mistershorr.databases;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Rating;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.Collections;
import java.util.Comparator;

public class FriendDetailActivity extends AppCompatActivity {

    private EditText editTextName;
    private TextView textViewClumsinessTitle;
    private SeekBar seekBarClumsiness;
    private Switch aSwitchAwesome;
    private TextView textViewGymTitle;
    private SeekBar seekBarGym;
    private TextView textViewTrustTitle;
    private RatingBar ratingBarTrustworthiness;
    private EditText editTextOwed;
    private Button buttonSave;
    private Button buttonDelete;
    private Friend friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);
        wireWidgets();
        Intent lastIntent = getIntent();
        friend = lastIntent.getParcelableExtra(FriendListActivity.EXTRA_FRIEND);
        if (friend != null) {
            editTextName.setText(friend.getName());
            seekBarClumsiness.setProgress(friend.getClumsiness());
            aSwitchAwesome.setChecked(friend.isAwesome());
            seekBarGym.setProgress((int) friend.getGymFrequency());
            ratingBarTrustworthiness.setRating((float) ((double) friend.getTrustworthiness()) / 2);
            editTextOwed.setText(String.valueOf(friend.getMoneyOwed()));
        } else {
            friend = new Friend();
            friend.setOwnerId(Backendless.UserService.CurrentUser().getObjectId());
        }
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNewFriend();
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFriend();
            }
        });
    }

    public void saveNewFriend() {
        friend.setName(editTextName.getText().toString());
        friend.setClumsiness(seekBarClumsiness.getProgress());
        friend.setGymFrequency(seekBarGym.getProgress());
        friend.setAwesome(aSwitchAwesome.isChecked());
        friend.setTrustworthiness((int) (ratingBarTrustworthiness.getRating() * 2));
        friend.setMoneyOwed(Double.valueOf(editTextOwed.getText().toString()));


        // save object asynchronously
        Backendless.Persistence.save(friend, new AsyncCallback<Friend>() {
            public void handleResponse(Friend response) {
                // new Contact instance has been saved
                Toast.makeText(FriendDetailActivity.this, "Friend Saved", Toast.LENGTH_SHORT).show();
                finish();
            }

            public void handleFault(BackendlessFault fault) {
                // an error has occurred, the error code can be retrieved with fault.getCode()
                Toast.makeText(FriendDetailActivity.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteFriend() {
        Backendless.Persistence.save(friend, new AsyncCallback<Friend>() {
            public void handleResponse(Friend savedFriend) {
                Backendless.Persistence.of(Friend.class).remove(savedFriend,
                        new AsyncCallback<Long>() {
                            public void handleResponse(Long response) {
                                Toast.makeText(FriendDetailActivity.this, "Friend Deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            public void handleFault(BackendlessFault fault) {
                                Toast.makeText(FriendDetailActivity.this, fault.getCode(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(FriendDetailActivity.this, fault.getCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void wireWidgets() {
        editTextName = findViewById(R.id.edittext_detail_name);
        textViewClumsinessTitle = findViewById(R.id.textview_detail_clumsiness);
        seekBarClumsiness = findViewById(R.id.seekbar_detail_clumsiness);
        aSwitchAwesome = findViewById(R.id.switch_detail_awesome);
        textViewGymTitle = findViewById(R.id.textview_detail_gym);
        seekBarGym = findViewById(R.id.seekbar_detail_gym);
        textViewTrustTitle = findViewById(R.id.textview_detail_trustworthiness);
        ratingBarTrustworthiness = findViewById(R.id.ratingbar_detail_trustworthiness);
        editTextOwed = findViewById(R.id.edittext_detail_owed);
        buttonSave = findViewById(R.id.button_detail_save);
        buttonDelete = findViewById(R.id.button_detail_delete);
    }
}
