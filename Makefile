# Copyright 2001 Sun Microsystems, Inc.
# All Rights Reserved.  Use is subject to license terms.
# 
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL 
# WARRANTIES.
#

# Relative path to the "base" of the source tree
TOP = .

TAR = tar
ZIP = zip

# List any sub directories that need to be built
SUBDIRS = javax com demo bin tests docs

# List src dirs that get deployed
# SRCDIRS = javax com
SRCDIRS = com
TESTSDIRS = wave data bin tests


#JARS = lib/freetts.jar lib/cmulex.jar lib/cmukal8.jar lib/cmukal16.jar \
#	lib/cmuawb.jar lib/jsapi.jar 

JARS = lib/freetts.jar lib/cmulex.jar lib/cmukal8.jar lib/cmukal16.jar \
	lib/cmuawb.jar lib/cmutimelex.jar lib/demo.jar

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


DEPLOY_DOCS_TARGET = freettsdocs.tar
DOC_STAGING_AREA = ./htdocs
DEPLOY_DOCS = demo docs license.terms acknowledgments.txt index.html
DEPLOY_DOCS_EXCLUDED_FILES = $(DOC_STAGING_AREA)/XXX

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
	rm -f $(DEPLOY_DOCS_TARGET) $(DEPLOY_DOCS_TARGET).gz


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
	tar cf freetts.tar $(STAGING_AREA)
	gzip freetts.tar
	rm -rf $(ZIPS)
	rm -rf $(STAGING_AREA)

deploy_docs:
	rm -f $(DEPLOY_DOCS_TARGET) $(DEPLOY_DOCS_TARGET).gz
	rm -rf $(DOC_STAGING_AREA)
	$(MAKE) javadocs
	(cd docs; $(MAKE) deploy)
	mkdir $(DOC_STAGING_AREA)
	cp -r $(DEPLOY_DOCS) $(DOC_STAGING_AREA)
	rm -f $(DEPLOY_DOCS_EXCLUDED_FILES) 
	-find $(DOC_STAGING_AREA) -name CVS -exec rm -rf {} \;
	tar cvf $(DEPLOY_DOCS_TARGET) $(DOC_STAGING_AREA)
	gzip $(DEPLOY_DOCS_TARGET)
	rm -rf $(DOC_STAGING_AREA)

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
	)
