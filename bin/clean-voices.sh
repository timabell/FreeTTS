#!/bin/bash
TOP=`dirname $0`/..
EN_US_DIR=$TOP/com/sun/speech/freetts/en/us

JARS=`ls $TOP/lib/*.jar`
RM_JARS=""
for vj in $JARS; do
    if [ "$vj" != "$TOP/lib/jsapi.jar" ]; then
        RM_JARS="$RM_JARS $vj"
    fi
done

RM_VOICES=""

for vd in $@; do
    if ! grep $vd ${TOP}/build/clean-voices.list >/dev/null 2>/dev/null; then
        RM_VOICES="$RM_VOICES $EN_US_DIR/$vd"
    fi
done

echo "Warning: about to delete:"
echo "     `echo $RM_JARS | sed 's/ /\n     /g'`"
echo "  Voices:"
echo "     `echo $RM_VOICES | sed 's/ /\n     /g'`"
echo "  And replace lib/voices.txt"
echo
echo "Press <Enter> to continue or <Ctrl-C> to cancel."
read



rm -f $RM_JARS
rm -rf $RM_VOICES

cp -f $TOP/build/clean-voices.txt $TOP/lib/voices.txt
