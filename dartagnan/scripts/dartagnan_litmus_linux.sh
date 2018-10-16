for file in $(find litmus/C -name '*.litmus' | sort);
do
   if [[ $file == *"mutual"* ]];
   then
      continue
   fi
   herd7 -I cat -model cat/linux-kernel.cat -bell cat/linux-kernel.bell -macros cat/linux-kernel.def $file > ./herd_linux.out
   herd=$(grep -e 'No' herd_linux.out | wc -l)
   rm herd_linux.out
   java dartagnan/Dartagnan -t sc -i $file -cat cat/linux-kernel.cat > ./dart_linux.out
   dart=$(grep -e 'No' dart_linux.out | wc -l)
   rm dart_linux.out
   if [ $dart != $herd ]
   then
      echo $file
      break
   fi
   java dartagnan/Dartagnan -t sc -i $file -cat cat/linux-kernel.cat -idl > ./dart_linux.out
   dart=$(grep -e 'No' dart_linux.out | wc -l)
   rm dart_linux.out
   if [ $dart != $herd ]
   then
      echo $file
      break
   fi
   java dartagnan/Dartagnan -t sc -i $file -cat cat/linux-kernel.cat -relax > ./dart_linux.out
   dart=$(grep -e 'No' dart_linux.out | wc -l)
   rm dart_linux.out
   if [ $dart != $herd ]
   then
      echo $file
      break
   fi
done
