#!/bin/bash

mkdir output
cp app/build/outputs/apk/debug/PixelXpert-signed.apk MagiskModBase/PixelXpert-Next.apk
cd MagiskModBase

FILENAME="PixelXpert.zip"
zip -r ../output/$FILENAME *;