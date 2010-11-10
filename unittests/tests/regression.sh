#!/bin/sh
# Copyright (c) 2001-2003 Sun Microsystems, Inc.
# All Rights Reserved.
# 
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL 
# WARRANTIES.
#
#
# Runs all the regression tests.  This should be the only file
# you need to run in this directory.
#
TIME_INPUT=06:51
FLITE_VERSION=v1.2-release

export TIME_INPUT FLITE_VERSION

# The wave tests...
#
echo
echo Running the wave tests...
chmod +x wavetest.sh waveTimeTest.sh
sh ./waveTimeTest.sh ${TIME_INPUT} data/flite.${FLITE_VERSION}.wave.time.float.txt
sh ./wavetest.sh wave/08-01-01.wave.text data/flite.${FLITE_VERSION}.wave.first.float.txt

# The lpc tests...
#
echo
echo Running the lpc tests...
chmod +x lpctest.sh
sh ./lpctest.sh

# The utterance tests...
#
echo
echo Running the utterance tests
chmod +x utteranceTest.sh compareNumericUtt compareUtt testUtt utteranceTimeTest.sh testTimeUtt compareNumericUtt
sh ./utteranceTest.sh data/alice2 data/flite.${FLITE_VERSION}.alice2.rel
sh ./utteranceTimeTest.sh  ${TIME_INPUT} data/flite.${FLITE_VERSION}.time.rel 
sh ./utteranceTest.sh data/TokenGame.txt data/flite.${FLITE_VERSION}.TokenGame.rel

echo 
echo Cleaning up...
rm -f *.1 *.2 *.res *.diff timeTest.wave

echo
echo DONE!
