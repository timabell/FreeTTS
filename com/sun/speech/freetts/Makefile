# Copyright 2001 Sun Microsystems, Inc.
# All Rights Reserved.  Use is subject to license terms.
# 
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL 
# WARRANTIES.
#

# Relative path to the top of the source tree
TOP = ../../../..
PACKAGE_PATH = com/sun/speech/freetts
DOC_DEST = /lab/speech/web/freetts

# By default, build all of the .java files. 
FILES = $(shell echo *.java)

# List any sub directories that need to be built
SUBDIRS = audio diphone clunits en cart lexicon util relp jsapi

##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/${PACKAGE_PATH}/%.class}

all:: ${CLASSES} ${STUBS}

${CLASS_DEST_DIR}/${PACKAGE_PATH}/%.txt: %.txt
	@mkdir -p ${CLASS_DEST_DIR}/${PACKAGE_PATH}
	$(RM) ${CLASS_DEST_DIR}/${PACKAGE_PATH}/$?
	cp $? ${CLASS_DEST_DIR}/${PACKAGE_PATH}

