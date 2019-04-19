JCC = javac
default: Notenspiegel.class DrawScreen.class Commands.class Hsmw.class Extract.class Score.class

Notenspiegel.class:
	$(JCC) Notenspiegel.java

DrawScreen.class:
	$(JCC) DrawScreen.java

Commands.class:
	$(JCC) Commands.java

Hsmw.class:
	$(JCC) Hsmw.java

Extrac.class:
	$(JCC) Extract.class

Score.class:
	$(JCC) Score.class

clean:
	$(RM) *.class

run:
	java Notenspiegel
