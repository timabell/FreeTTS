# Copyright 2001 Sun Microsystems, Inc.
# All Rights Reserved.  Use is subject to license terms.
# 
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL 
# WARRANTIES.
#

# Relative path to the top of the source tree
TOP = ../../../../..
PACKAGE_PATH = com/sun/speech/freetts/jsapi

# By default, build all of the .java files. 
FILES = $(shell echo *.java)

# List any sub directories that need to be built
SUBDIRS = 

##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/${PACKAGE_PATH}/%.class}

RESOURCE_FILES =  synthesizer.properties
RESOURCES = ${RESOURCE_FILES:%=${CLASS_DEST_DIR}/${PACKAGE_PATH}/%}

all:: ${CLASSES} ${STUBS} ${RESOURCES}

${CLASS_DEST_DIR}/${PACKAGE_PATH}/%.properties: %.properties
	@mkdir -p ${CLASS_DEST_DIR}/${PACKAGE_PATH}
	$(RM) ${CLASS_DEST_DIR}/${PACKAGE_PATH}/$?
	cp $? ${CLASS_DEST_DIR}/${PACKAGE_PATH}


