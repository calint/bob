FILES=$(ls charts/*.txt)

for FILE in $FILES; do
	echo $FILE
	FILEBASE=${FILE%%.*}
	echo "set terminal png;set key autotitle columnhead;set yrange [0:2000];plot '$FILE'" | gnuplot > $FILEBASE.png
done
