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
if [ -z "${JAVA_HOME}" ] ; then
    JAVA_HOME=/lab/speech/java/j2sdk1.4.0
fi

${JAVA_HOME}/bin/java -Xms64m -ea -cp $FREETTS_CLASSES \
	-Dcom.sun.speech.freetts.outputLPC=true \
	com.sun.speech.freetts.FreeTTS -silent -file $TOP_DIR/wave/08-01-01.wave.text | grep -v "^#" > lpctest.res

diff lpctest.res $TOP_DIR/wave/flite1.1.lpcres.txt > lpctest.diff

wc lpctest.diff | awk '
{
	if ($1 == 0) {
	    printf("%s differences in lpctest.diff.  Test PASSED\n", $1);
	} else {
	    printf("%s differences in lpctest.diff.  Test FAILED\n", $1);
	}
}
'
