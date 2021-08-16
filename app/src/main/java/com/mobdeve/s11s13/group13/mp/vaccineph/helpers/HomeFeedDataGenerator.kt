package com.mobdeve.s11s13.group13.mp.vaccineph.helpers

object HomeFeedDataGenerator {

    fun generateData() : MutableList<HomeFeedData> {
        val feedDataList = mutableListOf<HomeFeedData>()

        var temp = "Keep your mask on at all times.\n" +
                "Don\'t touch your mask once it\'s on and properly fitted.\n" +
                "Keep at least 1 metre distance between yourself and others.\n" +
                "Sanitize or wash your hands after touching door handles, surfaces or furniture.\n" +
                "Don’t touch your face."
        temp = temp.replace("\n", "\n\n")

        feedDataList.add(
            HomeFeedData("At the Vaccine Center1", temp)
        )
        feedDataList.add(
            HomeFeedData("At the Vaccine Center2", temp)
        )
        feedDataList.add(
            HomeFeedData("At the Vaccine Center3", temp)
        )
        return feedDataList
    }
}