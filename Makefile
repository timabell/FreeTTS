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

# en/us voices are immediate subdirectories of en/us
EN_US_DIR = com/sun/speech/freetts/en/us
EN_US_VOICES:=$(shell (cd ${EN_US_DIR}; ls -l ) | egrep "^d" | awk '{ print $$9 }' | grep -v CVS )
EN_US_VOICES_JAR_FILES:=$(shell for i in ${EN_US_VOICES}; do echo $$i.jar; done)

JAR_FILES = freetts.jar cmulex.jar \
	cmutimelex.jar demo.jar en_us.jar $(EN_US_VOICES_JAR_FILES)

JARS = freetts.jar cmulex.jar \
	cmutimelex.jar demo.jar en_us.jar $(EN_US_VOICES)

WEBSTART_CLOCK_DIR = demo/JSAPI/WebStartClock

WEB_START_CLOCK_FILES = $(WEBSTART_CLOCK_DIR)/wsc.jar \
			$(WEBSTART_CLOCK_DIR)/jsapi.jar\
			$(WEBSTART_CLOCK_DIR)/clockKey \
			$(WEBSTART_CLOCK_DIR)/clock.jnlp

ZIPS = javadoc.zip src.zip tests.zip


API_DIR = docs/api

CMULEX_FILES =  com/sun/speech/freetts/en/us/cmulex_compiled.bin \
		com/sun/speech/freetts/en/us/cmulex_addenda.bin  \
		com/sun/speech/freetts/en/us/cmulex_lts.bin  

CMUTIME_FILES= com/sun/speech/freetts/en/us/cmutimelex_compiled.bin \
		com/sun/speech/freetts/en/us/cmutimelex_addenda.bin  \
		com/sun/speech/freetts/en/us/cmutimelex_lts.bin  \
   
#CMUKAL_FILES = com/sun/speech/freetts/en/us/cmu_kal/diphone_units.bin \
#		com/sun/speech/freetts/en/us/cmu_kal/diphone_units.idx \
#		com/sun/speech/freetts/en/us/cmu_kal/diphone_units16.bin \
#		com/sun/speech/freetts/en/us/cmu_kal/diphone_units16.idx \
#		com/sun/speech/freetts/en/us/cmu_kal/KevinVoiceDirectory.class

#CMUAWB_FILES = com/sun/speech/freetts/en/us/cmu_awb/cmu_time_awb.bin 


#JSAPI_FILES = javax

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

TARS =  $(DEPLOY_DOCS_TARGET) $(DEPLOY_TARGET)

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


####################################################
# Returns the tree to a pristene state
####################################################
clean::
	rm -rf $(CLASS_DEST_DIR)
	(cd lib; rm -f $(JAR_FILES))
	rm -rf $(TARS) $(ZIPS)
	rm -rf $(API_DIR)
	rm -f $(DEPLOY_TARGET)
	rm -rf $(STAGING_AREA)
	rm -rf $(DOC_STAGING_AREA)
	rm -f $(DEPLOY_DOCS_TARGET) 
	rm -rf $(WSC_STAGING_AREA)


####################################################
# Creates the freetts tarball ready for deploying
####################################################
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
	-find $(STAGING_AREA) -name jsapi.jar -exec rm -rf {} \;
	rm -rf  $(STAGING_AREA)/$(API_DIR)
	$(GTAR) czf freetts.tar.gz $(STAGING_AREA)
	rm -rf $(ZIPS)
	rm -rf $(STAGING_AREA)

####################################################
# Creates the emacspeak-server  tarball ready for deploying
####################################################
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

####################################################
# Creates the documentation tarball ready for deploying
####################################################
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
	-rm -rf $(DOC_STAGING_AREA)

####################################################
# Deploys the docs to the sourceforge test area
####################################################
push_docs_test:
	sscp $(DEPLOY_DOCS_TARGET)
	sshh tar xzfC $(DEPLOY_DOCS_TARGET) $(PUSH_DEST_DOC_TEST)

####################################################
# Deploys the docs to the sourceforge release area
####################################################
push_docs:
	sscp $(DEPLOY_DOCS_TARGET)
	sshh tar xzfC $(DEPLOY_DOCS_TARGET) $(PUSH_DEST_DOC)

all::

# we don't have the dependencies for jar files done up so we just
# remove the old ones and make the new ones

jars:  
	echo $(JARS)
	(cd lib; rm -f $(JAR_FILES))
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

cmulex.jar: 
	(cd classes; $(JAR) cfm ../lib/$@ $(EN_US_DIR)/cmulex.Manifest \
		$(CMULEX_FILES))

cmutimelex.jar: 
	(cd classes; $(JAR) cfm ../lib/$@ $(EN_US_DIR)/cmutimelex.Manifest \
		$(CMUTIME_FILES))

#lib/cmukal.jar: 
#	(cd classes; $(JAR) cf ../$@ $(CMUKAL_FILES))

#lib/cmuawb.jar:
#	(cd classes; $(JAR) cf ../$@ $(CMUAWB_FILES))

#jsapi.jar:
#	(cd classes; $(JAR) cf ../lib/$@ $(JSAPI_FILES))

demo.jar: 
	( cd classes; $(JAR) cf ../lib/$@ `ls *.class`)

${EN_US_VOICES}:
	(cd classes; \
	$(JAR) cfm ../lib/$@.jar $(EN_US_DIR)/$@/voice.Manifest \
		`ls ${EN_US_DIR}/$@/*.class` \
		`ls ${EN_US_DIR}/$@/*.idx` \
		`ls ${EN_US_DIR}/$@/*.bin` \
	)
	
en_us.jar:
	(cd classes; \
	$(JAR) cfm ../lib/$@ $(EN_US_DIR)/en_us.Manifest \
		`ls com/sun/speech/freetts/en/*.class \
		com/sun/speech/freetts/en/us/*.class \
		com/sun/speech/freetts/en/us/*.txt \
		| egrep -v '.*\cmulex.*\.txt' \
		| egrep -v '.*\cmu.*\.txt'` \
	)

freetts.jar:
	(cd classes; \
	$(JAR) cfm ../lib/$@ $(EN_US_DIR)/en_us.Manifest \
		`find com -type f -print | \
		egrep -v -f ../build/freetts_exclude_list`\
                `find de -type f -print | \
		egrep -v -f ../build/freetts_exclude_list` \
	)

javadoc.zip: javadocs
	$(ZIP) -r $@ $(API_DIR)

#########################################
# Builds the webstartclock product files, leaves them
# in the WEBSTART_CLOCK_DIR directory
#########################################

webstartclock:
	(cd $(WEBSTART_CLOCK_DIR); $(MAKE) jars)


