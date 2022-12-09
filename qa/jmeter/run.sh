#!/bin/sh
set -e

JMETER_HOME=~/Downloads/apache-jmeter-5.5
JMETER_QA_HOME=~/w/bob/qa/jmeter

CMD="$JMETER_HOME/bin/jmeter -n -t $JMETER_QA_HOME/test-plan.jmx -l $JMETER_QA_HOME/result -e -o $JMETER_QA_HOME/reports"
echo $CMD
$CMD
