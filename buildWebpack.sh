#!/usr/bin/env bash

rm -Rf build/distributions/*
rm -Rf docs/*
./gradlew build
cp -R build/distributions/* docs
