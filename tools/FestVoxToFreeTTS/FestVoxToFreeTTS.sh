#!/bin/sh
# Portions Copyright 2001 Sun Microsystems, Inc.
# Portions Copyright 1999-2001 Language Technologies Institute,
# Carnegie Mellon University.
# All Rights Reserved.  Use is subject to license terms.
#
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL
# WARRANTIES.

# [[TODO]]: for now, requires FLITEDIR and FREETTSDIR
#   Also, check in correct subfolder of FREETTSDIR for .scm scripts....


#[[TODO]]: confirm bourne compatibility

usage() {
    echo "Usage: $0 <voicedir> [lpc | sts | mcep | idx]"
    echo "       $0 --help"
    echo "Converts a festvox voice into FreeTTS format."
    echo
    echo "--help            Show this message."
    echo "<voicedir>        The directory containing the festvox voice data."
    echo "                    This directory must have a wav/ subdirectory."
    echo "lpc,sts,mcep,idx  The second parameter may explicitly run an"
    echo "                    individual stage of the conversion process."
    echo
    echo "The ESTDIR environment variable must point to the directory"
    echo "  containing a compiled version of the Edinbourough Speech Tools."
    echo "festival must be in your path."
    echo
    echo "Running with no second parameter will run the stages in order:"
    echo "  (lpc,sts,mcep,idx)."
    echo "Note that some stages may rely on the execution of previous stages."
    echo
    echo "It is recommended to execute the conversion process without"
    echo "  the second parameter."
    exit -1
}

if [ "$1" = "--help" ] || [ "$1" = "-h" ] || [ "$1" = "-help" ]; then
    usage
fi

if [ -d "$1" ] && [ -d "$1/wav" ]; then
    VOICEDIR="$1"
else
    usage
fi

if [ ! "$ESTDIR" ]; then
    echo "environment variable ESTDIR is unset"
    echo "set it to your local speech tools directory e.g."
    echo '   bash$ export ESTDIR=/home/<username>/projects/speech_tools/'
    echo or
    echo '   csh% setenv ESTDIR /home/<username>/projects/speech_tools/'
    echo
    usage
fi

if [ ! -f $VOICEDIR/etc/voice.defs ]; then
   echo "Can't find $VOICEDIR/etc/voice.defs file"
   echo "don't know what voice to convert"
   echo "Setup for conversion of $1 to flite FAILED"
   echo
   usage
fi

if ! festival --version; then
    echo "Error: festival not in path."
    echo
    usage
fi


# perform conversion
. $VOICEDIR/etc/voice.defs
if [ "$2" = "" ]; then
    $0 $VOICEDIR lpc
    $0 $VOICEDIR sts
    if [ "$FV_TYPE" != "diphone" ]
    then
        $0 $VOICEDIR mcep
    fi
    $0 $VOICEDIR idx
fi

OUTDIR=$VOICEDIR/FreeTTS
mkdir $OUTDIR >/dev/null 2>/dev/null

if [ "$2" = "lpc" ]; then
    echo "Finding LPC coefficients"
    currdir=`pwd`
    cd $VOICEDIR
    for f in $VOICEDIR/wav/*.wav; do
        $VOICEDIR/bin/make_lpc $f  # festival script
    done
    cd $currdir

    echo "Finding LPC min, max, and range"
    for f in $VOICEDIR/lpc/*.lpc; do
        $ESTDIR/bin/ch_track -otype est_ascii $f  # EST binary
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
        }' >$VOICEDIR/lpc/lpc.params
fi

# build sts files
if [ "$2" = "sts" ]; then
   echo "Finding STS files"
   . $VOICEDIR/lpc/lpc.params

   mkdir $VOICEDIR/sts 2>/dev/null

   for f in $VOICEDIR/lpc/*.lpc; do
      fname=`basename $f .lpc`
      echo $fname STS
      $FLITEDIR/tools/find_sts $LPC_MIN $LPC_RANGE $f \
        $VOICEDIR/wav/$fname.wav $VOICEDIR/sts/$fname.sts
      #[[TODO]]: Replace Flite dependancy
   done
fi

if [ "$2" = "mcep" ]; then
    echo "Finding MCEP min max and range"
    for i in $VOICEDIR/mcep/*.mcep; do
        $ESTDIR/bin/ch_track -otype est_ascii $i
    done | sed '1,/EST_Header_End/d' |
    awk 'BEGIN {min=0; max=0;} {
            for (i=4; i<=NF; i++) {
                if ($i < min) min = $i;
                if ($i > max) max = $i;
            }
        } 
        END {printf("(set! mcep_min %f)\n",min);
        printf("(set! mcep_max %f)\n",max);
        printf("(set! mcep_range %f)\n",max-min);
    }' >$VOICEDIR/mcep/mcep.params.scm
fi

idx_non_diphone() {
    echo "Building clunits/ldom index"
    sed '1,/EST_Header_End/d' \
            $VOICEDIR/festival/clunits/$FV_VOICENAME.catalogue |
        awk 'BEGIN {p="CLUNIT_NONE";} {
            if ((NR > 1) && (t != "0_0")) {
                n = split(t,bits,"_");
                unit_type = substr(t,1,length(t)-(length(bits[n])+1));
                unit_occur = bits[n];
                if ((t == "0_0") || (f != $2) || ($1 == "0_0"))
                    printf("%s_%05d -- ( %s %s %s )\n",
                            unit_type,unit_occur,line,p,"CLUNIT_NONE");
                else
                    printf("%s_%05d -- ( %s %s unit_%s )\n",
                            unit_type,unit_occur, line,p,$1);
            }
            line = $0;
            if ((t == "0_0") || (f != $2))
                p = "CLUNIT_NONE";
            else
                p=sprintf("unit_%s",t);
            t=$1;
            f=$2;
        } 
        END {
            if (t != "0_0") {
                n = split(t,bits,"_");
                unit_type = substr(t,1,length(t)-(length(bits[n])+1));
                unit_occur = bits[n];
                printf("%s_%05d -- ( %s %s %s )\n", unit_type,unit_occur,
                        line,p,"CLUNIT_NONE");
            }
        }' | cat > $VOICEDIR/festival/clunits/$FV_VOICENAME.scm
        # [[TODO]]: flite_sort is a flite binary
        cat $VOICEDIR/festival/clunits/$FV_VOICENAME.scm |
            $FLITEDIR/tools/flite_sort |
            sed 's/^.* -- //'  >$VOICEDIR/festival/clunits/$FV_VOICENAME.unitordered.scm
        cat $VOICEDIR/festival/clunits/$FV_VOICENAME.scm |
            sed 's/^.* -- //'  >$VOICEDIR/festival/clunits/$FV_VOICENAME.fileordered.scm
        rm -f flite/$FV_VOICENAME"_lpc"*
        rm -f flite/$FV_VOICENAME"_mcep"*
        festival --heap 5000000 -b $FREETTSDIR/conv_clunits.scm \
            '(dump_clunits "'$FV_VOICENAME'" "'$VOICEDIR'"
            "'$OUTDIR'" "misc.txt" "unittypes.txt" "cart.txt" "units.txt"
            "lpc.txt" "lpc_header.txt" "mcep.txt" "mcep_header.txt"
            "weights.txt")'
        # parentheses allow script to only temporarily change to $OUTDIR
        (cd $OUTDIR
            rm README
            rm $FV_VOICENAME.txt
            echo "The data for the voice $FV_VOICENAME is stored in" > README
            echo "$FV_VOICENAME.txt  All other files may be ignored." >> README
            echo "*** Generated by $0 $1 $2" > $FV_VOICENAME.txt
            echo "*** clunits " `date` >> $FV_VOICENAME.txt
            cat misc.txt unittypes.txt cart.txt units.txt lpc_header.txt \
            lpc.txt mcep_header.txt mcep.txt weights.txt >> $FV_VOICENAME.txt)
}

idx_diphone() {
    echo TEST diphone
}

if [ "$2" = "idx" ]; then
    if [ "$FV_TYPE" = "diphone" ]; then
        idx_diphone
    else
        idx_non_diphone
    fi
fi

