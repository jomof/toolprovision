package com.github.jomof.toolprovision.dsl

data class ToolDef(
        val exe : String,
        val search : List<SearchLocation>
)