#!/bin/bash
for f in ./*.dot
do
 echo "Processing $f" # always double quote "$f" filename
 dot "${f}" -Tpng -o "${f}.png"
 # do something on $f
done