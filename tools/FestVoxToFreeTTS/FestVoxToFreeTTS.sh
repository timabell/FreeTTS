#!/bin/bash
# Portions Copyright 2003 Sun Microsystems, Inc.
# Portions Copyright 1999-2003 Language Technologies Institute,
# Carnegie Mellon University.
# All Rights Reserved.  Use is subject to license terms.
#
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL
# WARRANTIES.

usage() {
    echo "Usage: $0 <voicedir> [lpc | sts | mcep | idx | install | compile]"
    echo "       $0 --help"
    echo "Converts a festvox voice into FreeTTS format."
    echo
    echo "--help            Show this message."
    echo "<voicedir>        The directory containing the festvox voice data."
    echo "                    This directory must have a wav/ subdirectory."
    echo "lpc,sts,mcep,...  The second parameter may explicitly run an"
    echo "                    individual stage of the conversion process."
    echo
    echo "The ESTDIR environment variable must point to the directory"
    echo "  containing a compiled version of the Edinbourough Speech Tools."
    echo "festival, java, and javac must be in your path."
    echo
    echo "Running with no second parameter will run the stages in order:"
    echo "  (lpc,sts,mcep,idx,install,compile)."
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

. $VOICEDIR/etc/voice.defs

if [ "$2" = "compile" ]; then
    if [ "$FV_TYPE" = "diphone" ]; then
        ant -Ddiphone_voice=$FV_VOICENAME -find build.xml
    elif [ "$FV_TYPE" = "ldom" ] || [ "$FV_TYPE" = "clunits" ]; then
        ant -Dclunit_voice=$FV_VOICENAME -find build.xml
    fi
    exit 0
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
   echo
   echo "If the voice directory is correct, you could try"
   echo " festvox/src/general/guess_voice_defs to generate it."
   echo
   echo "Setup for conversion of $1 to flite FAILED"
   echo
   usage
fi

if ! festival --version; then
    echo "Error: festival not in path."
    echo
    usage
fi

if ! java -version >/dev/null 2>/dev/null || ! javac -help 2>/dev/null; then
    echo "Error: java and javac must be in path."
    echo
    usage
fi


# perform conversion
if [ "$2" = "" ]; then
    
    # perform each step individually
    if ! $0 $VOICEDIR lpc; then
        exit $?
    fi
    if ! $0 $VOICEDIR sts; then
        exit $?
    fi
    if [ "$FV_TYPE" != "diphone" ]
    then
        if ! $0 $VOICEDIR mcep; then
            exit $?
        fi
    fi
    if ! $0 $VOICEDIR idx; then
        exit $?
    fi

    if ! $0 $VOICEDIR "install"; then
        exit $?
    fi

    if ! $0 $VOICEDIR compile; then
        exit $?
    fi

    echo
    echo "Conversion process complete"
fi

# The scheme and java files should be in the same directory as this script
HELPERDIR=`dirname $0`

# This assumes that FreeTTS is configured with this directory structure
FREETTSDIR="$HELPERDIR/../.."
EN_US_DIR="$FREETTSDIR/com/sun/speech/freetts/en/us"

#This is where some temperary files are generated as well as the final voice
OUTDIR=$VOICEDIR/FreeTTS
mkdir $OUTDIR >/dev/null 2>/dev/null

if [ "$2" = "lpc" ]; then
    echo "Finding LPC coefficients"
    (cd $VOICEDIR
     for f in wav/*.wav; do
        bin/make_lpc $f  # festival script
     done
    )

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

   # compile FindSTS
   CLASSFILES="FindSTS.class LPC.class STS.class Wave.class Utility.class"
   (cd $HELPERDIR
    javac FindSTS.java
    jar -cf FindSTS.jar $CLASSFILES
    rm -f $CLASSFILES 2>/dev/null)

   for f in $VOICEDIR/lpc/*.lpc; do
      fname=`basename $f .lpc`
      echo $fname STS
      java -cp "$HELPERDIR/FindSTS.jar" FindSTS $LPC_MIN $LPC_RANGE $f \
        $VOICEDIR/wav/$fname.wav $VOICEDIR/sts/$fname.sts
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
        cat $VOICEDIR/festival/clunits/$FV_VOICENAME.scm |
           sed 's/^.* -- //'  \
            >$VOICEDIR/festival/clunits/$FV_VOICENAME.fileordered.scm
        cat $VOICEDIR/festival/clunits/$FV_VOICENAME.scm |
           sed 's/-- //'  | awk '{printf("( %s )\n",$0)}' \
            >$VOICEDIR/festival/clunits/$FV_VOICENAME.unitlabeled.scm
        # crazy formatting to exactly match diff of flite_sort
        festival --heap 5000000 -b \
            $HELPERDIR/qsort.scm \
            '(begin (set! clindex (load
                "'$VOICEDIR/festival/clunits/$FV_VOICENAME.unitlabeled.scm'" t))
             (set! clindex (qsort clindex carstring<? carstring=?))
             (while (not (null? clindex))
                ;(format t "%l\n" (cadr (car clindex)))
                (set! x (car (cdr (car clindex))))
                (format t "( %l %l %f %f %f %l %l ) \n"
                    (nth 0 x) (nth 1 x) (nth 2 x)
                    (nth 3 x) (nth 4 x) (nth 5 x) (nth 6 x))
                (set! clindex (cdr clindex))
             ))' > $VOICEDIR/festival/clunits/$FV_VOICENAME.unitordered.scm

        festival --heap 5000000 -b \
            $HELPERDIR/FestVoxClunitsToFreeTTS.scm \
            $HELPERDIR/qsort.scm \
            '(dump_clunits "'$FV_VOICENAME'" "'$VOICEDIR'"
            "'$OUTDIR'" "misc.txt" "unittypes.txt" "cart.txt" "units.txt"
            "lpc.txt" "lpc_header.txt" "mcep.txt" "mcep_header.txt"
            "weights.txt")'
        # parentheses allow script to only temporarily change to $OUTDIR
        (cd $OUTDIR
            rm -f README 2>/dev/null
            rm -f $FV_FULLVOICENAME.txt 2>/dev/null
            echo "The data for the voice $FV_FULLVOICENAME is stored in" > README
            echo "$FV_FULLVOICENAME.txt  All other files may be ignored." >> README
            echo "*** Generated by $0 $1 $2" > $FV_FULLVOICENAME.txt
            echo "*** clunits " `date` >> $FV_FULLVOICENAME.txt
            cat misc.txt unittypes.txt cart.txt units.txt lpc_header.txt \
            lpc.txt mcep_header.txt mcep.txt weights.txt >> $FV_FULLVOICENAME.txt)
}

idx_diphone() {
    echo "Building diphone index"

    sed '1,/EST_Header_End/d' $VOICEDIR/dic/*.est |
    awk '{printf("( %s )\n",$0)}' >$VOICEDIR/dic/diphidx.unsorted.scm
    festival --heap 5000000 -b \
        $HELPERDIR/qsort.scm \
        '(begin (set! diphindex (load
            "'$VOICEDIR/dic/diphidx.unsorted.scm'" t))
         (set! diphindex (qsort diphindex carstring<? carstring=?))
         (while (not (null? diphindex))
            (set! x (car diphindex))
            (format t "( %l %l %l %l %l ) \n"
                (nth 0 x) (nth 1 x) (nth 2 x) (nth 3 x) (nth 4 x))
            (set! diphindex (cdr diphindex))
         ))' > $VOICEDIR/dic/diphidx.scm

    festival --heap 5000000 -b \
        $HELPERDIR/FestVoxDiphoneToFreeTTS.scm \
        '(dump_diphone "'$FV_VOICENAME'" "'$VOICEDIR'"
        "'$OUTDIR'" "header.txt" "data.txt"
        "'$VOICEDIR'/dic/diphidx.scm")'
        # parentheses allow script to only temporarily change to $OUTDIR
        (cd $OUTDIR
            rm -f README 2>/dev/null
            rm -f $FV_FULLVOICENAME.txt 2>/dev/null
            echo "The data for the voice $FV_FULLVOICENAME is stored in" > README
            echo "$FV_FULLVOICENAME.txt  All other files may be ignored." >> README
            echo "*** Generated by $0 $1 $2" > $FV_FULLVOICENAME.txt
            echo "*** clunits " `date` >> $FV_FULLVOICENAME.txt
            cat header.txt data.txt >> $FV_FULLVOICENAME.txt
        )

}

if [ "$2" = "idx" ]; then
    if [ "$FV_TYPE" = "diphone" ]; then
        idx_diphone
    else
        idx_non_diphone
    fi
fi

setGender() {
    while true; do
        echo "Please the number corresponding to the gender of this voice:"
        echo "     0 <Cancel>"
        echo "     1 Neutral"
        echo "     2 Male"
        echo "     3 Female"
        echo "     Q <Quit>: Abort the conversion process."
        read 

        if [ "$REPLY" = "0" ]; then
            break
        elif [ "$REPLY" = "1" ]; then
            VP_GENDER="NEUTRAL"
            break
        elif [ "$REPLY" = "2" ]; then
            VP_GENDER="MALE"
            break
        elif [ "$REPLY" = "3" ]; then
            VP_GENDER="FEMALE"
            break
        elif [ "$REPLY" = "Q" ] || [ "$REPLY" = "q" ]; then
            exit 0
        fi
        #else try again
    done
}

setAge() {
    while true; do
        echo "Please enter the number corresponding to the age of this voice:"
        echo "     0 <Cancel>"
        echo "     1 Neutral:       Voice with an age that is indeterminate."
        echo "     2 Child:         Age roughly up to 12 years."
        echo "     3 Teenager:      Age roughly 13 to 19 years."
        echo "     4 Younger Adult: Age roughly 20 to 40 years."
        echo "     5 Middle Adult:  Age roughly 40 to 60 years."
        echo "     6 Older Adult:   Age roughly 60 years and up."
        echo "     Q <Quit>:        Abort the conversion process."
        read

        if [ "$REPLY" = "0" ]; then
            break
        elif [ "$REPLY" = "1" ]; then
            VP_AGE="NEUTRAL"
            break
        elif [ "$REPLY" = "2" ]; then
            VP_AGE="CHILD"
            break
        elif [ "$REPLY" = "3" ]; then
            VP_AGE="TEENAGER"
            break
        elif [ "$REPLY" = "4" ]; then
            VP_AGE="YOUNGER_ADULT"
            break
        elif [ "$REPLY" = "5" ]; then
            VP_AGE="MIDDLE_ADULT"
            break
        elif [ "$REPLY" = "6" ]; then
            VP_AGE="OLDER_ADULT"
            break
        elif [ "$REPLY" = "Q" ] || [ "$REPLY" = "q" ]; then
            exit 0
        fi
        # else try again
    done
}

if [ "$2" = "install" ]; then
    VP_DOMAIN=$FV_LANG
    VP_ORGANIZATION=$FV_INST
    if [ "$FV_TYPE" = "diphone" ]; then
        if ! [ "$FV_LANG" = "us" ]; then
            echo
            echo "This script can only install US/English voices."
            echo "Please refer to your documentation for instructions on"
            echo "how to procede."
            exit -1;
        fi
        VP_DOMAIN="general"
    elif [ "$FV_TYPE" = "ldom" ] || [ "$FV_TYPE" = "clunits" ]; then
        if [ "$FV_TYPE" = "clunits" ]; then
            VP_DOMAIN="general"
        fi
        echo
        echo "Warning: This script will default to a full us english"
        echo "lexicon.  If you need a different lexicon, you can change"
        echo "that in the java voice directory after the install"
        echo "phase is finished."
        echo "If this is a non US/English voice, you may want to cancel."
        echo
        echo "Press <Enter> to continue, or <Ctrl-C> to cancel"
        read
    else
        echo
        echo "Only diphone, clunits, and ldom types are supported by this operation."
        echo "Aborting."
        exit -1;
    fi


    # defaults
    VP_NAME="$FV_NAME"
    VP_GENDER="NEUTRAL"
    VP_AGE="NEUTRAL"
    VP_DESCRIPTION="description not available"
    VP_FULL_NAME="$FV_VOICENAME"

    while true; do
        echo
        echo
        echo
        echo
        echo
        echo
        echo
        echo
        echo
        echo
        echo "Current properties of this voice:"
        echo "     0 <continue with installation>"
        echo "     1 Name:         '$VP_NAME'"
        echo "     2 Gender:       '$VP_GENDER'"
        echo "     3 Age:          '$VP_AGE'"
        echo "     4 Description:  '$VP_DESCRIPTION'"
        echo "     5 Full Name:    '$VP_FULL_NAME'"
        echo "     6 Domain:       '$VP_DOMAIN'"
        echo "     7 Organization: '$VP_ORGANIZATION'"
        echo "     H <Help>"
        echo "     Q <Quit>:       Abort the conversion process."
        echo
        echo
        echo "Enter the number for the property you would like to change,"
        echo "'0' if everything looks correct, or 'Q' to exit:"
        read

        if [ "$REPLY" = "Q" ] || [ "$REPLY" = "q" ]; then
            exit 0
        elif [ "$REPLY" = "H" ] || [ "$REPLY" = "h" ]; then
            echo
            echo
            echo
            echo
            echo
            echo
            echo "  Name: generally a one-word name by which you want this"
            echo "    voice to be known, such as \"kevin\", \"alan\","
            echo "    or \"dave\"."
            echo "  Description: a sentence or so that describes this voice."
            echo "  Gender: male, female, or neutral"
            echo "  Age: one of: Neutral, Child, Teenager, Younger Adult,"
            echo "    Middle Adult, Older Adult"
            echo "  Full Name: the name that will be used for the FreeTTS"
            echo "    files for this voice.  The Full Name must be unique"
            echo "    name from all other voices in FreeTTS.  It is HIGHLY"
            echo "    recommended that you do NOT change this property unless"
            echo "    it conflicts with an existing voice."
            echo "  Domain: the domain for limited domain voices (such as"
            echo "    \"time\" or \"weather\"), otherwise \"general\"."
            echo "  Organization: the organization which recorded the voice,"
            echo "    such as \"sun\" or \"cmu\"."
            echo "  All properties can be changed manually after the conversion"
            echo "    process, but it is easiest to do it now."
            echo
            echo "Press <Enter> to return to the menu."
            read UNUSED
        elif [ "$REPLY" = "0" ]; then  # only way to exit while loop
            if [ -d "$EN_US_DIR/$VP_FULL_NAME" ]; then
                echo
                echo "Warning: the voice '$FV_VOICENAME' is already installed"
                echo "in this version of FreeTTS.  Please enter the number "
                echo "corresponding to the action you would like to take: "
                echo "     0 Cancel conversion process"
                echo "     1 Over-write existing voice"
                echo "     2 Change your voice's Full Name"
                echo "        (it is recommended to follow a convention similar"
                echo "         to <institution>_<lang/domain>_<name>)"
                read REPLY2

                if [ "$REPLY2" = "0" ]; then
                    exit 0
                elif [ "$REPLY2" = "1" ]; then
                    echo
                    echo "Are you sure you want to over-write the existing"
                    echo "voice?  WARNING: May cause permanent loss of"
                    echo "existing voice!  (Yes/No/Cancel):"
                    read REPLY3

                    if [ "$REPLY3" = "Y" ] || [ "$REPLY3" = "y" ] \
                        || [ "$REPLY3" = "yes" ] || [ "$REPLY3" = "Yes" ] \
                        || [ "$REPLY3" = "YES" ]; then
                        echo
                        echo "***** Over-writing existing voice *****"
                        break # exit while loop
                    fi
                fi
            else
                break # exit while loop
            fi
        elif [ "$REPLY" = "1" ]; then
            echo
            echo "Enter a new name: "
            read VP_NAME
        elif [ "$REPLY" = "2" ]; then
            setGender
        elif [ "$REPLY" = "3" ]; then
            setAge
        elif [ "$REPLY" = "4" ]; then
            echo
            echo "Enter a new Description: "
            read VP_DESCRIPTION
        elif [ "$REPLY" = "5" ]; then
            echo
            echo "Enter a new Full Name: "
            read VP_FULL_NAME
        elif [ "$REPLY" = "6" ]; then
            echo
            echo "Enter a new domain (\"general\" for unlimited domains) : "
            read VP_DOMAIN
        elif [ "$REPLY" = "7" ]; then
            echo
            echo "Enter the organization which created this voice: "
            read VP_ORGANIZATION
        fi
    done

    # start from a clean slate
    rm -rf "$EN_US_DIR/$VP_FULL_NAME" 2>/dev/null
    mkdir "$EN_US_DIR/$VP_FULL_NAME" 2>/dev/null

    if ! [ -d "$EN_US_DIR/$VP_FULL_NAME" ]; then
        echo "Error: Unable to create $EN_US_DIR/$VP_FULL_NAME."
        echo "Aborting."
    fi
    
    # java class names should begin with a capital letter
    VOICEDIRECTORY_CLASS=`echo $VP_NAME | awk '{ print(toupper(substr($0,1,1)) substr($0,2)) }'`"VoiceDirectory"
    FULL_VOICEDIRECTORY_CLASS="com.sun.speech.freetts.en.us.$VP_FULL_NAME.$VOICEDIRECTORY_CLASS"

    (
        echo "Copyright 2003 Sun Microsystems, Inc."
        echo 
        echo "See the file "license.terms" for information on usage and redistribution of"
        echo "this file, and for a DISCLAIMER OF ALL WARRANTIES."
        echo
        echo
        echo "This directory contains a voice imported from FestVox."
        echo "$VP_FULL_NAME.txt is the text version of the voice data."
        echo ".bin and .idx files are compiled versions of this file."
        echo "$VOICEDIRECTORY_CLASS.java is the voice directory which"
        echo "contains information about the voices (or variations on"
        echo "voices) that are provided in this directory.  By default"
        echo "the FestVox to FreeTTS conversion utility only puts one"
        echo "voice in this directory."
        echo
        echo "$VP_FULL_NAME.jar is the file that is created when FreeTTS"
        echo "is compiled.  This jar file will be put in the same directory"
        echo "as the other FreeTTS jar files.  (Generally '<FreeTTS>/lib/')"
        echo "voice.Manifest is used as the Manifest for the jar file."
        echo
        echo "Please confirm that $VOICEDIRECTORY_CLASS.java and"
        echo "voice.Manifest contain the correct information."
        echo "(If you created a ldom voice, it is still configured to use"
        echo "a full US/English lexicon.  You may wish to change that)."
    ) > "$EN_US_DIR/$VP_FULL_NAME/README"



    cp -f "$OUTDIR/$FV_FULLVOICENAME.txt" "$EN_US_DIR/$VP_FULL_NAME/$VP_FULL_NAME.txt"
    echo "Main-Class: $FULL_VOICEDIRECTORY_CLASS" > "$EN_US_DIR/$VP_FULL_NAME/voice.Manifest"
    echo "FreeTTSVoiceDefinition: true" >> "$EN_US_DIR/$VP_FULL_NAME/voice.Manifest"
    echo "Class-Path: cmulex.jar" >> "$EN_US_DIR/$VP_FULL_NAME/voice.Manifest"

    if [ "$FV_TYPE" = "diphone" ]; then
        VD_TEMPLATE="$HELPERDIR/CMU_USDiphoneTemplate.java.template"
        UNIT_DATABASE_CLASS="com.sun.speech.freetts.diphone.DiphoneUnitDatabase"
        MAKEFILE_EXCLUDE="CLUNITS_ONLY"
    else #clunit
        VD_TEMPLATE="$HELPERDIR/CMU_USClunitTemplate.java.template"
        UNIT_DATABASE_CLASS="com.sun.speech.freetts.clunits.ClusterUnitDatabase"
        MAKEFILE_EXCLUDE="DIPHONE_ONLY"
    fi

    # create the voice directory class
    cat $VD_TEMPLATE | sed "s/%CLASSNAME%/$VOICEDIRECTORY_CLASS/g" \
        | sed "s/%VOICENAME%/$VP_FULL_NAME/g" \
        | sed "s/%NAME%/$VP_NAME/g" \
        | sed "s/%GENDER%/$VP_GENDER/g" \
        | sed "s/%AGE%/$VP_AGE/g" \
        | sed "s/%DESCRIPTION%/$VP_DESCRIPTION/g" \
        | sed "s/%DOMAIN%/$VP_DOMAIN/g" \
        | sed "s/%ORGANIZATION%/$VP_ORGANIZATION/g" \
        > "$EN_US_DIR/$VP_FULL_NAME/$VOICEDIRECTORY_CLASS.java"

    echo "The voice has been successfully installed in"
    echo "$EN_US_DIR/$VP_FULL_NAME/"
fi
