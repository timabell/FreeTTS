#!/bin/bash
# Portions Copyright 2004 Sun Microsystems, Inc.
# Portions Copyright 1999-2003 Language Technologies Institute,
# Carnegie Mellon University.
# All Rights Reserved.  Use is subject to license terms.
#
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL
# WARRANTIES.

# Shell script to do a lot of the work to import a cluster
# unit selection voice into FreeTTS.  This is still a work
# in progress.

# You should override these environment variables to match
# your environment.
#
export JAVA_HOME=/usr
export ESTDIR=/Users/wwalker/work/festival/speech_tools
export FESTIVALDIR=/Users/wwalker/work/festival/festival
export FESTVOXDIR=/Users/wwalker/work/festival/festvox
export FREETTSDIR=/Users/wwalker/work/SourceForge/FreeTTS/FreeTTS

export HELPERDIR=$FREETTSDIR/tools/ArcticToFreeTTS

export JAVAHEAP=-mx512m




# Compile the helping Java classes
#
(cd $HELPERDIR; mkdir -p classes; cd src; javac -d ../classes *.java)




# Get FV_VOICENAME and FV_FULLVOICENAME
#
. ./etc/voice.defs

echo Importing $FV_VOICENAME

export VOICEDIR=$FREETTSDIR/com/sun/speech/freetts/en/us/$FV_VOICENAME

mkdir -p FreeTTS
mkdir -p $VOICEDIR




# Import F0_MEAN (for setPitch) and F0_RANGE (for setPitchRange)
#
# [[[WDW FIXME: Currently unused.]]]
#
$FESTIVALDIR/bin/festival -b \
    festvox/$FV_FULLVOICENAME.scm \
    $HELPERDIR/scheme/dump_f0_terms.scm \
    "(begin (voice_${FV_FULLVOICENAME}) (dump_f0_terms))" > \
    /tmp/foo.sh
chmod +x /tmp/foo.sh
. /tmp/foo.sh




########################################################################
#                                                                      #
# Create voice-specific files that are not part of the big database.   #
#                                                                      #
# [[[WDW FIXME: Currently copies some files from FreeTTS instead of    #
#    generating them directly from the voice data itself.]]]           #
#                                                                      #
########################################################################

echo Creating $VOICEDIR/dur_stat.txt
$FESTIVALDIR/bin/festival -b \
    festvox/$FV_FULLVOICENAME.scm \
    $HELPERDIR/scheme/dump_dur_stat.scm \
    "(begin (voice_${FV_FULLVOICENAME}) (dump_dur_stat))" > \
    $VOICEDIR/dur_stat.txt




echo Creating $VOICEDIR/durz_cart.txt
$FESTIVALDIR/bin/festival -b \
    festvox/$FV_FULLVOICENAME.scm \
    $HELPERDIR/scheme/dump_durz_cart.scm \
    "(begin (voice_${FV_FULLVOICENAME}) (dump_durz_cart))" > \
    $VOICEDIR/durz_cart.txt




echo Creating $VOICEDIR/f0_lr_terms.txt
cp $FREETTSDIR/com/sun/speech/freetts/en/us/f0_lr_terms.txt $VOICEDIR




echo Creating $VOICEDIR/int_accent_cart.txt
cp $FREETTSDIR/com/sun/speech/freetts/en/us/int_accent_cart.txt $VOICEDIR




echo Creating $VOICEDIR/int_tone_cart.txt
cp $FREETTSDIR/com/sun/speech/freetts/en/us/int_tone_cart.txt $VOICEDIR




echo Creating $VOICEDIR/nums_cart.txt
cp $FREETTSDIR/com/sun/speech/freetts/en/us/nums_cart.txt $VOICEDIR




echo Creating $VOICEDIR/part_of_speech.txt
$FESTIVALDIR/bin/festival -b \
    festvox/$FV_FULLVOICENAME.scm \
    $HELPERDIR/scheme/dump_pos.scm \
    "(begin (voice_${FV_FULLVOICENAME}) (dump_pos))" > \
    $VOICEDIR/part_of_speech.txt




echo Creating $VOICEDIR/phoneset.txt
$FESTIVALDIR/bin/festival -b \
    festvox/$FV_FULLVOICENAME.scm \
    $HELPERDIR/scheme/dump_phoneset.scm \
    "(begin (voice_${FV_FULLVOICENAME}) (dump_phoneset))" > \
    $VOICEDIR/phoneset.txt




echo Creating $VOICEDIR/phrasing_cart.txt
cp $FREETTSDIR/com/sun/speech/freetts/en/us/phrasing_cart.txt $VOICEDIR




echo Creating $VOICEDIR/prefix_fsm.txt
cp $FREETTSDIR/com/sun/speech/freetts/en/us/prefix_fsm.txt $VOICEDIR




echo Creating $VOICEDIR/suffix_fsm.txt
cp $FREETTSDIR/com/sun/speech/freetts/en/us/suffix_fsm.txt $VOICEDIR




########################################################################
#                                                                      #
# Create voice-specific files that are part of the big database.       #
# These will ultimately be concatenated together to make the big txt   #
# file for the voice data.                                             #
#                                                                      #
########################################################################

echo Creating lpc files
mkdir -p lpc
bin/make_lpc wav/*.wav




echo Creating lpc/lpc.params
for file in lpc/*.lpc; do
    $ESTDIR/bin/ch_track -otype est_ascii $file
done | sed '1,/EST_Header_End/d' |
awk 'BEGIN {min=0; max=0;} {
         for (i=4; i<=NF; i++) {
             if ($i < min) min = $i;
             if ($i > max) max = $i;
         }
     } END {
         printf("LPC_MIN=%f\n",min);
         printf("LPC_MAX=%f\n",max);
         printf("LPC_RANGE=%f\n",max-min);
     }' > lpc/lpc.params




echo Creating mcep/mcep.params and converting mcep files to text
for file in mcep/*.mcep; do
    echo $file MCEP
    $ESTDIR/bin/ch_track -otype est_ascii $file > $file.txt
    cat $file.txt
done | sed '1,/EST_Header_End/d' |
awk 'BEGIN {min=0; max=0;} {
         for (i=4; i<=NF; i++) {
             if ($i < min) min = $i;
             if ($i > max) max = $i;
         }
     } END {
         printf("MCEP_MIN=%f\n",min);
         printf("MCEP_MAX=%f\n",max);
         printf("MCEP_RANGE=%f\n",max-min);
     }' > mcep/mcep.params




echo "Creating short term signal (STS) files in sts/*.sts"
mkdir -p sts
java -cp $HELPERDIR/classes FindSTS \
    `find wav -type f | cut -f2 -d/ | cut -f1 -d.`




echo Creating FreeTTS/misc.txt
$FESTIVALDIR/bin/festival -b \
    festvox/$FV_FULLVOICENAME.scm \
    $HELPERDIR/scheme/dump_misc.scm \
    "(begin (voice_${FV_FULLVOICENAME}) (dump_misc))" > \
    FreeTTS/misc.txt




# UnitDatabase outputs its own info...

java $JAVAHEAP -cp $HELPERDIR/classes UnitDatabase \
    festival/clunits/${FV_VOICENAME}.catalogue \
    `find wav -type f | cut -f2 -d/ | cut -f1 -d.`




echo Creating FreeTTS/trees.txt
$FESTIVALDIR/bin/festival -b \
    festvox/$FV_FULLVOICENAME.scm \
    $HELPERDIR/scheme/dump_trees.scm \
    "(begin (voice_${FV_FULLVOICENAME}) (dump_trees))" > \
    FreeTTS/trees.txt




echo Creating FreeTTS/weights.txt
$FESTIVALDIR/bin/festival -b \
    festvox/$FV_FULLVOICENAME.scm \
    $HELPERDIR/scheme/dump_join_weights.scm \
    "(begin (voice_${FV_FULLVOICENAME}) (dump_join_weights))" > \
    FreeTTS/weights.txt




########################################################################
#                                                                      #
# Now create the big database file and also set up the *.java files    #
# for this voice.                                                      #
#                                                                      #
########################################################################

echo Creating $VOICEDIR/$FV_VOICENAME.txt
(cd FreeTTS; cat misc.txt unit_catalog.txt trees.txt unit_index.txt sts.txt mcep.txt weights.txt > $VOICEDIR/$FV_VOICENAME.txt)




echo Creating $VOICEDIR/ArcticVoiceDirectory.java
cat $HELPERDIR/ArcticVoiceDirectory.java.template | \
    sed "s/%FV_VOICENAME%/$FV_VOICENAME/g" | \
    sed "s/%FV_NAME%/$FV_NAME/g" | \
    sed "s/%FV_INST%/$FV_INST/g" > \
    $VOICEDIR/ArcticVoiceDirectory.java




echo Creating $VOICEDIR/ArcticVoice.java
cat $HELPERDIR/ArcticVoice.java.template | \
    sed "s/%FV_VOICENAME%/$FV_VOICENAME/g" | \
    sed "s/%FV_NAME%/$FV_NAME/g" | \
    sed "s/%FV_INST%/$FV_INST/g" > \
    $VOICEDIR/ArcticVoice.java




echo Creating $VOICEDIR/voice.Manifest
cat $HELPERDIR/voice.Manifest.template | \
    sed "s/%FV_VOICENAME%/$FV_VOICENAME/g" | \
    sed "s/%FV_NAME%/$FV_NAME/g" | \
    sed "s/%FV_INST%/$FV_INST/g" > \
    $VOICEDIR/voice.Manifest




echo Compiling $FV_VOICENAME
(cd $FREETTSDIR; ant -Darctic_voice=$FV_VOICENAME)




echo Done.
