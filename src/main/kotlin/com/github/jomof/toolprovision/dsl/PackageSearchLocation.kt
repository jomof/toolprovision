package com.github.jomof.toolprovision.dsl

data class PackageSearchLocation(
        val pkg : String,
        val folder : String
) : SearchLocation()