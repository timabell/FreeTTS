#!/bin/sh
#
# This test script first runs the FreeTTS with our first utterance file,
# dumps the LPC residual in text form, and compares (diff) it with our
# standard LPC file for our first utterance (first.wave.txt). This test will
# tell you how many lines differ from the standard LPC file.
#

TOP_DIR=..
PWD=`pwd`

if [ -f lpctest.res ]; then
	rm lpctest.res
fi

if [ -f lpctest.diff ]; then
	rm lpctest.diff
fi

FREETTS_CLASSES=$TOP_DIR/classes
if [ -z "${JDK_DIR}" ] ; then
    JDK_DIR=/lab/speech/java/j2sdk1.4.0
fi

${JDK_DIR}/bin/java -Xms64m -ea -cp $FREETTS_CLASSES \
	-Dcom.sun.speech.freetts.outputLPC=true \
	com.sun.speech.freetts.FreeTTS -silent -file $TOP_DIR/wave/08-01-01.wave.text | grep -v "^#" > lpctest.res

diff lpctest.res $TOP_DIR/wave/08-01-01.wave.lpc > lpctest.diff

wc lpctest.diff | awk '{print $1 " lines in lpctest.diff file. See lpctest.res for the LPC residual file."}'
