# Copyright 2001 Sun Microsystems, Inc.
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
	./waveTimeTest.sh
	./wavetest.sh

lpctest: all
	@chmod +x lpctest.sh
	./lpctest.sh

utttest: all
	@chmod +x utteranceTest.sh compareNumericUtt compareUtt testUtt utteranceTimeTest.sh testTimeUtt compareNumericUtt
	./utteranceTest.sh
	./utteranceTimeTest.sh


alltests: all
	(cd $(TOP)/bin; $(MAKE))
	@$(MAKE) tests
	@$(MAKE) utttest
	@$(MAKE) lpctest
	@$(MAKE) wavetest

clean::
	rm -f *.1 *.2 *.res *.diff timeTest.wave
