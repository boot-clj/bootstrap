FROM bootclj/tooling AS build

WORKDIR /usr/local/src/bootstrap

RUN mkdir build bin src

ENV BOOT_AS_ROOT=yes

COPY boot.properties build.boot ./

RUN boot deps

COPY ./src ./src/

RUN boot build standalone

RUN cat src/head.sh target/loader.jar > bin/boot.sh

FROM bootclj/clojure:1.10.0

ENV BOOT_AS_ROOT=yes

COPY --from=build /usr/local/src/bootstrap/bin/boot.sh /usr/local/bin/boot
COPY --from=build /root/.m2 /root/.m2
COPY --from=build /root/.boot /root/.boot

RUN chmod +x /usr/local/bin/boot

ENTRYPOINT ["boot"]
