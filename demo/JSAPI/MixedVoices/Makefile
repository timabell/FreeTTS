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
SUBDIRS = 

# Use a separate classes directory
CLASS_DEST_DIR = ${TOP}/demo/classes

##########################################################################

include ${TOP}/build/Makefile.config


CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/%.class}

all:: ${CLASSES} ${STUBS}

run: 
	${TOP}/bin/baseFreeTTS MixedVoices

runVerbose: 
	${TOP}/bin/baseFreeTTS MixedVoices -showPropertyChanges

runVeryVerbose: 
	${TOP}/bin/baseFreeTTS MixedVoices -showPropertyChanges -showEvents

