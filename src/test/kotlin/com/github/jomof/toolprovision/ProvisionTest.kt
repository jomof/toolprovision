package com.github.jomof.toolprovision

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class ProvisionTest {
    class ProvisionRecorder {
        private val getenvCalls = mutableMapOf<String, String?>()
        private val isFileCalls = mutableMapOf<String, Boolean>()
        private val listFoldersCalls = mutableMapOf<String, List<String>>()

        private fun getenv(key: String): String? {
            if (getenvCalls.containsKey(key)) {
                return getenvCalls[key]
            }
            getenvCalls[key] = System.getenv(key)
            return getenv(key)
        }
        private fun isFile(file: String): Boolean {
            val prior = isFileCalls[file]
            if (prior != null) return prior
            isFileCalls[file] = File(file).isFile
            return isFile(file)
        }

        private fun listFolders(folder: String): List<String> {
            val prior = listFoldersCalls[folder]
            if (prior != null) return prior
            listFoldersCalls[folder] = File(folder)
                    .listFiles()
                    .filter { it.isDirectory }
                    .map { it.toString() }
            return listFolders(folder)
        }

        fun provision(exe: String): List<String> {
            return ProvisionScope(
                    isWindows,
                    ::getenv,
                    ::isFile,
                    ::listFolders
            ).provision(exe)
        }

        fun printReplayer() {
            val sb = StringBuilder()
            sb.append("val replayer = ProvisionReplayer()\r\n")
            for ((key, value) in getenvCalls) {
                if (value == null) {
                    sb.append("replayer.addGetenv(\"$key\", null)\r\n")
                } else {
                    val doubled = value.replace("\\", "\\\\")
                    sb.append("replayer.addGetenv(\"$key\", \"$doubled\")\r\n")
                }
            }
            for ((file, value) in isFileCalls) {
                val doubled = file.replace("\\", "\\\\")
                sb.append("replayer.addIsFile(\"$doubled\", $value)\r\n")
            }
            for ((folder, values) in listFoldersCalls) {
                val parent = folder.replace("\\", "\\\\")
                for(value in values) {
                    val child = value.replace("\\", "\\\\")
                    sb.append("replayer.addSubfolder(\"$parent\", \"$child\")\r\n")
                }
            }
            System.err.print("$sb")
        }
    }

    class ProvisionReplayer(val isWindows: Boolean) {
        private val getenvCalls = mutableMapOf<String, String?>()
        private val isFileCalls = mutableMapOf<String, Boolean>()
        private val listFoldersCalls = mutableMapOf<String, MutableList<String>>()

        fun addGetenv(key: String, value: String?) {
            getenvCalls[key] = value
        }

        fun addIsFile(file: String, result: Boolean) {
            isFileCalls[file] = result
        }

        fun addSubfolder(folder: String, sub: String) {
            var children = listFoldersCalls[folder]
            if (children == null) {
                children = mutableListOf()
                listFoldersCalls[folder] = children
            }
            children.add(sub)
        }

        private fun getenv(key: String): String? {
            return getenvCalls[key]
        }

        private fun isFile(file: String): Boolean {
            return isFileCalls[file]!!
        }

        private fun listFolders(folder: String): List<String> {
            return listFoldersCalls[folder]!!
        }

        fun provision(exe: String): List<String> {
            return ProvisionScope(
                    isWindows,
                    ::getenv,
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
    fun testLinuxReplay() {
        val replayer = ProvisionReplayer(false)
        replayer.addGetenv("ANDROID_HOME", "/usr/local/android-sdk/")
        replayer.addIsFile("/usr/bin/cmake", true)
        replayer.addIsFile("/usr/local/android-sdk/cmake/3.6.4111459/bin/cmake", true)
        replayer.addIsFile("/usr/bin/ninja", false)
        replayer.addIsFile("/usr/local/android-sdk/cmake/3.6.4111459/bin/ninja", true)
        replayer.addSubfolder("/usr/local/android-sdk/cmake/", "/usr/local/android-sdk/cmake/3.6.4111459")
        val cmakes = replayer.provision("cmake")
        println("Found=$cmakes")
        assertThat(cmakes).contains("/usr/bin/cmake")
        assertThat(cmakes).contains("/usr/local/android-sdk/cmake/3.6.4111459/bin/cmake")
    }

    @Test
    fun testWindowsReplay() {
        val replayer = ProvisionReplayer(true)
        replayer.addGetenv("LOCALAPPDATA", "C:\\Users\\jomof\\AppData\\Local")
        replayer.addGetenv("ANDROID_HOME", null)
        replayer.addGetenv("ProgramFiles", "C:\\Program Files")
        replayer.addGetenv("ProgramFiles(x86)", "C:\\Program Files (x86)")
        replayer.addIsFile("C:/Users/jomof/AppData/Local/Android/Sdk/cmake/3.6.4111459/bin/cmake.exe", true)
        replayer.addIsFile("C:/Program Files/CMake/bin/cmake.exe", true)
        replayer.addIsFile("C:/Program Files (x86)/CMake/bin/cmake.exe", false)
        replayer.addIsFile("C:/Program Files/Microsoft Visual Studio/2017/Community/Common7/IDE/CommonExtensions/Microsoft/CMake/CMake/bin/cmake.exe", false)
        replayer.addIsFile("C:/Program Files (x86)/Microsoft Visual Studio/2017/Community/Common7/IDE/CommonExtensions/Microsoft/CMake/CMake/bin/cmake.exe", true)
        replayer.addIsFile("C:/Users/jomof/AppData/Local/Android/Sdk/cmake/3.6.4111459/bin/ninja.exe", true)
        replayer.addIsFile("C:/Program Files/CMake/bin/ninja.exe", false)
        replayer.addIsFile("C:/Program Files (x86)/CMake/bin/ninja.exe", false)
        replayer.addIsFile("C:/Program Files/Microsoft Visual Studio/2017/Community/Common7/IDE/CommonExtensions/Microsoft/CMake/CMake/bin/ninja.exe", false)
        replayer.addIsFile("C:/Program Files (x86)/Microsoft Visual Studio/2017/Community/Common7/IDE/CommonExtensions/Microsoft/CMake/CMake/bin/ninja.exe", false)
        replayer.addIsFile("C:/Program Files/Microsoft Visual Studio/2017/Community/Common7/IDE/CommonExtensions/Microsoft/CMake/Ninja/ninja.exe", false)
        replayer.addIsFile("C:/Program Files (x86)/Microsoft Visual Studio/2017/Community/Common7/IDE/CommonExtensions/Microsoft/CMake/Ninja/ninja.exe", true)
        replayer.addSubfolder("C:/Users/jomof/AppData/Local/Android/Sdk/cmake/", "C:\\Users\\jomof\\AppData\\Local\\Android\\Sdk\\cmake\\3.6.4111459")

        val cmakes = replayer.provision("cmake")
        assertThat(cmakes).hasSize(3)
        println("Found=$cmakes")
        val ninjas = replayer.provision("ninja")
        assertThat(ninjas).hasSize(2)
        println("Found=$ninjas")
    }
}