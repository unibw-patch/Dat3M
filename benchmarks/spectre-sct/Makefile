CC=gcc
CFLAGS=-m32 -masm=intel -fomit-frame-pointer  -fno-stack-protector -static -no-pie -fno-pic

# Compilation

all: spectre-sct spectre-sct.s

.DEFAULT_GOAL := all

spectre-sct : spectre-sct.c
	$(CC) $(CFLAGS) $< -o $@

spectre-sct.s : spectre-sct.c
	$(CC) -S $(CFLAGS) $< -o $@

clean :
	rm spectre-sct spectre-sct.s
