# Copyright 2001 Sun Microsystems, Inc.
# All Rights Reserved.  Use is subject to license terms.
# 
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL 
# WARRANTIES.
#

# Relative path to the top of the source tree
TOP = ../../..

# By default, build all of the .java files. 
FILES = $(shell echo *.java)

# List any sub directories that need to be built
SUBDIRS = 

##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/%.class}

all:: ${CLASSES} ${STUBS}

run: all
	$(JAVA_CMD) FreeTTSHelloWorld 

runMbrola1: all
	$(JAVA_CMD) \
	-Dmbrola.base=$(MBROLA_DIR) \
	FreeTTSHelloWorld de.dfki.lt.freetts.en.us.MbrolaVoiceUS1

runMbrola2: all
	$(JAVA_CMD) \
	-Dmbrola.base=$(MBROLA_DIR) \
	FreeTTSHelloWorld de.dfki.lt.freetts.en.us.MbrolaVoiceUS2

runMbrola3: all
	$(JAVA_CMD) \
	-Dmbrola.base=$(MBROLA_DIR) \
	FreeTTSHelloWorld de.dfki.lt.freetts.en.us.MbrolaVoiceUS3

audio: all
	$(JAVA_CMD) Audio

audioOutput: all
	$(JAVA_CMD) AudioOutput audio.out

convert: all
	$(JAVA_CMD) JavaSoundWavTest