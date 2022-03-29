#!/usr/bin/env sh
# First make the bin folder if it doesn't exist.
mkdir -p ./bin

# Run the compilation process.
# This will require JDK 8 specifically in order to work.
find -wholename "./src/*.java" > .TMPSRC
javac -encoding utf8 -sourcepath ./src -d ./bin -cp ./lib/* @.TMPSRC
