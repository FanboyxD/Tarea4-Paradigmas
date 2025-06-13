CC = gcc
CFLAGS = -Wall -Wextra -std=c99
LIBS = -lws2_32 -luser32 -lgdi32
TARGET = game_server.exe
SOURCES = main.c game_logic.c network.c ui.c
OBJECTS = $(SOURCES:.c=.o)

$(TARGET): $(OBJECTS)
	$(CC) $(OBJECTS) -o $(TARGET) $(LIBS)

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	del /f *.o $(TARGET)

.PHONY: cleans