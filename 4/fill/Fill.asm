// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/4/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, 
// the screen should be cleared.

@SCREEN
    D=A
    @R0
    M=D
 
(LOOP)
    @KBD
    D=M
    @BLACK
    D;JGT
    D=0
    @PAINT
    0;JMP
(BLACK)
    D=-1
 
(PAINT)
    @R0
    A=M
    M=D
 
    @R0
    M=M+1
    D=M

    @24576
    D=D-A

    @RESETPTR
    D;JEQ

    @LOOP
    0;JMP
 
(RESETPTR)
    @SCREEN
    D=A

    @R0
    M=D
    
    @LOOP
    0;JMP
 
(END)
    @END
    0;JMP
 