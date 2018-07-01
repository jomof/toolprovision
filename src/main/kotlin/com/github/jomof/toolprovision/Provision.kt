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

fun provision(exe : String) : List<File> {
    return ProvisionScope(
            isWindows,
            { file -> file.isFile },
            { folder -> folder.listFiles().filter {
                it.isDirectory
            }}
    ).provision(exe)
}