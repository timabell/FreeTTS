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

# Use a separate classes directory
CLASS_DEST_DIR = ${TOP}/demo/classes


##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/%.class}

all:: ${CLASSES} ${STUBS}

run: 
	${TOP}/bin/baseFreeTTS -Ddebug=true -Dport=2222 FreeTTSEmacspeakServer

runMbrola1:
	${TOP}/bin/baseFreeTTS \
	-Ddebug=true \
	-DvoiceName=mbrola1
	-Dmbrola.base=$(MBROLA_DIR) \
	-Dport=2222 \
	FreeTTSEmacspeakServer

runMbrola2:
	${TOP}/bin/baseFreeTTS \
	-Ddebug=true \
	-DvoiceName=mbrola2
	-Dmbrola.base=$(MBROLA_DIR) \
	-Dport=2222 \
	FreeTTSEmacspeakServer

runMbrola3:
	${TOP}/bin/baseFreeTTS \
	-Ddebug=true \
	-DvoiceName=mbrola3
	-Dmbrola.base=$(MBROLA_DIR) \
	-Dport=2222 \
	FreeTTSEmacspeakServer
