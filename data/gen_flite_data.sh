#!/bin/bash
# Generate data from a given version of flite to be used in freetts tests.
#
# Copyright (c) 2001-2003 Sun Microsystems, Inc.
# All Rights Reserved.
#
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL
# WARRANTIES.
#
# files generated:
# flite.<version>.TokenGame.rel
# flite.<version>.alice2.rel
# flite.<version>.time.rel
# flite.<version>.wave.first.float.txt
# flite.<version>.wave.time.float.txt

TIME="06:51"

usage() 
{
    echo "usage: $0  flite_bin_directory"
    exit -1
}

if [ "$1" = "" ] || [ "$1" = "--help" ] || [ "$1" = "-h" ] || [ ! -d $1 ]; then
    usage
fi

FLITE=$1/flite
FLITE_TIME=$1/flite_time

VERSION=v`$FLITE --version | awk '/version/ {print $2}' | sed 's/flite-//g'`

echo "Running on flite version '$VERSION', using time '$TIME' for time tests."
echo "Note: flite must be the Sun modified version of flite."
echo
echo "Make sure you update the version in ../tests/Makefile"
echo " and confirm that it is using the same time."
echo
echo "If output files are new, make sure files are added to CVS."
echo


rm -f flite.${VERSION}.TokenGame.rel 2>/dev/null
$FLITE -f TokenGame.txt --sets dump_final_relations=on --sets resynth_type=fixed  none > flite.${VERSION}.TokenGame.rel

rm -f flite.${VERSION}.alice2.rel 2>/dev/null
$FLITE -f alice2 --sets dump_final_relations=on --sets resynth_type=fixed  none > flite.${VERSION}.alice2.rel

rm -f flite.${VERSION}.time.rel 2>/dev/null
$FLITE_TIME -set dump_final_relations=on -set resynth_type=fixed $TIME none > flite.${VERSION}.time.rel

rm -f flite.${VERSION}.wave.first.float.txt 2>/dev/null
$FLITE ../wave/08-01-01.wave.text -o flite.${VERSION}.wave.first.float.txt

# it is undocumented that you can pass a file name into flite_time
#   instead of "play" or "none".
rm -f flite.${VERSION}.wave.time.float.txt 2>/dev/null
$FLITE_TIME $TIME flite.${VERSION}.wave.time.float.txt >/dev/null

