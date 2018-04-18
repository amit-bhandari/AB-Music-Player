package com.music.player.bhandari.m.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.InvitationItem;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Amit AB AB on 27-Feb-18.
 */

public class ActivityInvite extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.invite_button)
    AppCompatButton inviteButton;

    @BindView(R.id.invite_button_layout)
    View inviteButtonLayout;

    @BindView(R.id.invited_people_layout)
    View invitedPeopleLayout;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private Handler handler;
    private SentInvitationAdapter adapter;

    final private int REQUEST_INVITE = 10;

    private static final String invitation = "Invitation ";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(MyApp.getService()==null){
            Intent intent = new Intent(this, ActivityPermissionSeek.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }

        int themeSelector = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT);
        switch (themeSelector){
            case Constants.PRIMARY_COLOR.DARK:
                setTheme(R.style.AppThemeDark);
                break;

            case Constants.PRIMARY_COLOR.GLOSSY:
                setTheme(R.style.AppThemeDark);
                break;

            case Constants.PRIMARY_COLOR.LIGHT:
                setTheme(R.style.AppThemeLight);
                break;
        }
        setContentView(R.layout.activity_invite);
        ButterKnife.bind(this);
        swipeRefreshLayout.setOnRefreshListener(this);
        handler = new Handler(getMainLooper());
        adapter = new SentInvitationAdapter();

        findViewById(R.id.root_view_invite).setBackgroundDrawable(ColorHelper.getColoredThemeGradientDrawable());

        Toolbar toolbar = findViewById(R.id.toolbar_);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ColorHelper.getPrimaryColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.getDarkPrimaryColor());
        }

        List<InvitationItem> items = GetInvitedItems();
        if(items!=null && items.size()!=0){
            invitedPeopleLayout.setVisibility(View.VISIBLE);
            inviteButtonLayout.setVisibility(View.INVISIBLE);
            setUpRecyclerView();
        }else {
            invitedPeopleLayout.setVisibility(View.INVISIBLE);
            inviteButtonLayout.setVisibility(View.VISIBLE);
        }

        setTitle(getString(R.string.invite_friends_title));
    }

    private List<InvitationItem> GetInvitedItems() {
        String json = MyApp.getPref().getString(getString(R.string.pref_sent_invittions), "");
        List<InvitationItem> items = new Gson().fromJson(json, new TypeToken<List<InvitationItem>>() {
        }.getType());
        if(items==null){
            items = new ArrayList<>();
        }
        return items;
    }

    private void putInvitationItems(List<InvitationItem> items) {
        MyApp.getPref().edit().putString(getString(R.string.pref_sent_invittions), new Gson().toJson(items)).apply();
    }

    @OnClick(R.id.invite_button)
    void invite(){
        Intent intent = new AppInviteInvitation.IntentBuilder("Send invitation for AB Music")
                .setMessage("I have been using this amazing music player with instant lyrics feature, Give it a try.")
                .setDeepLink(Uri.parse("https://ddhk8.app.goo.gl/H3Ed"))
                .setCallToActionText("Get AB Music Now")
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @OnClick(R.id.invite_more_button)
    void inviteMore(){
        invite();
    }

    private void setUpRecyclerView(){
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference("invites");

                List<InvitationItem> items = GetInvitedItems();
                for (String id : ids) {
                    Log.d("ActivityMain", "onActivityResult: sent invitation " + id);
                    items.add(new InvitationItem(id, false));
                    myRef.child(id).setValue(false);
                }
                putInvitationItems(items);
                adapter.refreshInvitationStatus();
                setUpRecyclerView();
                inviteButtonLayout.setVisibility(View.INVISIBLE);
                invitedPeopleLayout.setVisibility(View.VISIBLE);
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
                Toast.makeText(this, R.string.error_invitation_not_sent, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        if(adapter!=null){
            adapter.refreshInvitationStatus();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    class SentInvitationAdapter extends RecyclerView.Adapter<SentInvitationAdapter.MyViewHolder> {

        private List<InvitationItem> invitationItems;

        private SentInvitationAdapter(){
            refreshInvitationStatus();
        }

        private void refreshInvitationStatus(){
            invitationItems = GetInvitedItems();
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference("invites");

            int position = 0;
            for(final InvitationItem invitationItem: invitationItems){
                final int finalPosition = position;
                ValueEventListener listener = new ValueEventListener() {
                   int pos = finalPosition;
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.getValue()==null) return;

                        if(UtilityFun.isAdsRemoved()) {
                            finish();
                            return;
                        }

                        boolean status = (boolean)dataSnapshot.getValue();
                        //remove ads and exit the activity
                        if(status){
                            MyApp.getPref().edit().putBoolean(getString(R.string.pref_remove_ads_after_payment), true).apply();
                            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("referrals_installs");
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    try {
                                        if (dataSnapshot.getValue() == null) {
                                            ref.setValue(1L);
                                        } else {
                                            ref.setValue((Long) dataSnapshot.getValue() + 1L);
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), getString(R.string.ads_removed),Toast.LENGTH_LONG).show();
                                    Toast.makeText(getApplicationContext(), getString(R.string.ads_still_showing),Toast.LENGTH_LONG).show();
                                }
                            });
                            finish();
                        }
                        invitationItems.get(pos).invitationAccepted = status;

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                notifyItemChanged(pos);
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ActivityInvite.this, "Error while retrieving invitation status, write me if problem persists", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                };
                myRef.child(invitationItem.invitationId).addListenerForSingleValueEvent(listener);
                position++;
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_invite, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

            holder.invitationId.setText(invitation + (position+1));
            if(invitationItems.get(position).invitationAccepted) {
                holder.status.setImageDrawable(getResources().getDrawable(R.drawable.ic_cloud_done_black_24dp));
            }else {
                holder.status.setImageDrawable(getResources().getDrawable(R.drawable.ic_access_time_black_24dp));
            }
        }

        @Override
        public int getItemCount() {
            return invitationItems.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder{
            @BindView(R.id.invite_id)
            TextView invitationId;

            @BindView(R.id.invitation_status)
            ImageView status;

            MyViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

}
