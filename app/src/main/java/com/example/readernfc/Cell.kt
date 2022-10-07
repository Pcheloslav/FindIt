package com.example.readernfc

data class Cell(
    val isRoad: Boolean, // Can be passed through irl
    val isEnd: Boolean, // Can be used as source/destination point on map
    val iconName: String,
    val cols: Int, // Number of rows of icon
    val rows: Int // Number of cols in icon
)
