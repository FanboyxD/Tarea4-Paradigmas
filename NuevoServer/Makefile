# Makefile para el servidor del juego de plataformas
CC = gcc
CFLAGS = -Wall -Wextra -std=c99
LIBS = -lws2_32 -luser32 -lgdi32 -lkernel32
TARGET = server.exe
SOURCES = server.c
HEADERS = constants.h

# Regla principal
$(TARGET): $(SOURCES) $(HEADERS)
	$(CC) $(CFLAGS) -o $(TARGET) $(SOURCES) $(LIBS)

# Regla para limpiar archivos compilados
clean:
	del /f $(TARGET) *.o

# Regla para ejecutar el servidor
run: $(TARGET)
	./$(TARGET)

# Regla para compilar en modo debug
debug: CFLAGS += -g -DDEBUG
debug: $(TARGET)

# Regla para compilar en modo release
release: CFLAGS += -O2 -DNDEBUG
release: $(TARGET)

.PHONY: clean run debug release