package com.mobdeve.s11s13.group13.mp.vaccineph

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.HomeFeedData
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
        feedDataList = mutableListOf()
        feedDataList.add(
            HomeFeedData("General Guidelines", "Don't Die")
        )
        feedDataList.add(
            HomeFeedData("Vaccine Side Effects", "Death")
        )


        rvHomeScreen.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        feedAdapter = HomeScreenRvAdapter(feedDataList)
        rvHomeScreen.adapter = feedAdapter

//        val helper = PagerSnapHelper()
//        helper.attachToRecyclerView(rvHomeScreen)
    }
}