# Helldivers 2 Automaton Language maker

**Notice**: I do not own the copyright to Helldivers 2. All rights go to Arrowhead and Sony.

## Compilation

```shell
./gradlew jar
```

This builds a jar in `build/libs/HD2BotLang.jar`, which can be run using `java -jar HD2BotLang.jar
` (or `java -jar build/libs/HD2BotLang.jar`)

## Usage

Every argument is directly translated into a line.

That means

```shell
java -jar HD2BotLang.jar Some Text
```

will have two lines of text. it is equivalent to

```
Some
Text
```

To get a single line, use quotes

```shell
java -jar HD2BotLang.jar "Some Text" "Another line"
```

will get you

```
Some Text
Another line
```

The output file, which is always `out.png`, will be created in the same directory as the terminal. There is **NO** check to see if the file already exists. It will be overwritten.
