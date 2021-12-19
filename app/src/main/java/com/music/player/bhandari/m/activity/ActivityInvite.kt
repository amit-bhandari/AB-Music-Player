package com.music.player.bhandari.m.activity

import android.R
import android.content.Context
import android.os.Handler
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.model.Constants

/**
 * Copyright 2017 Amit Bhandari AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class ActivityInvite constructor() : AppCompatActivity(), OnRefreshListener {
    @BindView(R.id.invite_button)
    var inviteButton: AppCompatButton? = null

    @BindView(R.id.invite_button_layout)
    var inviteButtonLayout: View? = null

    @BindView(R.id.invited_people_layout)
    var invitedPeopleLayout: View? = null

    @BindView(R.id.recyclerView)
    var recyclerView: RecyclerView? = null

    @BindView(R.id.swipeRefreshLayout)
    var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var handler: Handler? = null
    private var adapter: SentInvitationAdapter? = null
    private val REQUEST_INVITE: Int = 10
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (MyApp.Companion.getService() == null) {
            UtilityFun.restartApp()
        }
        ColorHelper.setStatusBarGradiant(this)
        val themeSelector: Int = MyApp.Companion.getPref()
            .getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        setContentView(R.layout.activity_invite)
        ButterKnife.bind(this)
        swipeRefreshLayout.setOnRefreshListener(this)
        handler = Handler(getMainLooper())
        adapter = SentInvitationAdapter()

        //findViewById(R.id.root_view_invite).setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar_)
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true)
            getSupportActionBar().setDisplayShowHomeEnabled(true)
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/
        val items: List<InvitationItem>? = GetInvitedItems()
        if (items != null && items.size != 0) {
            invitedPeopleLayout!!.setVisibility(View.VISIBLE)
            inviteButtonLayout!!.setVisibility(View.INVISIBLE)
            setUpRecyclerView()
        } else {
            invitedPeopleLayout!!.setVisibility(View.INVISIBLE)
            inviteButtonLayout!!.setVisibility(View.VISIBLE)
        }
        setTitle(getString(R.string.invite_friends_title))
    }

    private fun GetInvitedItems(): MutableList<InvitationItem>? {
        val json: String =
            MyApp.Companion.getPref().getString(getString(R.string.pref_sent_invittions), "")
        var items: MutableList<InvitationItem>? = Gson().fromJson<List<InvitationItem>>(json,
            object : TypeToken<List<InvitationItem?>?>() {}.getType())
        if (items == null) {
            items = ArrayList<InvitationItem>()
        }
        return items
    }

    private fun putInvitationItems(items: List<InvitationItem>?) {
        MyApp.Companion.getPref().edit()
            .putString(getString(R.string.pref_sent_invittions), Gson().toJson(items)).apply()
    }

    @OnClick(R.id.invite_button)
    fun invite() {
        val intent: Intent = AppInviteInvitation.IntentBuilder("Send invitation for AB Music")
            .setMessage("I have been using this amazing music player with instant lyrics feature, Give it a try.")
            .setDeepLink(Uri.parse("https://ddhk8.app.goo.gl/H3Ed"))
            .setCallToActionText("Get AB Music Now")
            .build()
        startActivityForResult(intent, REQUEST_INVITE)
    }

    @OnClick(R.id.invite_more_button)
    fun inviteMore() {
        invite()
    }

    private fun setUpRecyclerView() {
        recyclerView.setLayoutManager(LinearLayoutManager(this))
        recyclerView.setAdapter(adapter)
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_INVITE) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the invitation IDs of all sent messages
                val ids: Array<String> = AppInviteInvitation.getInvitationIds(resultCode, data)
                val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                val myRef: DatabaseReference = database.getReference("invites")
                val items: MutableList<InvitationItem>? = GetInvitedItems()
                for (id: String in ids) {
                    Log.d("ActivityMain", "onActivityResult: sent invitation " + id)
                    items!!.add(InvitationItem(id, false))
                    myRef.child(id).setValue(false)
                }
                putInvitationItems(items)
                adapter!!.refreshInvitationStatus()
                setUpRecyclerView()
                inviteButtonLayout!!.setVisibility(View.INVISIBLE)
                invitedPeopleLayout!!.setVisibility(View.VISIBLE)
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
                Toast.makeText(this, R.string.error_invitation_not_sent, Toast.LENGTH_SHORT).show()
            }
        }
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onRefresh() {
        if (adapter != null) {
            adapter!!.refreshInvitationStatus()
        }
    }

    protected override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    internal inner class SentInvitationAdapter private constructor() :
        RecyclerView.Adapter<SentInvitationAdapter.MyViewHolder?>() {
        private var invitationItems: List<InvitationItem>? = null
        fun refreshInvitationStatus() {
            invitationItems = GetInvitedItems()
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val myRef: DatabaseReference = database.getReference("invites")
            var position: Int = 0
            for (invitationItem: InvitationItem in invitationItems) {
                val finalPosition: Int = position
                val listener: ValueEventListener = object : ValueEventListener {
                    var pos: Int = finalPosition
                    public override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.getValue() == null) return
                        if (UtilityFun.isAdsRemoved) {
                            finish()
                            return
                        }
                        val status: Boolean = dataSnapshot.getValue()
                        //remove ads and exit the activity
                        if (status) {
                            MyApp.Companion.getPref().edit()
                                .putBoolean(getString(R.string.pref_remove_ads_after_payment), true)
                                .apply()
                            val ref: DatabaseReference =
                                FirebaseDatabase.getInstance().getReference("referrals_installs")
                            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                public override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {
                                        if (dataSnapshot.getValue() == null) {
                                            ref.setValue(1L)
                                        } else {
                                            ref.setValue(dataSnapshot.getValue() as Long + 1L)
                                        }
                                    } catch (ignored: Exception) {
                                    }
                                }

                                public override fun onCancelled(databaseError: DatabaseError) {}
                            })
                            handler!!.post(object : Runnable {
                                public override fun run() {
                                    Toast.makeText(getApplicationContext(),
                                        getString(R.string.ads_removed),
                                        Toast.LENGTH_LONG).show()
                                    Toast.makeText(getApplicationContext(),
                                        getString(R.string.ads_still_showing),
                                        Toast.LENGTH_LONG).show()
                                }
                            })
                            finish()
                        }
                        invitationItems!!.get(pos).invitationAccepted = status
                        handler!!.post(object : Runnable {
                            public override fun run() {
                                notifyItemChanged(pos)
                                swipeRefreshLayout.setRefreshing(false)
                            }
                        })
                    }

                    public override fun onCancelled(databaseError: DatabaseError) {
                        handler!!.post(object : Runnable {
                            public override fun run() {
                                Toast.makeText(this@ActivityInvite,
                                    "Error while retrieving invitation status, write me if problem persists",
                                    Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
                myRef.child(invitationItem.invitationId).addListenerForSingleValueEvent(listener)
                position++
            }
        }

        public override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view: View
            view = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.item_invite, parent, false)
            return MyViewHolder(view)
        }

        public override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.invitationId.setText(invitation + (position + 1))
            if (invitationItems!!.get(position).invitationAccepted) {
                holder.status.setImageDrawable(getResources().getDrawable(R.drawable.ic_cloud_done_black_24dp))
            } else {
                holder.status.setImageDrawable(getResources().getDrawable(R.drawable.ic_access_time_black_24dp))
            }
        }

        public override fun getItemCount(): Int {
            return invitationItems!!.size
        }

        internal inner class MyViewHolder constructor(itemView: View?) :
            RecyclerView.ViewHolder(itemView) {
            @BindView(R.id.invite_id)
            var invitationId: TextView? = null

            @BindView(R.id.invitation_status)
            var status: ImageView? = null

            init {
                ButterKnife.bind(this, itemView)
            }
        }

        init {
            refreshInvitationStatus()
        }
    }

    companion object {
        private val invitation: String = "Invitation "
    }
}