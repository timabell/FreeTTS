# Copyright 2001 Sun Microsystems, Inc.
# All Rights Reserved.  Use is subject to license terms.
# 
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL 
# WARRANTIES.
#
# @(#)Makefile	1.3 01/07/16

# Relative path to the top of the source tree
TOP = ../../../../../..
PACKAGE_PATH = com/sun/speech/freetts/en/us
DOC_DEST = /lab/speech/web/freetts

# By default, build all of the .java files. 
FILES = $(shell echo *.java)

# List any sub directories that need to be built
SUBDIRS:=$(shell ls -l | egrep "^d" | awk '{ print $$9 }' | grep -v CVS)

##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/${PACKAGE_PATH}/%.class}

DATA_FILES = phrasing_cart.txt durz_cart.txt dur_stat.txt \
	     int_tone_cart.txt int_accent_cart.txt \
	     part_of_speech.txt phoneset.txt f0_lr_terms.txt \
	     cmulex_compiled.txt cmulex_addenda.txt cmulex_lts.txt \
	     cmutimelex_compiled.txt cmutimelex_addenda.txt cmutimelex_lts.txt \
	     nums_cart.txt prefix_fsm.txt suffix_fsm.txt \
	     en_us.Manifest cmutimelex.Manifest cmulex.Manifest

DATA = ${DATA_FILES:%=${CLASS_DEST_DIR}/${PACKAGE_PATH}/%}

BINARY_FILES = cmulex_compiled.bin cmulex_addenda.bin  cmulex_lts.bin \
		cmutimelex_compiled.bin cmutimelex_addenda.bin \
		cmutimelex_lts.bin 

BINARY = ${BINARY_FILES:%=${CLASS_DEST_DIR}/${PACKAGE_PATH}/%}

${CLASS_DEST_DIR}/${PACKAGE_PATH}/%.txt: %.txt
	@mkdir -p ${CLASS_DEST_DIR}/${PACKAGE_PATH}
	$(RM) ${CLASS_DEST_DIR}/${PACKAGE_PATH}/$?
	cp $? ${CLASS_DEST_DIR}/${PACKAGE_PATH}

${CLASS_DEST_DIR}/${PACKAGE_PATH}/%.Manifest: %.Manifest
	@mkdir -p ${CLASS_DEST_DIR}/${PACKAGE_PATH}
	$(RM) ${CLASS_DEST_DIR}/${PACKAGE_PATH}/$?
	cp $? ${CLASS_DEST_DIR}/${PACKAGE_PATH}

all:: ${CLASSES} ${STUBS} ${DATA} $(BINARY)

clean::
	$(RM) ${CLASS_DEST_DIR}/${PACKAGE_PATH}/*.txt
	$(RM) ${CLASS_DEST_DIR}/${PACKAGE_PATH}/*.bin
	$(RM) $(BINARY)

binaries: all $(BINARY)

verify_binaries: all binaries
	$(JAVA_CMD) com.sun.speech.freetts.en.us.CMULexicon -compare -showtimes
	$(JAVA_CMD) com.sun.speech.freetts.lexicon.LetterToSoundImpl -compare -showtimes


${CLASS_DEST_DIR}/${PACKAGE_PATH}/cmulex_compiled.bin:	cmulex_compiled.txt
	$(JAVA_CMD) com.sun.speech.freetts.en.us.CMULexicon -generate_binary
	$(RM) $@
	cp `basename $@`  ${CLASS_DEST_DIR}/${PACKAGE_PATH}
	$(RM) -f cmulex_compiled.bin cmulex_addenda.bin

${CLASS_DEST_DIR}/${PACKAGE_PATH}/cmulex_addenda.bin:	cmulex_addenda.txt
	$(JAVA_CMD) com.sun.speech.freetts.en.us.CMULexicon -generate_binary
	$(RM) $@
	cp cmulex_addenda.bin ${CLASS_DEST_DIR}/${PACKAGE_PATH}
	$(RM) `basename $@`
	$(RM) -f cmulex_compiled.bin cmulex_addenda.bin

${CLASS_DEST_DIR}/${PACKAGE_PATH}/cmutimelex_addenda.bin: cmutimelex_addenda.txt
	$(JAVA_CMD) com.sun.speech.freetts.en.us.CMULexicon -name cmutimelex -generate_binary
	$(RM) $@
	cp cmutimelex_addenda.bin ${CLASS_DEST_DIR}/${PACKAGE_PATH}
	$(RM) cmutimelex_addenda.bin cmutimelex_compiled.bin

${CLASS_DEST_DIR}/${PACKAGE_PATH}/cmutimelex_compiled.bin: cmutimelex_compiled.txt
	$(JAVA_CMD) com.sun.speech.freetts.en.us.CMULexicon -name cmutimelex -generate_binary
	$(RM) $@
	cp cmutimelex_compiled.bin ${CLASS_DEST_DIR}/${PACKAGE_PATH}
	$(RM) cmutimelex_addenda.bin cmutimelex_compiled.bin

${CLASS_DEST_DIR}/${PACKAGE_PATH}/cmulex_lts.bin:	cmulex_lts.txt
	$(JAVA_CMD) com.sun.speech.freetts.lexicon.LetterToSoundImpl -name cmulex_lts -generate_binary
	$(RM) $@
	cp cmulex_lts.bin ${CLASS_DEST_DIR}/${PACKAGE_PATH}
	$(RM) `basename $@`

${CLASS_DEST_DIR}/${PACKAGE_PATH}/cmutimelex_lts.bin:	cmutimelex_lts.txt
	$(JAVA_CMD) com.sun.speech.freetts.lexicon.LetterToSoundImpl -name cmutimelex_lts -generate_binary
	$(RM) $@
	cp cmutimelex_lts.bin ${CLASS_DEST_DIR}/${PACKAGE_PATH}
	$(RM) `basename $@`

release_doc:
	$(JDK)/bin/javadoc -windowtitle "FreeTTS" -d $(DOC_DEST)  $(FILES)
