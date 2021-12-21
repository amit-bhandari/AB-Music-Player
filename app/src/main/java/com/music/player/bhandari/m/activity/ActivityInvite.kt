package com.music.player.bhandari.m.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.appinvite.AppInviteInvitation
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.InvitationItem
import com.music.player.bhandari.m.utils.UtilityFun
import io.github.inflationx.viewpump.ViewPumpContextWrapper

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
class ActivityInvite : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    @JvmField @BindView(R.id.invite_button)
    var inviteButton: AppCompatButton? = null

    @JvmField @BindView(R.id.invite_button_layout)
    var inviteButtonLayout: View? = null

    @JvmField @BindView(R.id.invited_people_layout)
    var invitedPeopleLayout: View? = null

    @JvmField @BindView(R.id.recyclerView)
    var recyclerView: RecyclerView? = null

    @JvmField @BindView(R.id.swipeRefreshLayout)
    var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var handler: Handler? = null
    private var adapter: SentInvitationAdapter? = null
    private val REQUEST_INVITE: Int = 10
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (MyApp.getService() == null) {
            UtilityFun.restartApp()
        }
        ColorHelper.setStatusBarGradiant(this)
        val themeSelector: Int = MyApp.getPref()
            .getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        setContentView(R.layout.activity_invite)
        ButterKnife.bind(this)
        swipeRefreshLayout!!.setOnRefreshListener(this)
        handler = Handler(mainLooper)
        adapter = SentInvitationAdapter()

        //findViewById(R.id.root_view_invite).setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());
        val toolbar: Toolbar = findViewById(R.id.toolbar_)
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (supportActionBar != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/
        val items = GetInvitedItems()
        if (items != null && items.size != 0) {
            invitedPeopleLayout!!.visibility = View.VISIBLE
            inviteButtonLayout!!.visibility = View.INVISIBLE
            setUpRecyclerView()
        } else {
            invitedPeopleLayout!!.visibility = View.INVISIBLE
            inviteButtonLayout!!.visibility = View.VISIBLE
        }
        title = getString(R.string.invite_friends_title)
    }

    private fun GetInvitedItems(): MutableList<InvitationItem>? {
        val json = MyApp.getPref().getString(getString(R.string.pref_sent_invittions), "")
        var items = Gson().fromJson<MutableList<InvitationItem>>(json, object : TypeToken<List<InvitationItem?>?>() {}.type)
        if (items == null) {
            items = ArrayList()
        }
        return items
    }

    private fun putInvitationItems(items: List<InvitationItem>?) {
        MyApp.getPref().edit()
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
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_INVITE) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the invitation IDs of all sent messages
                val ids: Array<String> = AppInviteInvitation.getInvitationIds(resultCode, data!!)
                val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                val myRef: DatabaseReference = database.getReference("invites")
                val items: MutableList<InvitationItem>? = GetInvitedItems()
                for (id: String in ids) {
                    Log.d("ActivityMain", "onActivityResult: sent invitation $id")
                    items!!.add(InvitationItem(id, false))
                    myRef.child(id).setValue(false)
                }
                putInvitationItems(items)
                adapter!!.refreshInvitationStatus()
                setUpRecyclerView()
                inviteButtonLayout!!.visibility = View.INVISIBLE
                invitedPeopleLayout!!.visibility = View.VISIBLE
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
                Toast.makeText(this, R.string.error_invitation_not_sent, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRefresh() {
        if (adapter != null) {
            adapter!!.refreshInvitationStatus()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    internal inner class SentInvitationAdapter :
        RecyclerView.Adapter<SentInvitationAdapter.MyViewHolder?>() {
        private var invitationItems: List<InvitationItem>? = null
        fun refreshInvitationStatus() {
            invitationItems = GetInvitedItems()
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val myRef: DatabaseReference = database.getReference("invites")
            for ((position, invitationItem: InvitationItem) in invitationItems!!.withIndex()) {
                val finalPosition: Int = position
                val listener: ValueEventListener = object : ValueEventListener {
                    var pos: Int = finalPosition
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.value == null) return
                        if (UtilityFun.isAdsRemoved) {
                            finish()
                            return
                        }
                        val status = dataSnapshot.value as Boolean
                        //remove ads and exit the activity
                        if (status) {
                            MyApp.getPref().edit()
                                .putBoolean(getString(R.string.pref_remove_ads_after_payment), true)
                                .apply()
                            val ref: DatabaseReference =
                                FirebaseDatabase.getInstance().getReference("referrals_installs")
                            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {
                                        if (dataSnapshot.value == null) {
                                            ref.setValue(1L)
                                        } else {
                                            ref.setValue(dataSnapshot.value as Long + 1L)
                                        }
                                    } catch (ignored: Exception) {
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                            handler!!.post {
                                Toast.makeText(applicationContext,
                                    getString(R.string.ads_removed),
                                    Toast.LENGTH_LONG).show()
                                Toast.makeText(applicationContext,
                                    getString(R.string.ads_still_showing),
                                    Toast.LENGTH_LONG).show()
                            }
                            finish()
                        }
                        invitationItems!![pos].invitationAccepted = status
                        handler!!.post {
                            notifyItemChanged(pos)
                            swipeRefreshLayout!!.isRefreshing = false
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        handler!!.post {
                            Toast.makeText(this@ActivityInvite,
                                "Error while retrieving invitation status, write me if problem persists",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                myRef.child(invitationItem.invitationId).addListenerForSingleValueEvent(listener)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view: View = LayoutInflater.from(applicationContext).inflate(R.layout.item_invite, parent, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.invitationId!!.text = invitation + (position + 1)
            if (invitationItems!![position].invitationAccepted) {
                holder.status!!.setImageDrawable(resources.getDrawable(R.drawable.ic_cloud_done_black_24dp))
            } else {
                holder.status!!.setImageDrawable(resources.getDrawable(R.drawable.ic_access_time_black_24dp))
            }
        }

        override fun getItemCount(): Int {
            return invitationItems!!.size
        }

        internal inner class MyViewHolder constructor(itemView: View?) :
            RecyclerView.ViewHolder(itemView!!) {
            @JvmField @BindView(R.id.invite_id)
            var invitationId: TextView? = null

            @JvmField @BindView(R.id.invitation_status)
            var status: ImageView? = null

            init {
                ButterKnife.bind(this, itemView!!)
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