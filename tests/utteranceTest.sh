#!/bin/sh
# Copyright (c) 2001 Sun Microsystems, Inc.
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

REF=reference.res

if [ -f utteranceTest.res ]; then
	rm utteranceTest.res
fi

if [ -f utteranceTest.diff ]; then
	rm utteranceTest.diff
fi

sh ../bin/freetts -dumpRelations -silent -file ../data/alice2 > utteranceTest.res 

rm -f $REF
cp ../data/alice2.flite.v1.1-beta.rel $REF
sh testUtt




