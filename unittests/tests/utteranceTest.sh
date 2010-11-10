#!/bin/sh
# Copyright (c) 2001-2003 Sun Microsystems, Inc.
# All Rights Reserved.
# 
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL 
# WARRANTIES.
#
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

# Input: input_file flite_time_wave_file

REF_TMP=utterance.ref.res
NEW=utterance.res


if [ -f $NEW ]; then
	rm $NEW
fi

if [ -f $1.diff ]; then
	rm $1.diff
fi

java -jar ../lib/freetts.jar -voice kevin -dumpRelations -silent -file $1 > $NEW.t
sed < $NEW.t > $NEW s///g
rm -f $REF_TMP
cp $2 $REF_TMP
sh testUtt 

