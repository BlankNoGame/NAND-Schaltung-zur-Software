Projekte für den Kurs von Schaltung zur Software.

Helferchips in Projekt1: Not4, And4
Helferchips in Projekt2: Or16Way
Helferchips in Projekt3: DFF4, DFF16

Projekt 6: Assembler kann per java Assembler ./path/File.asm unter Windows genutzt werden. Unter Linux sollte /path/File.asm reichen
    als Beispiel => java Assembler ./pong/Pong.asm
    (Ich war mir nicht sicher mit der Ordnerstruktur und hoffe das ist so in Ordnung)

Projekt 7:
      	Hilfklasse: Ich habe eine Enum Klasse als Hilfsklasse benutzt um zwischen den Command Typen zu unterscheiden, dadurch ist die Erweiterbarkeit der Commands später einfach sowie sind möglichkeit für Fehlerhafte Eingaben geringer.

   	Hilfsmethoden: Ich habe 2 weitere Methoden im CodeWriter hinzugefügt: 
       		1. writeVergleiche(String segment, int index): diese Methode streamlined die Code generierung für vergleiche da diese sich vom Grundaufbau nicht unterscheiden.
       		2. translateSegToSymbol(String segment, int index): hier zentriere ich die übersetzung der Segmente in Assembly Symbole, damit die write Methoden nicht all zu voll gecluttered sind

	Sonst gilt hier auch im Ordner 7:
        	compilieren mit javac VMTranslator.java             (Die Klassen sind zwar schon einmal compiliert worden, aber falls neu compiliert werden soll einmal hier beschrieben wie)
        	ausführen mit java VMTranslator ./path/File.vm      (Als Beispiel java VMTranslator ./MemoryAccess/PointerTest/PointerTest.vm) (eine alte .asm Datei liegt bereits vor)  


Projekt 8:

Erweiterung der Enum Klasse für die neuen Operationen. 

Hilfscomparator: Einführung eines eigenen Comparators welcher gewährleistet dass die Main.vm zuerst in der Liste liegt, und somit zuerst übersetzt wird.
Hilfsmethoden (nur in CodeWriter Klasse): Einführen weiterer Methoden:
	writeInit(): schreibt den Bootstrapcode an den Anfang wenn wir ein Verzeichnis vorliegen haben
	setFileName(String fileName): setzen des momentanen File Name um das static segment correct zu übersetzen
	scopedLabel(String label): prüft ob zuvor eine Funktion deklariert wurden ist, und ob das label somit in einem Funktion Scope liegen sollte. Ermöglicht damit auch das labeling außerhalb von Funktionen
Hilfsklassenvars: 
	returnCounter für die korrekte benennung der Returnslabels für die Funktionen
	currentFunction: für das korrekte labeling von labels in der Funktion
	currentFileName: für das korrekte benennen der static segmente

Es gelten die selben Befehle wir in Projekt 7 d.h.
	compilieren mit javac VMTranslator.java             (Die Klassen sind zwar schon einmal compiliert worden, aber falls neu compiliert werden soll einmal hier beschrieben wie)
    ausführen mit java VMTranslator ./path/File.vm      (Als Beispiel java VMTranslator ./ProgramFlow/BasicLoop/BasicLoop.vm)
	Erweiterung der Ausführung das auch ganze Verzeichnise übergeben werden können, egal ob als Absoluter- oder Relativer Pfad
	!!Bekanntest Problem ist hierbei wenn Ordner Namen eine leerstelle besitzt, dadurch wird der Pfad an der leerstelle getrennt und als 2 Argumente an das Programm übergeben. Das hat zur folge dass die Quelle nicht gefunden werden kann, bitte vermeidet leerstellen in Ordner und Datei Namen!!

Projekt 9: Das Hangman game

Ich habe versucht ein Hangman game zu gestallten. Es funktioniert wie Typisches Hangman, Controlls sind einfach Buchstaben per Tastendruck eingeben und mit Enter bestätigen. per eingabe von RES kann das spiel mittendrin neugestartet werden.
Bei Win/Loss kann mit Y und Enter ein neues Spiel gestartet werden. Die Pixel hängen etwas in einander aber naja ist halt jetzt so, da meine Zeit etwas knapp wird bleibt das auch so.

Projekt 10:

Hilfsklassen: Ich habe 2 enums für die Keywords und tokentyp Konstanten erstellt, sowie einen eigenen Record für Token, damit die Art des Tokens und sein Inhalt zusammen an einer Stelle steht. Dadurch erstellt der Tokenizer für jeden expliziten TokenTyp ein eigenes Token Objekt mit Inhalt.
Dadurch werden folgende Methode nicht mehr genutzt: identifier, intVal, stringVal, da diese einfach durch die Hilfsmethode tokenValue() ersetzt werden können. Dies ist der Fall da diese Methoden keine Logik enthalten, da Sie nur den zugeordneten Wert des Token Objekts lesen und wiedergeben. Die Logik für diese wiedergabe steht dadurch Basically in der tokenize Methode.

Hilfsmethoden: Ich habe 8 Hilfsmethoden eingebracht, darunter fallen die Methoden: tokenize (JackTokenizer), removeComments (JackTokenizer), writeEinrueckung (CompilationEngine), openXMLTagNonTerminal (CompilationEngine), closeXMLTagNonTerminal (CompilationEngine), writeCurrentToken (CompilationEngine), exchangeSpecificSymbols (CompilationEngine), compileSubroutineCalls (CompilationEngine)

removeComments: 
Nimmt den Inhalt der Jack Datei, welcher in einen String umgewandet wurden ist, und entfernt alle Arten der Jack Kommentare
tokenize: 
Nimmt den Inhalt ohne Kommentare als String an und iteriert über diesen drüber. Es wird ein char des String genommen und bestimmte gegebenheiten geprüft (ob eine leerzeile, ", oder ein Symbol etc.). Je nach Gegebenheit wird dann ein neues Token Datenobjekt erstellt mit den richtigen Tokentypen und seinem Inhalt. Das Token wird intern in eine Liste gespeichert. advance iteriert nur noch über diese Liste drüber

writeEinrueckung, writeCurrentToken, openXMLTagNonTerminal, closeXMLTagNonTerminal sind nur Methoden welche das immer wieder schreiben von der korrekten einrückung, dem Nonterminal tag und dem Terminal/Token übernehmen. Somit werden die compile Methoden lesbarer.

exchangeSpecificSymbols:
ersetzt die in der Lecture erwähnten Symbole, da diese in XML auch definiert sind

compileSubroutineCalls:
übernimmt das compelieren von Methoden aufrufen im Code, basically habe ich nur die compileSubroutine Methode aufgeteilt in eine Methode welche die deklaration von subroutinen übernimmt und eine welche aufrufe von Subroutinen übernimmt.

=> dazu habe ich entsprechende Variablen / Klassenvariablen hinzugefügt

Programm funktioniert wie in der Lecture beschrieben, mit javac JackAnalyzer.java werden alle Klassen compiliert und dann mit java JackAnalyzer source werden alle jack files in der source gelesen und in eine eigene xml datei umgeformt in der form von JackFileNameT.xml. 
Es gilt weiter !!Bekanntest Problem ist hierbei wenn Ordner Namen eine leerstelle besitzt, dadurch wird der Pfad an der leerstelle getrennt und als 2 Argumente an das Programm übergeben. Das hat zur folge dass die Quelle nicht gefunden werden kann, bitte vermeidet leerstellen in Ordner und Datei Namen!!

Projekt 11:

Hilfsklassen: Ich habe 2 enums für die Kind und Segment Konstanten erstellt, sowie einen eigenen Record für Symbol, damit es einen Datenkontainer gibt welche für die SymbolTable Kind, Type und Index kombiniert.

Hilfsmethoden: Aus Projekt 10 habe ich alle Hilfsmethoden entfernt welche für das schreiben der xml file zuständig waren. Dafür habe ich Hilfsmethoden hinzugefügt welche helfen VM code zu schreiben. Vorallem die Expect Methoden sind dafür da nur den tokenizer zu advance wenn wir den richtige Token haben, damit die Grammatik besser eigehalten werden kann und das debugging einfacher wird.

consume: Extramethode welche den hasMoreTokens check macht und gegebenenfalls den Tokenizer advanced. Vereinfacht die Abfrage im weiteren Code

expectKeyword: checks ob der momentanen Token vom Typ Keyword ist und ob das übergebene keyWort zu dem im Token passt. Falls es dies tut wird das Token konsumiert. Die Methoden expectSymbol, expectIdentifier, expectType führen diese Abfrage für deren eigene Fälle aus. Diese Methoden sind vorallem dafür da um den tokenizer zu advance wenn wir den richtige Token haben, damit die Grammatik besser eigehalten werden kann und das debugging einfacher wird.

writeOperator: Hier habe ich das schreiben für die Operatoren extrahiert und in eine Methode gefassst.

pushVaribale: Methode welche für die gegebene Variable den vollständigen Push schreibt

popVariable: Methode welche für die gegebene Variable den vollständigen Pop schreibt

kindToSegment: Übersetzt die Werte des ENUMS Kind in das gleichnamig Segment um, damit wir writePush und writePop besser nutzen können.

Ansonsten Funktioniert das Programm genauso mit den selben befehlen wie das Projekt 10.

Projekt 12: 

Compilieren und ausführen wie im Projekt beschrieben, alle Dateien liegen in ihrem eigenen Ordner vor. Ich hoffe alles worked.