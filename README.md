# boot-native

Native Image built using Oracle Graal. This executable downloads and runs [Boot](http://boot-clj.com).

* Compatible with any version of Boot, 3.0.0 or later.
* Currently only for Linux, MacOS & Windows are pending.

## Build & Run

Build using docker - `docker build -t boot-graalvm:latest .`

Run using docker - `docker run -it boot-graalvm:latest`
