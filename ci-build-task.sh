#!/usr/bin/env sh

curl -L -O https://github.com/nullpomino/nullpomino/archive/refs/tags/v7.5.0.tar.gz
tar -xf ./v7.5.0.tar.gz
mv ./nullpomino-7.5.0/* .
mkdir -p ./bin
find -wholename "./src/*.java" > .TMPSRC
javac -source 8 -target 8 -encoding utf8 -sourcepath ./src -d ./bin -cp ./lib/* @.TMPSRC
