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