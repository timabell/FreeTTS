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

# To use another synthesizer, e.g., ViaVoice, change the System property
# "synthesizerName".

run: all
	$(JAVA_CMD) -Dport=2222 -DsynthesizerName="Unlimited domain FreeTTS Speech Synthesizer from Sun Labs" EmacspeakServer
