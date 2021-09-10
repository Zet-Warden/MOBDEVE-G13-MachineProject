package com.mobdeve.s11s13.group13.mp.vaccineph.helpers.homescreenactivityhelper

object HomeFeedDataGenerator {

    fun generateData() : MutableList<HomeFeedData> {
        val feedDataList = mutableListOf<HomeFeedData>()

        var info1 = "Carry a mask that covers your nose and mouth.\n" +
                "Prepare proof of your vaccine appointment as well as your valid ID.\n"
                "Wear loose or short sleeves that can be rolled up easily\n" +
                "Cancel your appointment if you have COVID-19 or the symptoms.\n"

        var info2 = "Keep your mask on at all times.\n" +
                "Don't touch your mask once it's on and properly fitted.\n" +
                "Keep at least 1 metre distance between yourself and others.\n" +
                "Sanitize or wash your hands after touching door handles, surfaces or furniture.\n" +
                "Don’t touch your face."

        var info3 = "Take care of yourself.\n" +
                "Avoid drinking alcoholic beverages.\n" +
                "Avoid smoking.\n" +
                "Care for the arm where your vaccine was injected.\n" +
                "Put your second vaccination appointment in your calendar.\n" +
                "Keep up preventive behaviours.\n"

        info1 = info1.replace("\n", "\n\n")
        info2 = info2.replace("\n", "\n\n")
        info3 = info3.replace("\n", "\n\n")

        feedDataList.add(
            HomeFeedData("Before Vaccination", info1)
        )
        feedDataList.add(
            HomeFeedData("At the Vaccine Center", info2)
        )
        feedDataList.add(
            HomeFeedData("After Vaccination", info3)
        )
        return feedDataList
    }
}