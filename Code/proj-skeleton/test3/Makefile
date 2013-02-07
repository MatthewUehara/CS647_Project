TARGET=test3

all: $(TARGET).bc outputs

include ../pipair.mak

$(TARGET).bc: httpd.bc.orig
	cp $< $@

clean:
	rm -rf $(TARGET).bc
	rm -rf $(TARGET)_*_*.out


