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
	$(JAVA_CMD) HelloWorld

runMbrola1: all
	$(JAVA_CMD) \
	-Dmbrola.base=$(MBROLA_DIR) \
	-DsynthesizerName="FreeTTS MBROLA Speech Synthesizer from DFKI" \
	-DvoiceName="mbrola1" \
	HelloWorld

runMbrola2: all
	$(JAVA_CMD) \
	-Dmbrola.base=$(MBROLA_DIR) \
	-DsynthesizerName="FreeTTS MBROLA Speech Synthesizer from DFKI" \
	-DvoiceName="mbrola2" \
	HelloWorld

runMbrola3: all
	$(JAVA_CMD) \
	-Dmbrola.base=$(MBROLA_DIR) \
	-DsynthesizerName="FreeTTS MBROLA Speech Synthesizer from DFKI" \
	-DvoiceName="mbrola3" \
	HelloWorld
