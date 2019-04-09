#!/bin/bash

find C/manual/rcu -name "*.litmus" | tr '[A-Z]' '[a-z]' | sort > orig.txt
cd ~/Desktop/Dat3M-2.0.1/Dat3M/litmus
find C/manual/rcu -name "*.litmus" | tr '[A-Z]' '[a-z]' | sort > dart.txt
cd ~/Desktop
mv ~/Desktop/Dat3M-2.0.1/Dat3M/litmus/dart.txt dart.txt
diff orig.txt dart.txt
