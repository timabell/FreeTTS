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
CUR_PWD=`pwd`
REF=reference.res

if [ -f utteranceTest.res ]; then
	rm utteranceTest.res
fi

if [ -f utteranceTest.diff ]; then
	rm utteranceTest.diff
fi

cd $TOP_DIR/bin

./jtime -dumpRelations -silent -time 06:51 > $CUR_PWD/utteranceTest.res 

cd $CUR_PWD
rm -f $REF
cp $TOP_DIR/data/flite_time.v1.1.rel $REF
./testTimeUtt
