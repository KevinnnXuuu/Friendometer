package com.mistershorr.databases;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FriendListActivity extends AppCompatActivity {

    public ListView listViewFriends;
    private List<Friend> friendsList;
    public ArrayAdapter<Friend> friendAdapter;
    public FloatingActionButton buttonAdd;
    public static final String EXTRA_FRIEND = "Friend";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        wireWidgets();

        listViewFriends = findViewById(R.id.listview_friendlist_list);
        // search only for friends that have ownerId that match the user's objectId
        String userId = Backendless.UserService.CurrentUser().getObjectId();
        String whereClause = "ownerId = '" + userId + "'";

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);


        Backendless.Data.of(Friend.class).find(queryBuilder, new AsyncCallback<List<Friend>>() {
            @Override
            public void handleResponse(List<Friend> foundFriends) {
                // first contact instance has been found
                Log.d("LOADED FRIENDS", "handleResponse: " + foundFriends.toString());
                friendsList = foundFriends;
                ArrayAdapter adapter = new FriendAdapter(friendsList);
                friendAdapter = adapter;
                listViewFriends.setAdapter(adapter);
                // TODO: make a custom adapter to display the friends and load the list that
                // is retrieved into that adapter

                // TODO make friend parcelable
                // TODO when a friend is clicked, it opens the detail activity and loads the info
                setListeners();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // an error has occurred, the error code can be retrieved with fault.getCode()
                Toast.makeText(FriendListActivity.this,
                        fault.getDetail(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        listViewFriends = findViewById(R.id.listview_friendlist_list);
        // search only for friends that have ownerId that match the user's objectId
        String userId = Backendless.UserService.CurrentUser().getObjectId();
        String whereClause = "ownerId = '" + userId + "'";

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);


        Backendless.Data.of(Friend.class).find(queryBuilder, new AsyncCallback<List<Friend>>() {
            @Override
            public void handleResponse(List<Friend> foundFriends) {
                // first contact instance has been found
                Log.d("LOADED FRIENDS", "handleResponse: " + foundFriends.toString());
                friendsList = foundFriends;
                ArrayAdapter adapter = new FriendAdapter(friendsList);
                friendAdapter = adapter;
                listViewFriends.setAdapter(adapter);
                // TODO: make a custom adapter to display the friends and load the list that
                // is retrieved into that adapter

                // TODO make friend parcelable
                // TODO when a friend is clicked, it opens the detail activity and loads the info
                setListeners();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // an error has occurred, the error code can be retrieved with fault.getCode()
                Toast.makeText(FriendListActivity.this,
                        fault.getDetail(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setListeners() {
        listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Friend friend = friendsList.get(i);
                Intent FriendIntent = new Intent(FriendListActivity.this, FriendDetailActivity.class);
                FriendIntent.putExtra(EXTRA_FRIEND, friend);
                startActivity(FriendIntent);
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent FriendIntent = new Intent(FriendListActivity.this, FriendDetailActivity.class);
                startActivity(FriendIntent);
            }
        });
        listViewFriends.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                return false;
            }
        });
        registerForContextMenu(listViewFriends);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.delete_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case (R.id.item_delete):
                Friend friend = friendsList.get(info.position);
                deleteFriend(friend);
                friendsList.remove(info.position);
                friendAdapter.notifyDataSetChanged();
        }
        return super.onContextItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_name:
                Log.d("SORT", "onOptionsItemSelected: " + friendsList);
                Collections.sort(friendsList);
                friendAdapter.notifyDataSetChanged();
                return true;
            case R.id.sort_money:
                Collections.sort(friendsList, new Comparator<Friend>() {
                    @Override
                    public int compare(Friend friend, Friend t1) {
                        return (int) (friend.getMoneyOwed() - t1.getMoneyOwed());
                    }
                });
                friendAdapter.notifyDataSetChanged();
                return true;
            case R.id.log_out:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void wireWidgets() {
        buttonAdd = findViewById(R.id.floatingactionbutton_list_add);
    }

    private class FriendAdapter extends ArrayAdapter<Friend> {

        private List<Friend> friendsList;

        public FriendAdapter(List<Friend> friendsList) {
            super(FriendListActivity.this, -1, friendsList);
            this.friendsList = friendsList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 1. inflate a layout
            LayoutInflater inflater = getLayoutInflater();

            // check if convertview is null, if so, replace it
            if (convertView == null) {

                convertView = inflater.inflate(R.layout.item_friend, parent, false);
            }

            // 2. wire widgets & link the hero to those widgets
            TextView textViewName = convertView.findViewById(R.id.textview_item_name);
            TextView textViewClumsiness = convertView.findViewById(R.id.textview_item_clumsiness);
            TextView textViewMoneyOwed = convertView.findViewById(R.id.textview_item_owed);

            textViewName.setText(friendsList.get(position).getName());
            textViewClumsiness.setText(String.valueOf(friendsList.get(position).getClumsiness()));
            textViewMoneyOwed.setText("$" + (friendsList.get(position).getMoneyOwed()));

            // set the values for each widget. use the position parameter variable
            // to get the hero that you need out of the list
            // and set the values for widgets

            // 3. return inflated view
            return convertView;

        }
    }

    public void deleteFriend(Friend friend) {
        Backendless.Persistence.save(friend, new AsyncCallback<Friend>() {
            public void handleResponse(Friend savedFriend) {
                Backendless.Persistence.of(Friend.class).remove(savedFriend,
                        new AsyncCallback<Long>() {
                            public void handleResponse(Long response) {
                                Toast.makeText(FriendListActivity.this, "Friend Deleted", Toast.LENGTH_SHORT).show();
                            }

                            public void handleFault(BackendlessFault fault) {
                                Toast.makeText(FriendListActivity.this, fault.getCode(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(FriendListActivity.this, fault.getCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
