JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	TrafficAnalysis.java \

default: classes

classes: $(CLASSES:.java=.class)

run: 
	java TrafficAnalysis

clean:
	$(RM) *.class
