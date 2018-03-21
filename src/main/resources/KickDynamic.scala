import java.lang.reflect.InvocationTargetException

import scala.language.dynamics


class DynamicWrapper(obj: Object) extends Dynamic {

  def value = obj

  def applyDynamic(name: String)(args: Any*) : DynamicWrapper = {

    val candidates = obj.getClass.getMethods.filter(method => method.getName.equals(name))

    candidates.foreach(method => {
      try {
        if(args.size != 0){
          return new DynamicWrapper(method.invoke(obj, args.map(_.asInstanceOf[AnyRef]): _*))
        }else{
          return new DynamicWrapper(method.invoke(obj))
        }
      } catch {
        case e: IllegalArgumentException => {}
        case e: InvocationTargetException => throw e.getCause
        case e: Exception => throw e
      }
    })

    throw new NoSuchMethodException("Object $obj does not have method $name which takes: "
      + args.map(_.toString).mkString(" "))
  }

  def selectDynamic(name: String) : DynamicWrapper = applyDynamic(name)()

  override def toString: String = obj.toString
}

implicit def toDouble (dyn : DynamicWrapper) : Double = {
    dyn.value.asInstanceOf[Any] match {
      case d : Double => d.toDouble
      case f : Float => f.toDouble
      case l : Long => l.toDouble
      case i: Int  => i.toDouble
      case s : Short => s.toDouble
      case b : Byte => b.toDouble
    }
}