# bootstrap

This is boot's loader shim, it's used download and configure a specific version of boot for a project.
To increase performance this package contains only closed-world code, which is a requirement for compiling bootstrap to a native-image using Oracle GraalVM.

- Compatible with any version of Boot, 3.0.0 or later.
- Currently supported runtimes:
  - OpenJDK 
    - Windows
    - MacOS
    - Linux
  - GraalVM
    - Linux
