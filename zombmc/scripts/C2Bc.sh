#!/bin/bash

DIR=$DAT3M_HOME/benchmarks/spectre

for version in v1 v2 v3 v4 v5 v6 v7 v8 v9 v10 v11 v12 v13 v14 v15
do
    for mitigation in none ns
    do
        flags="-g -c -Dklee -emit-llvm -fdeclspec -I $KLEE_HOME/include/ -D"$version;

        clang-6.0 $flags $DIR/spectre-pht.c -o $DIR/bc/spectre-pht-$version.$mitigation.o0.bc
        clang-6.0 $flags -O2 $DIR/spectre-pht.c -o $DIR/bc/spectre-pht-$version.$mitigation.o2.bc
    done
done
