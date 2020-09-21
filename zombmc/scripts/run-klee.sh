#!/bin/bash

TIMEOUT=120

CSV=$DAT3M_HOME/output/klee.csv
[ -e $CSV ] && rm $CSV
echo benchmark, safe >> $CSV

KLEE=$KLEE_HOME/build/bin/klee
KLEEFLAGS="--search=randomsp --enable-speculative"

for version in v01 v02 v03 v04 v05 v06 v07 v08 v09 v10 v11 v12 v13 v14 v15
do
    name=$version.none.o0
    log=/$DAT3M_HOME/output/logs/klee/$name.log
    timeout $TIMEOUT $KLEE $KLEEFLAGS $DAT3M_HOME/benchmarks/spectre/bc/$name.bc 2> $log
    safe=$(grep "Spectre found: 0" $log | wc -l)
    echo $name, $safe >> $CSV

    name=$version.none.o2
    log=/$DAT3M_HOME/output/logs/klee/$name.log
    timeout $TIMEOUT $KLEE $KLEEFLAGS $DAT3M_HOME/benchmarks/spectre/bc/$name.bc 2> $log
    safe=$(grep "Spectre found: 0" $log | wc -l)
    echo $name, $safe >> $CSV

    name=$version-cloop.none.o0
    log=/$DAT3M_HOME/output/logs/klee/$name.log
    timeout $TIMEOUT $KLEE $KLEEFLAGS $DAT3M_HOME/benchmarks/spectre/bc/$name.bc 2> $log
    safe=$(grep "Spectre found: 0" $log | wc -l)
    echo $name, $safe >> $CSV

    name=$version-cloop.none.o2
    log=/$DAT3M_HOME/output/logs/klee/$name.log
    timeout $TIMEOUT $KLEE $KLEEFLAGS $DAT3M_HOME/benchmarks/spectre/bc/$name.bc 2> $log
    safe=$(grep "Spectre found: 0" $log | wc -l)
    echo $name, $safe >> $CSV

    name=$version-sloop.none.o0
    log=/$DAT3M_HOME/output/logs/klee/$name.log
    timeout $TIMEOUT $KLEE $KLEEFLAGS $DAT3M_HOME/benchmarks/spectre/bc/$name.bc 2> $log
    safe=$(grep "Spectre found: 0" $log | wc -l)
    echo $name, $safe >> $CSV

    name=$version-sloop.none.o2
    log=/$DAT3M_HOME/output/logs/klee/$name.log
    timeout $TIMEOUT $KLEE $KLEEFLAGS $DAT3M_HOME/benchmarks/spectre/bc/$name.bc 2> $log
    safe=$(grep "Spectre found: 0" $log | wc -l)
    echo $name, $safe >> $CSV
done
