package com.github.jomof.toolprovision

import com.github.jomof.toolprovision.dsl.*
import java.io.File

class ProvisionScope(
        private val isWindows : Boolean,
        private val isFile : (File) -> Boolean,
        private val listFolders : (File) -> List<File>) {

    private fun expand(folder : File, segments : List<String>) : List<File> {
        if (segments.isEmpty()) {
            if (!isFile(folder)) {
                return listOf()
            }
            return listOf(folder)
        }
        val next = segments[0]
        val remaining = segments.drop(1)
        return listFolders(folder)
                .flatMap { sub -> expand(File(sub, next), remaining) }
    }

    private fun file(left : String, right : File) : File {
        return File(left, right.toString())
    }

    private fun expand(path : File) : List<File> {
        val split = path.toString().split("*")
        return expand(File(split[0]), split.drop(1))
    }

    private fun provision(exe : String, path : File) : List<File> {
        val exeName = exe + if (isWindows) ".exe" else ""
        return expand(File(path, exeName))
    }

    private fun provision(exe : String, folder : File, search : WindowsSearchLocation) : List<File> {
        if (!isWindows) return listOf()
        val sub = file(search.folder, folder)
        return when(search.type) {
            WindowsSearchLocationType.AppData ->
                provision(exe, file(System.getenv("LOCALAPPDATA"), sub))
            WindowsSearchLocationType.ProgramFiles ->
                provision(exe, file(System.getenv("ProgramFiles"), sub)) +
                        provision(exe, file(System.getenv("ProgramFiles(x86)"), sub))
        }
    }

    private fun provision(exe : String, folder : File, search : PackageSearchLocation, provisioning : ProvisionDef) : List<File> {
        return provisioning.packages.flatMap { pkg ->
            if (pkg.name == search.pkg) {
                provision(exe, file(search.folder, folder), pkg.search, provisioning)
            } else {
                listOf()
            }
        }
    }

    private fun provision(exe : String, folder : File, search : SearchLocation, provisioning : ProvisionDef) : List<File> {
        return when(search) {
            is WindowsSearchLocation -> provision(exe, folder, search)
            is PackageSearchLocation -> provision(exe, folder, search, provisioning)
            else -> throw RuntimeException()
        }
    }

    private fun provision(exe: String, folder : File, search: List<SearchLocation>, provisioning: ProvisionDef): List<File> {
        return search.flatMap { location ->
            provision(exe, folder, location, provisioning)
        }
    }

    private fun provision(exe : String, search : SearchLocation, provisioning : ProvisionDef) : List<File> {
        return when(search) {
            is WindowsSearchLocation -> provision(exe, File(""), search)
            is PackageSearchLocation -> provision(exe, File(""), search, provisioning)
            else -> throw RuntimeException()
        }
    }

    private fun provision(exe : String, search : List<SearchLocation>, provisioning : ProvisionDef) : List<File> {
        return search.flatMap { location ->
            provision(exe, location, provisioning)
        }
    }

    private fun provision(exe : String, tool : ToolDef, provisioning : ProvisionDef) : List<File> {
        if (exe != tool.exe) return listOf()
        return provision(exe, tool.search, provisioning)
    }

    fun provision(exe : String) : List<File> {
        val provisioning = createProvisioning()
        return provisioning.tools.flatMap { tool ->
            provision(exe, tool, provisioning)
        }.toSet().toList()
    }
}