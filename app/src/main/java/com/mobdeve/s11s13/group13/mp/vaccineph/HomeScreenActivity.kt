package com.mobdeve.s11s13.group13.mp.vaccineph

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.HomeFeedData
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.HomeFeedDataGenerator
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.HomeScreenRvAdapter
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import kotlinx.android.synthetic.main.activity_home_screen.*


class HomeScreenActivity : AppCompatActivity() {
    private lateinit var feedDataList: MutableList<HomeFeedData>
    private lateinit var feedAdapter: HomeScreenRvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        UIHider(this, clMainContainer)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        feedDataList = HomeFeedDataGenerator.generateData()

        val layoutManager = object : LinearLayoutManager(this, HORIZONTAL, false) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                lp.width = (width / 1.25).toInt() // change the width so it doesn't consume the entire RecyclerView Area (enables partial viewing)
                return true
            }
        }
        rvHomeScreen.layoutManager = layoutManager
        feedAdapter = HomeScreenRvAdapter(feedDataList)
        rvHomeScreen.adapter = feedAdapter

        val helper = PagerSnapHelper()
        helper.attachToRecyclerView(rvHomeScreen)

        //get the 'first item' nearest to the middle of the RecyclerView
        var midPoint = Int.MAX_VALUE / 2
        midPoint -= (midPoint % feedDataList.size)

        rvHomeScreen.scrollToPosition(midPoint - 1)
        rvHomeScreen.smoothScrollToPosition(midPoint)
    }
}