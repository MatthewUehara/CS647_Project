JFLAGS = -J-Xmx64m -sourcepath Part1/src Part1/src/Main.java -d Part1/bin
JC = javac
.SUFFIXES: .java .class
java.class:
	$(JC) $(JFLAGS)

default: classes

classes: $(.java=.class)

.PHONY: clean

clean:
	rm -rf Part1/bin/*.class
