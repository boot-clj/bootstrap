FROM bootclj/tooling AS build

WORKDIR /usr/src/bootstrap

RUN mkdir build bin src

ENV BOOT_AS_ROOT=yes

COPY boot.properties build.boot ./

RUN boot deps

COPY . .

RUN boot build

RUN cat src/head.sh target/loader.jar > bin/boot.sh

FROM bootclj/clojure:1.10

COPY --from=build /usr/src/bootstrap/bin/boot.sh /usr/local/bin/boot

ENTRYPOINT ["boot"]
