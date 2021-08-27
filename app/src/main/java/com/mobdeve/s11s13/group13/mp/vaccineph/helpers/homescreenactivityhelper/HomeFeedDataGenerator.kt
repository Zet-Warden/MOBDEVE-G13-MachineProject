package com.mobdeve.s11s13.group13.mp.vaccineph.helpers.homescreenactivityhelper

object HomeFeedDataGenerator {

    fun generateData() : MutableList<HomeFeedData> {
        val feedDataList = mutableListOf<HomeFeedData>()

        var info1 = "Keep your mask on at all times.\n" +
                "Don\'t touch your mask once it\'s on and properly fitted.\n" +
                "Keep at least 1 metre distance between yourself and others.\n" +
                "Sanitize or wash your hands after touching door handles, surfaces or furniture.\n" +
                "Don’t touch your face."
        info1 = info1.replace("\n", "\n\n")

        feedDataList.add(
            HomeFeedData("Before Vaccination", info1)
        )
        feedDataList.add(
            HomeFeedData("At the Vaccine Center", info1)
        )
        feedDataList.add(
            HomeFeedData("After Vaccination", info1)
        )
        return feedDataList
    }
}