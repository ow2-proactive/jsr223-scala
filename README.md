# jsr223-scala

JSR223 script engine for Scala.

## Build

    $> ./gradlew clean build

to generate jsr223-scala\build\libs\jsr223-scala-xx.jar

## Usage
This Scala script engine wrapper is discoverable under "scalaw" name
(http://docs.oracle.com/javase/7/docs/technotes/guides/scripting/programmer_guide/index.html).

## Bindings
User has access from the scala script to predefined variables, according to the current script context. Here are some of them

    val result = 123
    val loop = 2
    val runs = 4
    val selected = false
    variables.put("AA","aa")
    println(variables.get("AA"))
