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

GTAR=/pkg/gnu/bin/tar

PUSH_DEST = /home/groups/f/fr/freetts/htdocs/WebStartClock

# Use a separate classes directory
CLASS_DEST_DIR = ${TOP}/demo/classes

########### WebStartClock deploy macros ########################

WSC_STAGING_AREA = ./WebStartClock
EN_US_DIR = com/sun/speech/freetts/en/us
AUDIO_DIR = com/sun/speech/freetts/audio
KEY=clockKey

WEBSTARTCLOCK_CLASSES = \
	classes/Clock*.class \
	classes/JSAPIClock*.class \
	classes/TimeUtils.class \
	classes/com \
	classes/de

WEBSTARTCLOCK_EXCLUDES = \
	$(WSC_STAGING_AREA)/com/sun/speech/freetts/diphone \
	$(WSC_STAGING_AREA)/com/sun/speech/freetts/FreeTTSTime.class \
	$(WSC_STAGING_AREA)/$(EN_US_DIR)/cmu_kal/ \
	$(WSC_STAGING_AREA)/$(EN_US_DIR)/cmulex_compiled.bin \
	$(WSC_STAGING_AREA)/$(EN_US_DIR)/cmulex_compiled.txt \
	$(WSC_STAGING_AREA)/$(EN_US_DIR)/cmulex_addenda.bin \
	$(WSC_STAGING_AREA)/$(EN_US_DIR)/cmulex_addenda.txt \
	$(WSC_STAGING_AREA)/$(EN_US_DIR)/cmulex_lts.bin \
	$(WSC_STAGING_AREA)/$(EN_US_DIR)/cmulex_lts.txt \
	$(WSC_STAGING_AREA)/$(EN_US_DIR)/cmutimelex_compiled.txt \
	$(WSC_STAGING_AREA)/$(EN_US_DIR)/cmutimelex_addenda.txt \
	$(WSC_STAGING_AREA)/$(EN_US_DIR)/cmutimelex_lts.txt \
	$(WSC_STAGING_AREA)/$(EN_US_DIR)/cmu_awb/cmu_time_awb.txt \
	$(WSC_STAGING_AREA)/$(EN_US_DIR)/CMUDiphoneVoice.class \
	$(WSC_STAGING_AREA)/$(AUDIO_DIR)/AudioPlayerStdOut.class \
	$(WSC_STAGING_AREA)/$(AUDIO_DIR)/JavaStreamingAudioPlayer.class \
	$(WSC_STAGING_AREA)/$(AUDIO_DIR)/MultiFile8BitAudioPlayer.class \
	$(WSC_STAGING_AREA)/$(AUDIO_DIR)/MultiFileAudioPlayer.class \
	$(WSC_STAGING_AREA)/$(AUDIO_DIR)/NullAudioPlayer.class \
	$(WSC_STAGING_AREA)/$(AUDIO_DIR)/RawFileAudioPlayer.class \
	$(WSC_STAGING_AREA)/$(AUDIO_DIR)/SingleFileAudioPlayer.class


##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/%.class}
PUSH_FILES = wsc.jar jsapi.jar clockKey clock.jnlp
TAR_FILE=webstartclock.tar

all:: ${CLASSES} ${STUBS}

run: all
	${TOP}/bin/baseFreeTTS JSAPIClock

########################################
# Builds and signs the jars
########################################
jars:
	$(MAKE) unsignedjars
	$(MAKE) key
	$(MAKE) signjars

########################################
# Builds an unsigned jar
########################################
unsignedjars:
	rm -f wsc.jar
	rm -f jsapi.jar
	rm -f clockKey
	(cd $(TOP); \
	$(MAKE) all; \
	mkdir $(WSC_STAGING_AREA); \
	cp -r $(WEBSTARTCLOCK_CLASSES) $(WSC_STAGING_AREA); \
	rm -rf $(WEBSTARTCLOCK_EXCLUDES); \
	(cd $(WSC_STAGING_AREA); $(JAR) cvf wsc.jar *); \
	chmod a+x $(WSC_STAGING_AREA)/wsc.jar; \
	mv $(WSC_STAGING_AREA)/wsc.jar $(CURDIR); \
	rm -rf $(WSC_STAGING_AREA); )


########################################
# Builds the key file
########################################
key:
	rm -f $(KEY)
	$(JAVA_HOME)/bin/keytool -keypass freetts -storepass freetts -genkey  -keystore $(KEY) -alias freetts -dname "CN=Sun Labs, OU=Sun Microsystems, O=Sun Microsystems, L=Burlington, ST=MA, C=US"

########################################
# Signs the jar files
########################################
signjars:
	cp $(TOP)/lib/jsapi.jar .
	$(JAVA_HOME)/bin/jarsigner -storepass freetts -keypass freetts -keystore clockKey wsc.jar freetts 
	$(JAVA_HOME)/bin/jarsigner -storepass freetts -keypass freetts  -keystore clockKey jsapi.jar freetts 

	# jarsigner -keystore clockKey wsc.jar freetts 

runjar: all
	${TOP}/bin/baseFreeTTS -cp "./wsc.jar$(CSEP)../../../lib/jsapi.jar" JSAPIClock


###############################################
# Pushes out the webstart files to sourceforge
###############################################
push:
	$(GTAR) cf $(TAR_FILE) $(PUSH_FILES)
	sscp $(TAR_FILE)
	sshh tar xfC $(TAR_FILE) $(PUSH_DEST)
	rm $(TAR_FILE)

###############################################
# Cleans out the cruft
###############################################

clean::
	rm -f wsc.jar
	rm -f jsapi.jar
	rm -f $(KEY)
