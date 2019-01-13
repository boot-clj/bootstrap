#!/usr/bin/env bash

set -e

VERSION=$(git describe)

echo -e "\033[0;33m<< Version: $VERSION >>\033[0m"; \

boot build

cat  src/head.sh target/loader.jar > bin/boot.sh
echo -e "\033[0;32m<< Success: bin/boot.sh >>\033[0m"
