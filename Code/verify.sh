#!/bin/bash

TESTS=$(find . -maxdepth 1 -type d -name "test*" -printf "%f\n" | sort)
NAME=$(whoami)
LOG=/tmp/testing-$NAME-pi-$(date +%F_%T).log
TOTAL=0
PASSED=0

function verifyTest() {
  TESTNAME=$1
  T_SUPPORT=$2
  T_CONFIDENCE=$3

  OUT_FILE="$TESTNAME"_"$T_SUPPORT"_"$T_CONFIDENCE".out
  GOLD_FILE=gold_"$T_SUPPORT"_"$T_CONFIDENCE"

  TOTAL=$(echo $TOTAL | awk '{ print $1+1 }')
  echo -n Verifying $TESTNAME $T_SUPPORT $T_CONFIDENCE...

  # give 2 minutes
  ./timeout.sh 120 make $OUT_FILE -C $TESTNAME >>$LOG 2>&1

  RET=$?

  if [ "$RET" -eq 124 ]; then
    echo "time out."
    return
  fi
  if [ "$RET" -ne 0 ]; then
    echo "Can't make."
    return
  fi

  TEST_RESULT="sort $TESTNAME/$OUT_FILE 2>>$LOG | sed s/,//g 2>>$LOG | diff $TESTNAME/$GOLD_FILE - 2>>$LOG"

  DIFF_COUNT=$(eval $TEST_RESULT | wc -l 2>>$LOG)
  TOTAL_COUNT=$(cat $TESTNAME/$GOLD_FILE 2>>$LOG | wc -l 2>>$LOG)

  if [ $? -eq 0 -a $DIFF_COUNT -eq 0 ]; then
    echo -e "\\e[1;32mPASS.\\e[0m"
    PASSED=$(echo $PASSED | awk '{ print $1+1 }')
  else
    MISSING_COUNT=$(eval $TEST_RESULT | grep -e [\<] 2>>$LOG | wc -l 2>>$LOG)
    FALSE_COUNT=$(eval $TEST_RESULT | grep -e [\>] 2>>$LOG | wc -l 2>>$LOG)
    echo -e "\\e[1;31mFAIL! with $MISSING_COUNT missing, $FALSE_COUNT extra and $TOTAL_COUNT total.\\e[0m"
  fi
}


./clean.sh >$LOG 2>&1
for TEST in $TESTS
do
  verifyTest $TEST 3 65
  verifyTest $TEST 10 80
done

echo PASSED/TOTAL:\ $PASSED/$TOTAL

