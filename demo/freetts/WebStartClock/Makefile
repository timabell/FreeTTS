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
SUBDIRS = 



########### WebStartClock deploy macros ########################

WSC_STAGING_AREA = ./WebStartClock

WEBSTARTCLOCK_CLASSES = \
	classes/*.class \
	classes/com

WEBSTARTCLOCK_EXCLUDES = \
	$(WSC_STAGING_AREA)/Player*.class \
	$(WSC_STAGING_AREA)/tests/ \
	$(WSC_STAGING_AREA)/com/sun/speech/engine/ \
	$(WSC_STAGING_AREA)/com/sun/speech/freetts/en/us/cmu_kal/


##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/%.class}

all:: ${CLASSES} ${STUBS}

run: all
	$(JAVA_CMD) WebStartClock

webstartclock.jar:
	rm -rf webstartclock.jar
	(cd $(TOP); \
	$(MAKE) all; \
	mkdir $(WSC_STAGING_AREA); \
	cp -r $(WEBSTARTCLOCK_CLASSES) $(WSC_STAGING_AREA); \
	rm -rf $(WEBSTARTCLOCK_EXCLUDES); \
	(cd $(WSC_STAGING_AREA); $(JAR) cvf webstartclock.jar *); \
	mv $(WSC_STAGING_AREA)/webstartclock.jar $(PWD); \
	rm -rf $(WSC_STAGING_AREA); \
	cd $(PWD))
	chmod a+x webstartclock.jar
	keytool -genkey -keystore clockKey -alias freetts
	jarsigner -keystore clockKey webstartclock.jar freetts 
