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
if [ -f wavetest.res ]; then
	rm wavetest.res
fi

if [ -f wavetest.diff ]; then
	rm wavetest.diff
fi

java -jar ../lib/freetts.jar \
	-Dcom.sun.speech.freetts.useCommandLine=true \
	-Dcom.sun.speech.freetts.useStreamAudio=true \
	-Dcom.sun.speech.freetts.pauseShowUtterance=true \
	-Dcom.sun.speech.freetts.showRelation=Token \
	-Dcom.sun.speech.freetts.intTargetMean=100 \
	-Dcom.sun.speech.freetts.intTargetStdDev=11 \
	-Dcom.sun.speech.freetts.durationStretch=1 \
	-Dcom.sun.speech.freetts.joinType=modified_lpc \
	com.sun.speech.freetts.FreeTTS -voice kevin -dumpASCII wavetest.res -file wave/08-01-01.wave.text

diff wavetest.res first.wave.txt > wavetest.diff

wc wavetest.diff | awk '{print $1 " lines in wavetest.diff file. See wavetest.res for the wave file."}'
