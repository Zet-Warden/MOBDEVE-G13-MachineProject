package com.mobdeve.s11s13.group13.mp.vaccineph

import android.opengl.ETC1
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
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

        var temp = "Keep your mask on at all times.\n" +
                "Don\'t touch your mask once it\'s on and properly fitted.\n" +
                "Keep at least 1 metre distance between yourself and others.\n" +
                "Sanitize or wash your hands after touching door handles, surfaces or furniture.\n" +
                "Don’t touch your face."
        temp = temp.replace("\n", "\n\n")

        feedDataList.add(
            HomeFeedData("At the Vaccine Center", temp)
        )
        feedDataList.add(
            HomeFeedData("At the Vaccine Center", temp)
        )
        feedDataList.add(
            HomeFeedData("At the Vaccine Center", temp)
        )

        val layoutManager = object : LinearLayoutManager(this, HORIZONTAL, false) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                // force height of viewHolder here, this will override layout_height from xml
                lp.width = (width / 1.2).toInt()
                return true
            }
        }

        //rvHomeScreen.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvHomeScreen.layoutManager = layoutManager
        feedAdapter = HomeScreenRvAdapter(feedDataList)
        rvHomeScreen.adapter = feedAdapter

        val helper = PagerSnapHelper()
        helper.attachToRecyclerView(rvHomeScreen)
    }
}