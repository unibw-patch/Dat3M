#!/bin/bash

TIMEOUT=60

BINSECFLAGS="-relse -relse-fp 1 -sse-depth 0 -sse-load-ro-sections -sse-load-sections .got,.got.plt,.data,.plt,.data.rel.ro -fml-solver boolector -fml-solver-timeout 0 -relse-debug-level 0 -relse-paths 0 -x86-handle-seg gs -relse-timeout "$TIMEOUT" -relse-spectre-dyn-pht none -relse-speculative-window 200 -sse-memory "$DAT3M_HOME/benchmarks/spectre/memory.txt

LOGFOLDER=$DAT3M_HOME/output/logs/binsec-$(date +%Y-%m-%d_%H:%M)
mkdir -p $LOGFOLDER

RESULT=$DAT3M_HOME/output/binsec-spectre-pht-results.csv
[ -e $RESULT ] && rm $RESULT
TIMES=$DAT3M_HOME/output/binsec-spectre-pht-times.csv
[ -e $TIMES ] && rm $TIMES
echo benchmark, o0-ns, o2-ns, o0-none, o2-none >> $RESULT
echo benchmark, o0-ns, o2-ns, o0-none, o2-none >> $TIMES

for version in v1 v2 v3 v4 v5 v6 v7 v8 v9 v10 v11 v12 v13 v14 v15
do
    rline=$version
    tline=$version
    for mode in none haunted
    do
        for opt in o0 o2
        do
            flag="-relse-high-sym secret -relse-spectre-stl none -relse-spectre-pht "$mode;

            name=spectre-pht-$version.$mode.$opt
            log=$LOGFOLDER/$name.log
            (time timeout $TIMEOUT binsec $DAT3M_HOME/benchmarks/spectre/spectre-pht-$opt $BINSECFLAGS $flag -entrypoint victim_function_$version) > $log 2>> $log

            min=$(tail -3 $log | awk 'FNR == 1 {print $2}' | awk '{split($0,a,"m"); print a[1]}')
            sec=$(tail -3 $log | awk 'FNR == 1 {print $2}' | awk '{split($0,a,"m"); print a[2]}' | awk '{split($0,a,"."); print a[1]}')
            ms=$(tail -3 $log  | awk 'FNR == 1 {print $2}' | awk '{split($0,a,"m"); print a[2]}' | awk '{split($0,a,"."); print a[2]}' | awk '{split($0,a,"s"); print a[1]}')

            to=$(grep "Result:" $log | wc -l)
            if [ $to -eq 0 ]; then
                ex=$(grep "exception" $log | wc -l)
                if [ $ex -eq 0 ]; then
                    rline=$rline", \VarClock"
                    tline=$tline", "$TIMEOUT
                else
                    rline=$rline", -"
                    tline=$tline", "$((60*min+sec)).$ms
                fi
            else
                unsafe=$(grep "Insecure@Status" $log | wc -l)
                if [ $unsafe -eq 0 ]; then
                    rline=$rline", \gtick"
                else
                    rline=$rline", \redcross"
                fi
                tline=$tline", "$((60*min+sec)).$ms
            fi
        done
    done
    echo $rline >> $RESULT
    echo $tline >> $TIMES
done

RESULT=$DAT3M_HOME/output/binsec-spectre-stl-results.csv
[ -e $RESULT ] && rm $RESULT
TIMES=$DAT3M_HOME/output/binsec-spectre-stl-times.csv
[ -e $TIMES ] && rm $TIMES
echo benchmark, haunted, explicit >> $RESULT
echo benchmark, haunted, explicit >> $TIMES

for version in v1 v2 v3 v4 v5 v6 v7 v8 v9 v10 v11 v12 v13
do
    rline=$version
    tline=$version
    for mode in haunted explicit
    do
        flag="-relse-high-sym secretarray -relse-spectre-pht none";
        if [[ $mode = haunted ]]; then
            flag+=" -relse-spectre-stl haunted-ite";
        fi
        if [[ $mode = explicit ]]; then
            flag+=" -relse-spectre-stl explicit";
        fi

        name=spectre-stl-$version.$mode
        log=$LOGFOLDER/$name.log
        (time timeout $TIMEOUT binsec $DAT3M_HOME/benchmarks/spectre/spectre-stl $BINSECFLAGS $flag -entrypoint victim_function_$version) > $log 2>> $log

        min=$(tail -3 $log | awk 'FNR == 1 {print $2}' | awk '{split($0,a,"m"); print a[1]}')
        sec=$(tail -3 $log | awk 'FNR == 1 {print $2}' | awk '{split($0,a,"m"); print a[2]}' | awk '{split($0,a,"."); print a[1]}')
        ms=$(tail -3 $log  | awk 'FNR == 1 {print $2}' | awk '{split($0,a,"m"); print a[2]}' | awk '{split($0,a,"."); print a[2]}' | awk '{split($0,a,"s"); print a[1]}')
        tline=$tline", "$((60*min+sec)).$ms

        to=$(grep "Result:" $log | wc -l)
        if [ $to -eq 0 ]; then
            rline=$rline", \VarClock"
            tline=$tline", "$TIMEOUT
        else
            unsafe=$(grep "Insecure@Status" $log | wc -l)
            if [ $unsafe -eq 0 ]; then
                rline=$rline", \gtick"
            else
                rline=$rline", \redcross"
            fi
        fi
    done
    echo $rline >> $RESULT
    echo $tline >> $TIMES
done
