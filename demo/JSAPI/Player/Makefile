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

##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/%.class}

all:: ${CLASSES} ${STUBS}

run: 
	$(JAVA_CMD) Player

runbig: 
	$(JAVA_CMD) Player -fontsize 20

runmonitor: 
	$(JAVA_CMD) Player -monitor


# you can define multiple instance of plaintext, textfile, jsmltext,
# or jsmlfile

# INPUT = \
#	"plaintext:Welcome to the jay sappy Player program."\
#	"textfile:$(PWD)/Player.java"\
#	"jsmltext:<jsml>Hello JSML</jsml>"\
#	"jsmlfile:$(PWD)/test1.jsml"\

input: 
	$(JAVA_CMD) Player -monitor -input $(INPUT)

PLAINTEXTS = \
	"plaintext:Welcome to the jay sappy demo synthesis program. I can help you test this program. My name is kevin. First, I'll count for you"\
	"plaintext:one"\
	"plaintext:two"\
	"plaintext:three"\
	"plaintext:four"\
	"plaintext:five"\
	"plaintext:six"\
	"plaintext:seven"\
	"plaintext:eight"\
	"plaintext:nine"\
	"plaintext:10"\
	"plaintext:1 2 3 4 5 6 7 8 9 10"\
	"plaintext:how now brown cow"\
	"plaintext:For score and seven years ago,"\
	"plaintext:our fathers brought forth on this continent"\
	"plaintext:a new nation, conceived in liberty"\
	"plaintext:and dedicated to the proposition"\
	"plaintext:that all men are created equal."

plaintext: 
	$(JAVA_CMD) Player -monitor -input $(PLAINTEXTS)
