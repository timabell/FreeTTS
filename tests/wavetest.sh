#!/bin/sh
#
# This test script first runs the FreeTTS with our first utterance file,
# dumps the wave in text form, and compares (diff) it with our standard 
# wave form file for our first utterance (first.wave.txt). This test will
# tell you how many lines differ from the standard wave form file.
#
# (Reminder: our 'first utterance file' is "Hello world.  This is Duke 
# coming to you from inside the java virtual machine. I'm happy to have
# a voice because I've been meaning to tell you how much I care.")
#

TOP_DIR=..
PWD=`pwd`

if [ -f wavetest.res ]; then
	rm wavetest.res
fi

if [ -f wavetest.diff ]; then
	rm wavetest.diff
fi

FREETTS_CLASSES=$TOP_DIR/classes:$TOP_DIR/lib/CommandInterpreter.jar
if [ -z "${JAVA_HOME}" ] ; then
    JAVA_HOME=/lab/speech/java/j2sdk1.4.0
fi

${JAVA_HOME}/bin/java -Xms64m -ea -cp $FREETTS_CLASSES \
	com.sun.speech.freetts.FreeTTS -silent -dumpWave $PWD/wavetest.res -file $TOP_DIR/wave/08-01-01.wave.text

diff wavetest.res ../data/flite1.1_float.first.wave.txt > wavetest.diff

wc wavetest.diff | awk '
{
	if ($1 == 0) {
	    printf("%s differences in waveTest.res.  Test PASSED\n", $1);
	} else {
	    printf("%s differences in waveTest.res.  Test FAILED\n", $1);
	}
}
'
