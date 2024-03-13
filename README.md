#Project 7 and 8: Virtual Machine (VM) Translator and Compiler

Overview
Projects 7 and 8 in the Nand to Tetris course involve building a Virtual Machine (VM) translator (Project 7) and a high-level language compiler (Project 8). These projects are pivotal in understanding the transition from a simplified assembly-like language to a higher-level programming language that can be executed on the Hack computer platform.

Key Concepts:
Virtual Machine (VM) Language:
The VM language includes commands for stack manipulation, arithmetic operations, memory access, program flow control, and function calls.
High-Level Language:
Project 8 introduces a high-level language called Jack, which is a simple, object-oriented language resembling Java.
VM Translator:
Project 7 focuses on translating programs written in the VM language into equivalent programs in the Hack assembly language.
Compiler:
Project 8 involves building a compiler that translates programs written in the Jack language into equivalent VM code, which can then be further translated to Hack assembly code.

Project Structure:
VMTranslator: (VMTranslator.java, Parser.java, CodeWriter.java)
Translates VM language commands into Hack assembly code.
Handles stack arithmetic, memory access, program flow, and function call commands.
Compiler: (JackCompiler.java, JackTokenizer.java, CompilationEngine.java, SymbolTable.java, VMWriter.java)
Translates Jack language programs into VM code.
Implements lexical analysis, parsing, semantic analysis, and code generation stages of compilation.
VM Files:
Contains sample VM files (.vm) with VM language commands for translation (Project 7).
Jack Files:
Contains sample Jack language files (.jack) for compilation (Project 8).
