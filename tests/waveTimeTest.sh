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

TOP_DIR=../
CUR_PWD=`pwd`

if [ -f utteranceTest.res ]; then
	rm utteranceTest.res
fi

if [ -f utteranceTest.diff ]; then
	rm utteranceTest.diff
fi

cd $TOP_DIR/bin

rm -f $CUR_PWD/timeTest.wave
./jtime  -dumpWave $CUR_PWD/timeTest.wave -silent -time 06:51 

cd $CUR_PWD
diff $TOP_DIR/data/flite1.1_float_time.txt timeTest.wave | wc | awk '
{
	if ($1 == 0) {
	    printf("%s differences in timeTest.wave.  Test PASSED\n", $1);
	} else {
	    printf("%s differences in timeTest.wave.  Test FAILED\n", $1);
	}
}

'
