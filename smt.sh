#!/bin/bash

RESULTS=results.csv
TIMES=times.csv

[ -e $RESULTS ] && rm $RESULTS
[ -e $TIMES ] && rm $TIMES

echo bench,z3,cvc4 >> $RESULTS
echo bench,z3,cvc4 >> $TIMES

for f in `find . -name "*.smt2" -type f`; do
    name="$(basename $f .bpl.smt2)"
    echo "Testing $name"
    
    LOG=$f.z3.log
    TE=$f.z3.te
    (time timeout $1 z3 $f) 1> $LOG 2> $TE
    if grep -q sat "$LOG"; then
        sr0=$(awk 'FNR == 1' $LOG)
        m=$(awk 'FNR == 2 {print $2}' $TE | awk '{split($0,a,"m"); print a[1]}')
        s=$(awk 'FNR == 2 {print $2}' $TE | awk '{split($0,a,"m"); print a[2]}' | awk '{split($0,a,"s"); print a[1]}' | awk '{split($0,a,"."); print a[1]}')
        st0=$((60*m+s))
    else
        sr0=error
        st0=error
    fi
    [ -s $LOG ] || sr0=timeout
    [ -s $LOG ] || st0=timeout

    LOG=$f.cvc4.log
    TE=$f.cvc4.te
    (time timeout $1 cvc4 $f) 1> $LOG 2> $TE
    if grep -q segfault "$TE"; then
        sr1=crash
        st1=crash
    else
        if grep -q sat "$LOG"; then
            sr1=$(awk 'FNR == 1' $LOG)
            m=$(awk 'FNR == 2 {print $2}' $TE | awk '{split($0,a,"m"); print a[1]}')
            s=$(awk 'FNR == 2 {print $2}' $TE | awk '{split($0,a,"m"); print a[2]}' | awk '{split($0,a,"s"); print a[1]}' | awk '{split($0,a,"."); print a[1]}')
            st1=$((60*m+s))
        else
            if grep -q Parse "$LOG"; then
                sr1=parsing
                st1=parsing
            else
                sr1=error
                st1=error
            fi
        fi
        [ -s $LOG ] || sr1=timeout
        [ -s $LOG ] || st1=timeout
    fi

    echo $name,$sr0,$sr1 >> $RESULTS
    echo $name,$st0,$st1 >> $TIMES

done
