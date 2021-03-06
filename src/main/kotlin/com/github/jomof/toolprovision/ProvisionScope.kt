package com.github.jomof.toolprovision

import com.github.jomof.toolprovision.dsl.*
import com.github.jomof.toolprovision.dsl.WindowsSearchLocationType.AppData
import com.github.jomof.toolprovision.dsl.WindowsSearchLocationType.ProgramFiles

class ProvisionScope(
        private val isWindows : Boolean,
        private val getenv: (String) -> String?,
        private val isFile: (String) -> Boolean,
        private val listFolders: (String) -> List<String>) {

    private fun expand(folder: String, segments: List<String>): List<String> {
        if (segments.isEmpty()) {
            if (!isFile(folder)) {
                return listOf()
            }
            return listOf(folder)
        }
        val next = segments[0]
        val remaining = segments.drop(1)
        return listFolders(folder)
                .flatMap { sub -> expand(file(sub, next), remaining) }
    }

    private fun normalize(path: String): String {
        return path.replace("\\", "/").replace("//", "/")
    }

    private fun file(left: String, right: String): String {
        if (left.isEmpty()) return normalize(right)
        if (right.isEmpty()) return normalize(left)
        return normalize("$left/$right")
    }

    private fun expand(path: String): List<String> {
        val split = path.split("*")
        return expand(split[0], split.drop(1))
    }

    private fun provision(exe: String, path: String): List<String> {
        val exeName = exe + if (isWindows) ".exe" else ""
        return expand(file(path, exeName))
    }

    private fun provision(exe: String, variable: String, sub: String): List<String> {
        val value = getenv(variable) ?: return listOf()
        return provision(exe, file(value, sub))
    }

    private fun provision(exe: String, folder: String, search: WindowsSearchLocation): List<String> {
        if (!isWindows) return listOf()
        val sub = file(search.folder, folder)
        return when(search.type) {
            AppData ->
                provision(exe, "LOCALAPPDATA", sub)
            ProgramFiles -> {
                provision(exe, "ProgramFiles", sub) +
                        provision(exe, "ProgramFiles(x86)", sub)
            }
        }
    }

    private fun provision(exe: String, folder: String, search: LinuxSearchLocation): List<String> {
        if (isWindows) return listOf()
        val sub = file(search.folder, folder)
        return provision(exe, sub)
    }

    private fun provision(exe: String, folder: String, search: EnvironmentSearchLocation): List<String> {
        return provision(exe, search.variable, folder)
    }

    private fun provision(exe: String, folder: String, search: PackageSearchLocation, provisioning: ProvisionDef): List<String> {
        return provisioning.packages.flatMap { pkg ->
            if (pkg.name == search.pkg) {
                provision(exe, file(search.folder, folder), pkg.search, provisioning)
            } else {
                listOf()
            }
        }
    }

    private fun provision(exe: String, folder: String, search: SearchLocation, provisioning: ProvisionDef): List<String> {
        return when(search) {
            is EnvironmentSearchLocation -> provision(exe, folder, search)
            is WindowsSearchLocation -> provision(exe, folder, search)
            is LinuxSearchLocation -> provision(exe, folder, search)
            is PackageSearchLocation -> provision(exe, folder, search, provisioning)
            else -> throw RuntimeException(search.toString())
        }
    }

    private fun provision(exe: String, folder: String, search: List<SearchLocation>, provisioning: ProvisionDef): List<String> {
        return search.flatMap { location ->
            provision(exe, folder, location, provisioning)
        }
    }

    private fun provision(exe: String, search: List<SearchLocation>, provisioning: ProvisionDef): List<String> {
        return search.flatMap { location ->
            provision(exe, "", location, provisioning)
        }
    }

    private fun provision(exe: String, tool: ToolDef, provisioning: ProvisionDef): List<String> {
        if (exe != tool.exe) return listOf()
        return provision(exe, tool.search, provisioning)
    }

    fun provision(exe: String): List<String> {
        val provisioning = createProvisioning()
        return provisioning.tools.flatMap { tool ->
            provision(exe, tool, provisioning)
        }.toSet().toList()
    }
}