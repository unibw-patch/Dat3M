#!/bin/bash

DIR=$DAT3M_HOME/benchmarks/spectre

for version in v1 v2 v3 v4 v5 v6 v7 v8 v9 v10 v11 v12 v13 v14 v15
do
    for mitigation in none lfence slh ns
    do
        flags="-S -fdeclspec -D"$version" -Dspectector";

        if [[ $mitigation = lfence ]]; then
            flags+=" -mllvm -x86-speculative-load-hardening -mllvm -x86-slh-lfence";
        fi

        if [[ $mitigation = slh ]]; then
            flags+=" -mllvm -x86-speculative-load-hardening";
        fi

        clang $flags $DIR/spectre-pht.c -o $DIR/asm/spectre-pht-$version.$mitigation.o0.s
        clang $flags -O2 $DIR/spectre-pht.c -o $DIR/asm/spectre-pht-$version.$mitigation.o2.s
    done
done
