package com.ds.liverecorder.ui.model

import java.io.Serializable

data class Performer(
    val id: Long = 0,
    val name: String = "",
    val concerts: List<Concert> = listOf()
) : Serializable