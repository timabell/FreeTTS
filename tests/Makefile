# Copyright 2001-2003 Sun Microsystems, Inc.
# All Rights Reserved.  Use is subject to license terms.
# 
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL 
# WARRANTIES.
#
# @(#)Makefile	1.2 01/07/12

# Relative path to the top of the source tree
TOP = ..
PACKAGE_PATH = tests

# Change to compare against output from other versions of flite
#FLITE_VERSION=v1.0-beta
#FLITE_VERSION=v1.1-beta
#FLITE_VERSION=v1.1
FLITE_VERSION=v1.2-release

TIME_INPUT=06:51

# By default, build all of the .java files. 
FILES = $(shell echo *.java)
# List any sub directories that need to be built
SUBDIRS = 

TEST_SUITE=tests.AllTests
GUI_TEST_SUITE=junit.swingui.TestRunner $(TEST_SUITE)
TEXT_TEST_SUITE=junit.ui.TestRunner $(TEST_SUITE)



##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/${PACKAGE_PATH}/%.class}

all:: ${CLASSES} ${STUBS}

wavetest: all
	@chmod +x wavetest.sh waveTimeTest.sh
	sh ./waveTimeTest.sh ${TIME_INPUT} ../data/flite.${FLITE_VERSION}.wave.time.float.txt
	sh ./wavetest.sh ../wave/08-01-01.wave.text ../data/flite.${FLITE_VERSION}.wave.first.float.txt

lpctest: all
	@chmod +x lpctest.sh
	sh ./lpctest.sh

utttest: all
	@chmod +x utteranceTest.sh compareNumericUtt compareUtt testUtt utteranceTimeTest.sh testTimeUtt compareNumericUtt
	sh ./utteranceTest.sh ../data/alice2 ../data/flite.${FLITE_VERSION}.alice2.rel
	sh ./utteranceTimeTest.sh  ${TIME_INPUT} ../data/flite.${FLITE_VERSION}.time.rel 
	sh ./utteranceTest.sh ../data/TokenGame.txt ../data/flite.${FLITE_VERSION}.TokenGame.rel



alltests: all
	(cd $(TOP)/bin; $(MAKE))
	@$(MAKE) tests
	@$(MAKE) utttest
	@$(MAKE) lpctest
	@$(MAKE) wavetest

clean::
	rm -f *.1 *.2 *.res *.diff timeTest.wave
