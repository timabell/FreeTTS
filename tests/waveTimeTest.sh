#!/bin/sh
# Copyright (c) 2001-2003 Sun Microsystems, Inc.
# All Rights Reserved.
# 
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL 
# WARRANTIES.
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

# Input: input_time flite_utterence_file

if [ -f utteranceTest.res ]; then
	rm utteranceTest.res
fi

if [ -f utteranceTest.diff ]; then
	rm utteranceTest.diff
fi

rm -f timeTest.wave
java -cp ../lib/freetts.jar com.sun.speech.freetts.FreeTTSTime \
    -dumpASCII timeTest.wave -silent -time $1

diff -b $2 timeTest.wave | wc | awk '
{
	if ($1 == 0) {
	    printf("%s differences in timeTest.wave.  Test PASSED\n", $1);
	} else {
	    printf("%s differences in timeTest.wave.  Test FAILED\n", $1);
	}
}
'
