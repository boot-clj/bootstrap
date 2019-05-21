BINARY_LOC := $(shell which boot || which boot.bak | sed "s/.bak//")

echo:
	echo ${BINARY_LOC}

bin/boot.sh:
	boot build standalone
	cat src/head.sh target/bootstrap.uber.jar > bin/boot.sh
	chmod +x bin/boot.sh

install-boot-bin: bin/boot.sh
	mv bin/boot.sh ${BINARY_LOC}
	
backup-boot-bin:
	mv ${BINARY_LOC} ${BINARY_LOC}.bak
	
restore-boot-bin:
	mv ${BINARY_LOC}.bak ${BINARY_LOC}
