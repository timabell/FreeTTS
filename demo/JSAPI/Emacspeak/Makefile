# Copyright 2003 Sun Microsystems, Inc.
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
SUBDIRS = ../../util

##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/%.class}

all:: ${CLASSES} ${STUBS}

# To use another synthesizer, e.g., ViaVoice, change the System property
# "synthesizerName".

run: 
	${TOP}/bin/baseFreeTTS \
	-Ddebug=true \
	-Dport=2222 \
	-DvoiceName="kevin16" \
	EmacspeakServer

runMbrola1:
	${TOP}/bin/baseFreeTTS \
	-Dmbrola.base=$(MBROLA_DIR) \
	-Ddebug=true \
	-Dport=2222 \
	-DvoiceName="mbrola1" \
	EmacspeakServer

runMbrola2:
	${TOP}/bin/baseFreeTTS \
	-Dmbrola.base=$(MBROLA_DIR) \
	-Ddebug=true \
	-Dport=2222 \
	-DvoiceName="mbrola2" \
	EmacspeakServer

runMbrola3:
	${TOP}/bin/baseFreeTTS \
	-Dmbrola.base=$(MBROLA_DIR) \
	-Ddebug=true \
	-Dport=2222 \
	-DvoiceName="mbrola3" \
	EmacspeakServer
