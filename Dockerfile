FROM oracle/graalvm-ce:1.0.0-rc10 AS graalvm

RUN yum install -y wget git

RUN wget -O /usr/local/bin/boot https://github.com/boot-clj/boot-bin/releases/download/latest/boot.sh

RUN chmod 755 /usr/local/bin/boot

ENV BOOT_AS_ROOT=yes

RUN mkdir boot-graalvm

WORKDIR boot-graalvm

RUN mkdir build bin src

COPY boot.properties .

COPY build.boot .

RUN boot deps

COPY . .

RUN boot build

RUN cat  src/head.sh target/loader.jar > bin/boot.sh

RUN native-image -jar ./target/loader.jar --enable-https

FROM oracle/graalvm-ce:1.0.0-rc10

COPY --from=graalvm boot-graalvm/bin/boot.sh /usr/local/bin/boot

COPY --from=graalvm boot-graalvm/loader /usr/local/bin/boot-native

WORKDIR ~

ENTRYPOINT ["boot-native"]
