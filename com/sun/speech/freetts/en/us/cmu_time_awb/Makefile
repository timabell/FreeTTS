# Copyright 2001 Sun Microsystems, Inc.
# All Rights Reserved.  Use is subject to license terms.
# 
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL 
# WARRANTIES.
#

# Relative path to the top of the source tree
TOP = ../../../../../../..
PACKAGE_PATH = com/sun/speech/freetts/en/us/cmu_time_awb
DOC_DEST = /lab/speech/web/freetts

# By default, build all of the .java files. 
FILES = $(shell echo *.java)

# List any sub directories that need to be built
SUBDIRS = 

##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/${PACKAGE_PATH}/%.class}

DATA_FILES =  cmu_time_awb.txt voice.Manifest
BINARY_FILES = cmu_time_awb.bin

DATA = ${DATA_FILES:%=${CLASS_DEST_DIR}/${PACKAGE_PATH}/%}
INSTALLED_BINARY_FILES = ${BINARY_FILES:%=${CLASS_DEST_DIR}/${PACKAGE_PATH}/%}

${CLASS_DEST_DIR}/${PACKAGE_PATH}/%.txt: %.txt
	@mkdir -p ${CLASS_DEST_DIR}/${PACKAGE_PATH}
	$(RM) ${CLASS_DEST_DIR}/${PACKAGE_PATH}/$?
	cp $? ${CLASS_DEST_DIR}/${PACKAGE_PATH}

${CLASS_DEST_DIR}/${PACKAGE_PATH}/%.Manifest: %.Manifest
	@mkdir -p ${CLASS_DEST_DIR}/${PACKAGE_PATH}
	$(RM) ${CLASS_DEST_DIR}/${PACKAGE_PATH}/$?
	cp $? ${CLASS_DEST_DIR}/${PACKAGE_PATH}



all:: ${CLASSES} ${DATA} $(INSTALLED_BINARY_FILES)

binaries: all $(INSTALLED_BINARY_FILES)

verify_binaries: all
	$(JAVA_CMD) com.sun.speech.freetts.clunits.ClusterUnitDatabase -compare -showtimes

${CLASS_DEST_DIR}/${PACKAGE_PATH}/cmu_time_awb.bin:	cmu_time_awb.txt
	$(JAVA_CMD) com.sun.speech.freetts.clunits.ClusterUnitDatabase -generate_binary
	$(RM) $@
	cp `basename $@`  ${CLASS_DEST_DIR}/${PACKAGE_PATH}

clean::
	$(RM) ${CLASS_DEST_DIR}/${PACKAGE_PATH}/*.txt
	$(RM) $(INSTALLED_BINARY_FILES)

release_doc:
	$(JDK)/bin/javadoc -windowtitle "FreeTTS" -d $(DOC_DEST)  $(FILES)
