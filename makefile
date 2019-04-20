JCC=javac

default: Notenspiegel.class Visual.class Hsmw.class Extract.class Score.class

Notenspiegel.class:
	$(JCC) Notenspiegel.java

Visual.class:
	$(JCC) Visual.java

Hsmw.class:
	$(JCC) Hsmw.java

Extract.class:
	$(JCC) Extract.class

Score.class:
	$(JCC) Score.class

clean:
	$(RM) *.class

run:
	java Notenspiegel
