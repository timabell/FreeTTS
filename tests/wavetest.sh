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

# Input: input_file flite_wave_file

if [ -f wavetest.res ]; then
	rm wavetest.res
fi

if [ -f wavetest.diff ]; then
	rm wavetest.diff
fi

java -jar ../lib/freetts.jar -voice kevin -silent -dumpASCII wavetest.res.t -file $1

sed < wavetest.res.t > wavetest.res s///g
diff -b wavetest.res $2 > wavetest.diff

wc wavetest.diff | awk '
{
	if ($1 == 0) {
	    printf("%s differences in wavetest.res.  Test PASSED\n", $1);
	} else {
	    printf("%s differences in wavetest.res.  Test FAILED\n", $1);
	}
}
'
