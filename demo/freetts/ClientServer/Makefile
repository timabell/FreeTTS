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
SUBDIRS = ../../util

##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/%.class}

cclient: client.c
	rm -f cclient
	gcc -Wall -lxnet -o cclient client.c

all:: ${CLASSES} ${STUBS}

runserver: all
	$(JAVA_CMD) -Dmetrics=false Server

# the command line arguments: <server host> <port> <sample rate> <show metrics>
runcclient: cclient
	./cclient sunlabs.east 5555 16000 0

runjclient: all
	$(JAVA_CMD) \
	-Ddebug=false \
	-Dmetrics=false \
	-Dserver=localhost \
	-DsampleRate=16000 Client
