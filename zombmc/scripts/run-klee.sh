#!/bin/bash

TIMEOUT=60

KLEE=$KLEE_HOME/build/bin/klee
KLEEFLAGS="--search=randomsp"

LOGFOLDER=$DAT3M_HOME/output/logs/klee-$(date +%Y-%m-%d_%H:%M)
mkdir -p $LOGFOLDER

RESULT=$DAT3M_HOME/output/klee-results.csv
[ -e $RESULT ] && rm $RESULT
TIMES=$DAT3M_HOME/output/klee-times.csv
[ -e $TIMES ] && rm $TIMES
echo benchmark, o0-none, o2-none, o0-ns, o2-ns >> $RESULT
echo benchmark, o0-none, o2-none, o0-ns, o2-ns >> $TIMES

for version in v1 v2 v3 v4 v5 v6 v7 v8 v9 v10 v11 v12 v13 v14 v15
do
    rline=$version
    tline=$version
    for mitigation in none ns
    do
        for opt in o0 o2
        do
            flag="";
            if [[ $mitigation != ns ]]; then
                flag+="--enable-speculative";
            fi

            name=spectre-pht-$version.$mitigation.$opt
            log=$LOGFOLDER/$name.log
            (time timeout $TIMEOUT $KLEE $KLEEFLAGS $flag $DAT3M_HOME/benchmarks/spectre/bc/$name.bc) 2> $log

            min=$(tail -3 $log | awk 'FNR == 1 {print $2}' | awk '{split($0,a,"m"); print a[1]}')
            sec=$(tail -3 $log | awk 'FNR == 1 {print $2}' | awk '{split($0,a,"m"); print a[2]}' | awk '{split($0,a,"."); print a[1]}')
            ms=$(tail -3 $log  | awk 'FNR == 1 {print $2}' | awk '{split($0,a,"m"); print a[2]}' | awk '{split($0,a,"."); print a[2]}' | awk '{split($0,a,"s"); print a[1]}')
            tline=$tline", "$((60*min+sec)).$ms

            to=$(grep "Spectre found" $log | wc -l)
            if [ $to -eq 0 ]; then
                rline=$rline", \VarClock"
                tline=$tline", "$TIMEOUT
            else
                safe=$(grep "Spectre found: 0" $log | wc -l)
                if [ $safe -eq 0 ]; then
                    rline=$rline", \redcross"
                else
                    rline=$rline", \gtick"
                fi
            fi
        done
    done
    echo $rline >> $RESULT
    echo $tline >> $TIMES
done
