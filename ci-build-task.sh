#!/usr/bin/env sh

curl -L -O https://github.com/nullpomino/nullpomino/archive/refs/tags/v7.5.0.zip
unzip ./nullpomino-v7.5.0.zip
mv ./nullpomino-v7.5.0/* .
/usr/bin/env sh ./build.sh
