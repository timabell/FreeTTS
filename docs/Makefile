# Copyright 2001 Sun Microsystems, Inc.
# All Rights Reserved.  Use is subject to license terms.
# 
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL 
# WARRANTIES.
#
# build the php from the main page

TOP = ..

include ${TOP}/build/Makefile.config


deploy:	index.php

index.php:    index.html
	gawk -f create_php.awk < $< > $@

clean::
	rm -f index.php
