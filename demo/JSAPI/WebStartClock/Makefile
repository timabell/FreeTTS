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
EN_US_DIR = com/sun/speech/freetts/en/us
AUDIO_DIR = com/sun/speech/freetts/audio

WEBSTARTCLOCK_CLASSES = \
	classes/Clock*.class \
	classes/JSAPIClock*.class \
	classes/TimeUtils.class \
	classes/com

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

all:: ${CLASSES} ${STUBS}

run: all
	$(JAVA_CMD) JSAPIClock

wsc.jar:
	rm -f wsc.jar
	rm -f jsapi.jar
	rm -f clockKey
	(cd $(TOP); \
	$(MAKE) all; \
	mkdir $(WSC_STAGING_AREA); \
	cp -r $(WEBSTARTCLOCK_CLASSES) $(WSC_STAGING_AREA); \
	rm -rf $(WEBSTARTCLOCK_EXCLUDES); \
	(cd $(WSC_STAGING_AREA); $(JAR) cvf wsc.jar *); \
	mv $(WSC_STAGING_AREA)/wsc.jar $(PWD); \
	rm -rf $(WSC_STAGING_AREA); )
	chmod a+x wsc.jar
	keytool -genkey -keystore clockKey -alias freetts
	jarsigner -keystore clockKey wsc.jar freetts 
	cp $(TOP)/lib/jsapi.jar .
	jarsigner -keystore clockKey jsapi.jar freetts

runjar: all
	java -cp "./wsc.jar$(CSEP)../../../lib/jsapi.jar" JSAPIClock

