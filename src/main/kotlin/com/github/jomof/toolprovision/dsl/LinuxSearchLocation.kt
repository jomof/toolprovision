package com.github.jomof.toolprovision.dsl

enum class LinuxSearchLocationType {
    Path
}

data class LinuxSearchLocation(
        val type: LinuxSearchLocationType,
        val folder: String
) : SearchLocation()