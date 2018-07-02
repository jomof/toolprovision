package com.github.jomof.toolprovision

import com.github.jomof.toolprovision.dsl.*
import com.github.jomof.toolprovision.dsl.WindowsSearchLocationType.AppData
import com.github.jomof.toolprovision.dsl.WindowsSearchLocationType.ProgramFiles
import java.io.File

fun createProvisioning() =
        ProvisionDef(
                listOf(
                        ToolDef(
                                exe = "cmake",
                                search = listOf(
                                        PackageSearchLocation("CMake", "bin"))),
                        ToolDef(
                                exe = "ninja",
                                search = listOf(
                                        PackageSearchLocation("CMake", "bin"),
                                        PackageSearchLocation("Ninja", "")
                                )),
                        ToolDef(
                                exe = "clang++",
                                search = listOf())),
                listOf(
                        PackageDef(
                                name = "Ninja",
                                search = listOf(
                                        WindowsSearchLocation(ProgramFiles, "Microsoft Visual Studio/2017/Community/Common7/IDE/CommonExtensions/Microsoft/CMake/Ninja")
                                )),
                        PackageDef(
                                name = "CMake",
                                search = listOf(
                                        PackageSearchLocation("Android SDK", "cmake/*"),
                                        WindowsSearchLocation(ProgramFiles, "CMake"),
                                        WindowsSearchLocation(ProgramFiles, "Microsoft Visual Studio/2017/Community/Common7/IDE/CommonExtensions/Microsoft/CMake/CMake")
                                )),
                        PackageDef(
                                name = "Android NDK",
                                search = listOf(
                                        PackageSearchLocation("Android SDK", "ndk-bundle"))),
                        PackageDef(
                                name = "Android SDK",
                                search = listOf(
                                        WindowsSearchLocation(AppData, "/Android/Sdk")))))

private fun isFile(file: String) = File(file).isFile
private fun listFolders(folder: String): List<String> {
    return File(folder).listFiles().filter { it.isDirectory }.map { it.toString() }
}

private fun getenv(key: String) = System.getenv(key)

fun provision(exe: String): List<String> {
    return ProvisionScope(
            isWindows = isWindows,
            getenv = ::getenv,
            isFile = ::isFile,
            listFolders = ::listFolders
    ).provision(exe)
}