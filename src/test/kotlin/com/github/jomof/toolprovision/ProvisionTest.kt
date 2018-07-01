package com.github.jomof.toolprovision

import org.junit.Test
import java.io.File

class ProvisionTest {
    class ProvisionRecorder {
        private val isFileCalls = mutableMapOf<File, Boolean>()
        private val listFoldersCalls = mutableMapOf<File, List<File>>()

        private fun isFile(file : File) : Boolean {
            val prior = isFileCalls[file]
            if (prior != null) return prior
            isFileCalls[file] = file.isFile
            return isFile(file)
        }

        private fun listFolders(folder : File) : List<File> {
            val prior = listFoldersCalls[folder]
            if (prior != null) return prior
            listFoldersCalls[folder] = folder.listFiles().filter { it.isDirectory }
            return listFolders(folder)
        }

        fun provision(exe : String) : List<File> {
            return ProvisionScope(
                    isWindows,
                    ::isFile,
                    ::listFolders
            ).provision(exe)
        }

        fun printReplayer() {
            val sb = StringBuilder()
            sb.append("val replayer = ProvisionReplayer(\r\n")
            sb.append("    mapOf(\r\n")
            for ((file, value) in isFileCalls) {
                val doubled = file.toString().replace("\\", "\\\\")
                sb.append("      File(\"$doubled\") to $value,\r\n")
            }
            sb.append("    ),\r\n")
            sb.append("    mapOf(\r\n")
            for ((folder, values) in listFoldersCalls) {
                val doubled = folder.toString().replace("\\", "\\\\")
                sb.append("      File(\"$doubled\") to listOf(\r\n")
                for(value in values) {
                    val doubled2 = value.toString().replace("\\", "\\\\")
                    sb.append("            File(\"$doubled2\"),\r\n")
                }
                sb.append("      ),\r\n")
            }
            sb.append("    ),\r\n")
            sb.append(")\r\n")
            println("$sb")
        }
    }

    class ProvisionReplayer(
            val isFileCalls : Map<File, Boolean>,
            val listFoldersCalls : Map<File, List<File>>
    ) {

        private fun isFile(file : File) : Boolean {
            return isFileCalls[file]!!
        }

        private fun listFolders(folder : File) : List<File> {
            return listFoldersCalls[folder]!!
        }

        fun provision(exe : String) : List<File> {
            return ProvisionScope(
                    isWindows,
                    ::isFile,
                    ::listFolders
            ).provision(exe)
        }

    }

    @Test
    fun testLocalEnvironmentRecord() {
        val recorder = ProvisionRecorder()
        val cmakes = recorder.provision("cmake")
        println("Found=$cmakes")
        recorder.printReplayer()
    }

    @Test
    fun testWindowsReplay() {
        val replayer = ProvisionReplayer(
                mapOf(
                        File("C:\\Users\\jomof\\AppData\\Local\\Android\\Sdk\\cmake\\3.6.4111459\\bin\\cmake.exe") to true,
                        File("C:\\Program Files\\CMake\\bin\\cmake.exe") to true,
                        File("C:\\Program Files (x86)\\CMake\\bin\\cmake.exe") to false,
                        File("C:\\Program Files\\Microsoft Visual Studio\\2017\\Community\\Common7\\IDE\\CommonExtensions\\Microsoft\\CMake\\CMake\\bin\\cmake.exe") to false,
                        File("C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\Community\\Common7\\IDE\\CommonExtensions\\Microsoft\\CMake\\CMake\\bin\\cmake.exe") to true
                        ),
                mapOf(
                        File("C:\\Users\\jomof\\AppData\\Local\\Android\\Sdk\\cmake") to listOf(
                                File("C:\\Users\\jomof\\AppData\\Local\\Android\\Sdk\\cmake\\3.6.4111459")
                                )
                        )
                )

        val cmakes = replayer.provision("cmake")
        println("Found=$cmakes")
    }
}