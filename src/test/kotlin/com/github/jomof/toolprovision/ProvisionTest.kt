package com.github.jomof.toolprovision

import com.google.common.truth.Truth.assertThat
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
            sb.append("val replayer = ProvisionReplayer()\r\n")
            for ((file, value) in isFileCalls) {
                val doubled = file.toString().replace("\\", "\\\\")
                sb.append("replayer.addIsFile(\"$doubled\", $value)\r\n")
            }
            for ((folder, values) in listFoldersCalls) {
                val parent = folder.toString().replace("\\", "\\\\")
                for(value in values) {
                    val child = value.toString().replace("\\", "\\\\")
                    sb.append("replayer.addSubfolder(\"$parent\", \"$child\")\r\n")
                }
            }
            println("$sb")
        }
    }

    class ProvisionReplayer {
        private val isFileCalls = mutableMapOf<File, Boolean>()
        private val listFoldersCalls = mutableMapOf<File, MutableList<File>>()
        fun addIsFile(file: String, result: Boolean) {
            isFileCalls[File(file)] = result
        }

        fun addSubfolder(folderString: String, sub: String) {
            val folder = File(folderString)
            var children = listFoldersCalls[folder]
            if (children == null) {
                children = mutableListOf()
                listFoldersCalls[folder] = children
            }
            children.add(File(sub))
        }

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
        val ninjas = recorder.provision("ninja")
        println("Found=$ninjas")
        recorder.printReplayer()
    }

    @Test
    fun testWindowsReplay() {
        val replayer = ProvisionReplayer()
        replayer.addIsFile("C:\\Users\\jomof\\AppData\\Local\\Android\\Sdk\\cmake\\3.6.4111459\\bin\\cmake.exe", true)
        replayer.addIsFile("C:\\Program Files\\CMake\\bin\\cmake.exe", true)
        replayer.addIsFile("C:\\Program Files (x86)\\CMake\\bin\\cmake.exe", false)
        replayer.addIsFile("C:\\Program Files\\Microsoft Visual Studio\\2017\\Community\\Common7\\IDE\\CommonExtensions\\Microsoft\\CMake\\CMake\\bin\\cmake.exe", false)
        replayer.addIsFile("C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\Community\\Common7\\IDE\\CommonExtensions\\Microsoft\\CMake\\CMake\\bin\\cmake.exe", true)
        replayer.addIsFile("C:\\Users\\jomof\\AppData\\Local\\Android\\Sdk\\cmake\\3.6.4111459\\bin\\ninja.exe", true)
        replayer.addIsFile("C:\\Program Files\\CMake\\bin\\ninja.exe", false)
        replayer.addIsFile("C:\\Program Files (x86)\\CMake\\bin\\ninja.exe", false)
        replayer.addIsFile("C:\\Program Files\\Microsoft Visual Studio\\2017\\Community\\Common7\\IDE\\CommonExtensions\\Microsoft\\CMake\\CMake\\bin\\ninja.exe", false)
        replayer.addIsFile("C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\Community\\Common7\\IDE\\CommonExtensions\\Microsoft\\CMake\\CMake\\bin\\ninja.exe", false)
        replayer.addIsFile("C:\\Program Files\\Microsoft Visual Studio\\2017\\Community\\Common7\\IDE\\CommonExtensions\\Microsoft\\CMake\\Ninja\\ninja.exe", false)
        replayer.addIsFile("C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\Community\\Common7\\IDE\\CommonExtensions\\Microsoft\\CMake\\Ninja\\ninja.exe", true)
        replayer.addSubfolder("C:\\Users\\jomof\\AppData\\Local\\Android\\Sdk\\cmake", "C:\\Users\\jomof\\AppData\\Local\\Android\\Sdk\\cmake\\3.6.4111459")

        val cmakes = replayer.provision("cmake")
        assertThat(cmakes).hasSize(3)
        println("Found=$cmakes")
        val ninjas = replayer.provision("ninja")
        assertThat(ninjas).hasSize(2)
        println("Found=$ninjas")
    }
}