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

Considering a simple 2-tasks workflow with task_A -> task_B

    // In task_A
    variables.put("AA", "aa")
    variables.put("BB", Array(1, 2, 3))
    variables.put("CC", Map("name" -> "Gromit", "likes" -> "cheese", "id" -> "1234"))
    val result = Map(0 -> "abc", 1 -> "def")

    // In task_B
    println(variables.get("AA")) // aa
    val arr = variables.get("BB").valueCast[Array[Int]]
    println(arr(0)) // 1
    println(variables.get("CC").values) // MapLike.DefaultValuesIterable(Gromit, cheese, 1234)
    println(variables.get("CC").get("name")) // Some(Gromit)
    println(results(0).getValue.get(0)) // Some(abc)