#!/bin/sh
set -e

echo "set terminal png;set key autotitle columnhead;set yrange [0:2000];plot 'data.txt'" | gnuplot > chart.png
