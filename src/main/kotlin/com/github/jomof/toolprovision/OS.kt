package com.github.jomof.toolprovision

private val os = System.getProperty("os.name").toLowerCase()
val isWindows = os.indexOf("win") >= 0
val isMac = os.indexOf("mac") >= 0
val isUnix = os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0