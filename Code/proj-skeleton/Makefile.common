
# Where the LLVM and clang binaries are located. Change this variable if not in PATH
PREFIX=

# Enumerate source files and create a list of object files
C_SRC=$(wildcard *.c)
CXX_SRC=$(wildcard *.cpp)
C_OBJ=$(C_SRC:.c=.bc)
CXX_OBJ=$(CXX_SRC:.cpp=.bc)

# Flags for CC and CXX. 
CFLAGS=-g
CXXFLAGS="$(CFLAGS)"

all: $(TARGET) outputs

include ../pipair.mak

# LLVM binaries
CC=$(PREFIX)clang
CXX=$(PREFIX)clang++
LINK=$(PREFIX)llvm-link
OPT=$(PREFIX)opt

$(TARGET): $(TARGET).bc
	$(CXX) $(CXXFLAGS) $< -o $@

$(TARGET).bc: $(C_OBJ) $(CXX_OBJ)
	$(LINK) -v $(C_OBJ) $(CXX_OBJ) -o $@

%.bc: %.c
	$(CC) $(CFLAGS) -emit-llvm -c $< -o $@

%.bc: %.cpp
	$(CXX) $(CFLAGS) -emit-llvm -c $< -o $@

clean:
	rm -rf *.bc
	rm -rf $(TARGET)
	rm -rf $(TARGET)_*_*.out


