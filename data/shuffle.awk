BEGIN { NWORDS=2 }

{ words[count] = $1; count++ }
END { 
    for (i = 0 ; i < 50000; i++) {
	for (j = 0; j < NWORDS; j++) {
	    idx = int((rand() * count));
	    printf("%s ",  words[idx]);
	}
	printf("\n");
    }
}


