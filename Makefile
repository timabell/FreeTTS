# Copyright 2001 Sun Microsystems, Inc.docs/
# All Rights Reserved.  Use is subject to license terms.
# 
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL 
# WARRANTIES.
#

# Relative path to the "base" of the source tree
TOP = .

GTAR = /pkg/gnu/bin/tar
ZIP = zip

# List any sub directories that need to be built
SUBDIRS = com de demo bin tests docs

# List src dirs that get deployed
# SRCDIRS = javax com
SRCDIRS = com de
TESTSDIRS = wave data bin tests


PUSH_DEST_DOC = /home/groups/f/fr/freetts/
PUSH_DEST_DOC_TEST = /home/groups/f/fr/freetts/htdocs/test


#JARS = lib/freetts.jar lib/cmulex.jar lib/cmukal8.jar lib/cmukal16.jar \
#	lib/cmuawb.jar lib/jsapi.jar 

JARS = lib/freetts.jar lib/cmulex.jar lib/cmukal8.jar lib/cmukal16.jar \
	lib/cmuawb.jar lib/cmutimelex.jar lib/demo.jar

WSC_STAGING_AREA = ./WebStartClock

WEB_START_CLOCK_FILES = $(WSC_STAGING_AREA)/wsc.jar \
			$(WSC_STAGING_AREA)/jsapi.jar\
			$(WSC_STAGING_AREA)/wsc.key \
			$(WSC_STAGING_AREA)/clock.jnlp

ZIPS = javadoc.zip src.zip tests.zip

TARS = 

API_DIR = docs/api

CMULEX_FILES =  com/sun/speech/freetts/en/us/cmulex_compiled.bin \
		com/sun/speech/freetts/en/us/cmulex_addenda.bin  \
		com/sun/speech/freetts/en/us/cmulex_lts.bin  

CMUTIME_FILES= com/sun/speech/freetts/en/us/cmutimelex_compiled.bin \
		com/sun/speech/freetts/en/us/cmutimelex_addenda.bin  \
		com/sun/speech/freetts/en/us/cmutimelex_lts.bin  \
   
CMUKAL8_FILES = com/sun/speech/freetts/en/us/cmu_kal/diphone_units.bin \
		com/sun/speech/freetts/en/us/cmu_kal/diphone_units.idx 

CMUKAL16_FILES = com/sun/speech/freetts/en/us/cmu_kal/diphone_units16.bin \
		 com/sun/speech/freetts/en/us/cmu_kal/diphone_units16.idx 

CMUAWB_FILES = com/sun/speech/freetts/en/us/cmu_awb/cmu_time_awb.bin 


JSAPI_FILES = javax

DEPLOY_FILES =  \
	    build \
	    demo \
	    docs \
	    lib \
	    $(ZIPS) \
	    speech.properties \
	    license.terms \
	    acknowledgments.txt \
	    RELEASE_NOTES \
	    README.txt \
	    overview.html \
	    index.html \
	    Makefile

DEPLOY_TARGET = freetts.tar.gz
STAGING_AREA = ./FreeTTS

# These files are removed from the binary distribution
DEPLOY_EXCLUDED_FILES = \
	$(STAGING_AREA)/docs/*copyright.txt	

DEPLOY_EXCLUDED_DIRECTORIES = \
	$(STAGING_AREA)/demo/NuanceClock	\
	$(STAGING_AREA)/lib/jsapi.jar


DEPLOY_DOCS_TARGET = freettsdocs.tar.gz
DOC_STAGING_AREA = ./htdocs
DEPLOY_DOCS = demo docs license.terms RELEASE_NOTES overview.html README.txt acknowledgments.txt index.html license.terms
DEPLOY_DOCS_EXCLUDED_FILES = $(DOC_STAGING_AREA)/XXX


########### EmacsServer deploy macros ########################
EMACS_SERVER_FILES =  		\
	    acknowledgments.txt \
	    build 		\
	    demo 		\
	    license.terms 	\
	    Makefile		\
	    README.txt 		\
	    RELEASE_NOTES 	\
	    docs/emacspeak_index.html \
	    $(SRCDIRS) 		\


EMACS_SERVER_EXCLUDES =  				\
	    $(STAGING_AREA)/demo/JSAPI 			\
	    $(STAGING_AREA)/demo/NuanceClock		\
	    $(STAGING_AREA)/demo/freetts/ClientServer	\
	    $(STAGING_AREA)/demo/freetts/HelloWorld	\
	    $(STAGING_AREA)/com/sun/speech/engine 	\
	    $(STAGING_AREA)/com/sun/speech/freetts/jsapi \
	    $(STAGING_AREA)/com/sun/speech/freetts/clunits \
	    $(STAGING_AREA)/com/sun/speech/freetts/FreeTTSTime.java \
	    $(STAGING_AREA)/$(API_DIR)			\
	    $(STAGING_AREA)/com/sun/speech/freetts/en/us/cmu_awb/ \
	    $(STAGING_AREA)/com/sun/speech/freetts/en/us/CMUClusterUnitVoice.java \
	    $(STAGING_AREA)/com/sun/speech/freetts/en/us/CMUTimeAWBVoice.java 

##########################################################################

include ${TOP}/build/Makefile.config


# Any extra actions to perform when cleaning
clean::
	rm -rf $(CLASS_DEST_DIR)
	rm -rf $(JARS) $(TARS) $(ZIPS)
	rm -rf $(API_DIR)
	rm -f $(DEPLOY_TARGET)
	rm -rf $(STAGING_AREA)
	rm -rf $(DOC_STAGING_AREA)
	rm -f $(DEPLOY_DOCS_TARGET) 


deploy: all jars zips
	rm -f $(DEPLOY_TARGET)
	rm -rf $(STAGING_AREA)
	(cd docs; $(MAKE) deploy)
	(cd tests; $(MAKE) clean)
	mkdir $(STAGING_AREA)
	cp -r $(DEPLOY_FILES) $(STAGING_AREA)
	rm -f $(DEPLOY_EXCLUDED_FILES)
	rm -rf $(DEPLOY_EXCLUDED_DIRECTORIES)
	-find $(STAGING_AREA) -name CVS -exec rm -rf {} \;
	rm -rf  $(STAGING_AREA)/$(API_DIR)
	$(GTAR) czf freetts.tar.gz $(STAGING_AREA)
	rm -rf $(ZIPS)
	rm -rf $(STAGING_AREA)

emacspeak-server:
	rm -f emacspeak.tar emacspeak.tar.gz
	rm -rf $(STAGING_AREA)
	mkdir $(STAGING_AREA)
	cp -r $(EMACS_SERVER_FILES) $(STAGING_AREA)
	rm -rf $(EMACS_SERVER_EXCLUDES) 
	-find $(STAGING_AREA) -name CVS -exec rm -rf {} \;
	mv $(STAGING_AREA)/emacspeak_index.html $(STAGING_AREA)/index.html
	$(GTAR) cf emacspeak.tar $(STAGING_AREA)
	gzip emacspeak.tar
	# rm -rf $(STAGING_AREA)

emacspeak-server-jar:
	$(MAKE) all
	rm -f emacspeak-server.jar
	(cd classes; $(JAR) cf emacspeak-server.jar *)
	mv classes/emacspeak-server.jar .

deploy_docs:
	rm -f $(DEPLOY_DOCS_TARGET) 
	rm -rf $(DOC_STAGING_AREA)
	$(MAKE) javadocs
	(cd docs; $(MAKE) deploy)
	mkdir $(DOC_STAGING_AREA)
	$(MAKE) webstartclock
	mkdir $(DOC_STAGING_AREA)/WebStartClock
	cp $(WEB_START_CLOCK_FILES) $(DOC_STAGING_AREA)/WebStartClock
	cp -r $(DEPLOY_DOCS) $(DOC_STAGING_AREA)
	rm -f $(DEPLOY_DOCS_EXCLUDED_FILES) 
	-find $(DOC_STAGING_AREA) -name CVS -exec rm -rf {} \;
	$(GTAR) czf $(DEPLOY_DOCS_TARGET) $(DOC_STAGING_AREA)
	rm -rf $(DOC_STAGING_AREA)

push_docs_test:
	sscp $(DEPLOY_DOCS_TARGET)
	sshh tar xzfC $(DEPLOY_DOCS_TARGET) $(PUSH_DEST_DOC_TEST)

push_docs:
	sscp $(DEPLOY_DOCS_TARGET)
	sshh tar xzfC $(DEPLOY_DOCS_TARGET) $(PUSH_DEST_DOC)

all::

# we don't have the dependencies for jar files done up so we just
# remove the old ones and make the new ones

jars:  
	rm -rf $(JARS)
	$(MAKE) $(JARS)

tars:  $(TARS)
zips:  $(ZIPS)

src.zip: 
	$(RM) -f $(CMUAWB_FILES) $(CMUKAL16_FILES) \
		$(CMUKAL8_FILES) $(CMULEX_FILES) $(CMUTIME_FILES)
	find $(SRCDIRS) -name CVS -prune  -o -print | zip $@ -@ 

tests.zip: 
	find $(TESTSDIRS) -name CVS -prune -o -name benchmarks -prune -o -print | zip $@ -@ 

javadocs:
	$(MAKE)  DOC_DEST=$(API_DIR) docs

emacsdocs:

lib/cmulex.jar: 
	(cd classes; $(JAR) cf ../$@ $(CMULEX_FILES))

lib/cmutimelex.jar: 
	(cd classes; $(JAR) cf ../$@ $(CMUTIME_FILES))

lib/cmukal8.jar: 
	(cd classes; $(JAR) cf ../$@ $(CMUKAL8_FILES))

lib/cmukal16.jar:
	(cd classes; $(JAR) cf ../$@ $(CMUKAL16_FILES))

lib/cmuawb.jar:
	(cd classes; $(JAR) cf ../$@ $(CMUAWB_FILES))

lib/jsapi.jar:
	(cd classes; $(JAR) cf ../$@ $(JSAPI_FILES))

lib/demo.jar: 
	( cd classes; $(JAR) cf ../$@ `ls *.class`)

javadoc.zip: javadocs
	$(ZIP) -r $@ $(API_DIR)


lib/freetts.jar:
	(cd classes; \
	$(JAR) cf ../$@ `find com -type f -print | \
		egrep -v -f ../build/freetts_exclude_list`\
                `find de -type f -print | \
		egrep -v -f ../build/freetts_exclude_list`\
	)



########### WebStartClock deploy macros ########################

EN_US_DIR = com/sun/speech/freetts/en/us
AUDIO_DIR = com/sun/speech/freetts/audio
KEY=wsc.key

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
########################################
# Builds and signs the jars
########################################
WEBSTART_CLOCK_FILES  = wsc.jar jsapi.jar wsc.key clock.jnlp
KEY=$(WSC_STAGING_AREA)/wsc.key

webstartclock:
	$(MAKE) all; 
	mkdir -p $(WSC_STAGING_AREA); 
	rm -rf $(WSC_STAGING_AREA)/*;
	cp -r $(WEBSTARTCLOCK_CLASSES) $(WSC_STAGING_AREA); 
	rm -rf $(WEBSTARTCLOCK_EXCLUDES); 
	(cd $(WSC_STAGING_AREA); $(JAR) cf ../wsc.jar  *; )
	rm -rf $(WSC_STAGING_AREA)/*
	mv wsc.jar $(WSC_STAGING_AREA)
	chmod a+x $(WSC_STAGING_AREA)/wsc.jar; 
	cp demo/JSAPI/WebStartClock/clock.jnlp $(WSC_STAGING_AREA)
	$(MAKE) key
	$(MAKE) signjars


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
	cp $(TOP)/lib/jsapi.jar $(WSC_STAGING_AREA)
	$(JAVA_HOME)/bin/jarsigner -storepass freetts -keypass freetts -keystore $(KEY) $(WSC_STAGING_AREA)/wsc.jar freetts 
	$(JAVA_HOME)/bin/jarsigner -storepass freetts -keypass freetts -keystore $(KEY) $(WSC_STAGING_AREA)/jsapi.jar freetts 


