import java.util.Date

var javaMap = new java.util.HashMap[String, Object]();
javaMap.put("user", "Gleb")

variables.put("name","proactive")
variables.put("now",new Date())
variables.put("javaMap",javaMap)

println("'gleb'.length : " + variables.get("javaMap").get("user").length)

val len = variables.get("javaMap").get("user").length
println(5 + len)
println("6.5" + len)
println("'gleb'.substring(1): " + variables.get("javaMap").get("user").substring(1))