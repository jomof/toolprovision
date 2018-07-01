Entities:
- A tool, for example "cmake"
- A framework, for example "ndk"

* A package may contain a tools, for example "ndk" contains "clang"
* A package may contain a sub-package, for example "Android sdk" contains "ndk"

tool
  exe: cmake
  
package:
  name: cmake
  tools:
    path: bin/
    exe: cmake

tool
  exe: clang++

framework
  name: ndk
  location:
    path:  



Discovery pipeline:
var tool = find("cmake")

Inventory Pipeline:
- Get list of known tools, for example "cmake"
- Get list of known frameworks, for example "ndk"
- Given list of exes call "--version" on each