package com.github.jomof.toolprovision.dsl

data class ProvisionDef(
        val tools : List<ToolDef>,
        val packages : List<PackageDef>
)