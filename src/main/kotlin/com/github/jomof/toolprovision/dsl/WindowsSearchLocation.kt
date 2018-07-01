package com.github.jomof.toolprovision.dsl

enum class WindowsSearchLocationType {
    AppData,
    ProgramFiles
}

data class WindowsSearchLocation(
        val type : WindowsSearchLocationType,
        val folder : String
) : SearchLocation()