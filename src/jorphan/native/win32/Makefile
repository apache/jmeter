SRC=org_apache_jorphan_timer_HRTimer.c
OBJ=$(SRC:.c=.o)
DLL=hrtlib.dll

CC=C:/mingw/bin/gcc
CFLAGS=-Wall -D_JNI_IMPLEMENTATION_ -IC:/Progra~1/IBM/Java131/include
LDFLAGS=-shared -Wl,--kill-at

%.o: %.c
	$(CC) $(CFLAGS) -o $@ -c $<

all: $(DLL)

$(DLL): $(OBJ)
	$(CC) $(OBJ) $(LDFLAGS) -o $(DLL)

#C:/mingw/bin/gcc -Wall -D_JNI_IMPLEMENTATION_ -Wl,--kill-at -IC:/Progra~1/IBM/Java131/include -shared org_apache_jorphan_timer_HRTimer.c -o hrtlib.dll
