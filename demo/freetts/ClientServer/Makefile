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
SUBDIRS = ../../util

# Use a separate classes directory
CLASS_DEST_DIR = ${TOP}/demo/classes


##########################################################################

include ${TOP}/build/Makefile.config

CLASSES = ${FILES:%.java=${CLASS_DEST_DIR}/%.class}

cclient: client.c
	rm -f cclient
	gcc -Wall -lxnet -o cclient client.c

all:: ${CLASSES} ${STUBS}

runserver: all
	${TOP}/bin/baseFreeTTS \
	-Dmetrics=true Server

runMbrola1Server: all
	${TOP}/bin/baseFreeTTS \
	-Ddebug=false \
	-Dmbrola.base=$(MBROLA_DIR) \
	-Dmetrics=true \
	-Dvoice16kName="mbrola1" \
	Server

runMbrola2Server: all
	${TOP}/bin/baseFreeTTS \
	-Ddebug=false \
	-Dmbrola.base=$(MBROLA_DIR) \
	-Dmetrics=true \
	-Dvoice16kName="mbrola2" \
	Server

runMbrola3Server: all
	${TOP}/bin/baseFreeTTS \
	-Ddebug=false \
	-Dmbrola.base=$(MBROLA_DIR) \
	-Dmetrics=true \
	-Dvoice16kName="mbrola3" \
	Server

# the command line arguments: <server host> <port> <sample rate> <show metrics>
runcclient: cclient
	./cclient localhost 5555 16000 0

runjclient: all
	${TOP}/bin/baseFreeTTS \
	-Ddebug=false \
	-Dmetrics=false \
	-Dserver=localhost \
	-DsampleRate=16000 Client
