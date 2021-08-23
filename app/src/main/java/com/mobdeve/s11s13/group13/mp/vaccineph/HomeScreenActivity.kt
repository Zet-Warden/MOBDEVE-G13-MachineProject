package com.mobdeve.s11s13.group13.mp.vaccineph

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.homescreenactivityhelper.HomeFeedData
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.homescreenactivityhelper.HomeFeedDataGenerator
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.homescreenactivityhelper.HomeScreenRvAdapter
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.NavBarLinker
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.ViewLinker
import kotlinx.android.synthetic.main.activity_home_screen.*


class HomeScreenActivity : AppCompatActivity() {
    private lateinit var feedDataList: MutableList<HomeFeedData>
    private lateinit var feedAdapter: HomeScreenRvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        init()
    }

    /**
     * Initialize components of the activity
     */
    private fun init() {
        UIHider(this, clMainContainer)
        initRecyclerView()
        ViewLinker.linkViewsAndActivities(
            this,
            NavBarLinker.createNavBarLinkPairs(btnHome, btnProfile, btnLocation, btnCalendar)
        )
    }

    /**
     * Initializes the recycler view
     */
    private fun initRecyclerView() {
        feedDataList = HomeFeedDataGenerator.generateData()

        val layoutManager = object : LinearLayoutManager(this, HORIZONTAL, false) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                // change the width so it doesn't consume the entire RecyclerView Area (enables partial viewing)
                lp.width = (width / 1.25).toInt()
                return true
            }
        }
        rvHomeScreen.layoutManager = layoutManager
        feedAdapter = HomeScreenRvAdapter(feedDataList)
        rvHomeScreen.adapter = feedAdapter

        val helper = PagerSnapHelper()
        helper.attachToRecyclerView(rvHomeScreen)

        // get the 'first item' nearest to the middle of the RecyclerView
        var midPoint = Int.MAX_VALUE / 2
        midPoint -= (midPoint % feedDataList.size)

        // ensure that the first time content is shown, it is centered
        rvHomeScreen.scrollToPosition(midPoint - 1)
        rvHomeScreen.smoothScrollToPosition(midPoint)
    }
}