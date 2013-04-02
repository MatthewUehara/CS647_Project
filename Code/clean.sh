#!/bin/bash
prog="$(readlink "$0" 2>&1)"
[ $? -eq 127 -o "$prog" = "" ] && prog="$0"
ROOT=$(dirname $prog)
TESTS=$(find $ROOT -maxdepth 1 -type d -name "test*" -printf "%f\n" | sort)

for TEST in $TESTS
do
  make clean -C $ROOT/$TEST
done

