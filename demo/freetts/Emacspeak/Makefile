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
SUBDIRS = ../../util

##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/%.class}

all:: ${CLASSES} ${STUBS}

run: 
	$(JAVA_CMD) -Ddebug=true -Dport=2222 FreeTTSEmacspeakServer

runMbrola1:
	$(JAVA_CMD) \
	-Ddebug=true \
	-Dmbrola.base=$(MBROLA_DIR) \
	-Dport=2222 \
	-DvoiceClass=de.dfki.lt.freetts.en.us.MbrolaVoiceUS1 \
	FreeTTSEmacspeakServer

runMbrola2:
	$(JAVA_CMD) \
	-Ddebug=true \
	-Dmbrola.base=$(MBROLA_DIR) \
	-Dport=2222 \
	-DvoiceClass=de.dfki.lt.freetts.en.us.MbrolaVoiceUS2 \
	FreeTTSEmacspeakServer

runMbrola3:
	$(JAVA_CMD) \
	-Ddebug=true \
	-Dmbrola.base=$(MBROLA_DIR) \
	-Dport=2222 \
	-DvoiceClass=de.dfki.lt.freetts.en.us.MbrolaVoiceUS3 \
	FreeTTSEmacspeakServer
