package other

/**
 * 方便熟悉scopt框架的类
 */
object ScoptTest {

  case class Params(
                     input: String = "input",
                     output: String = "output",
                     index: Int = 0,
                     partitionnum: Int = 2,
                     debug: Boolean = false)


  val parser = new scopt.OptionParser[Params]("test") {
    head("this is a test for scopt params")


    opt[String]('i', "input").required().action {
      (x, c) =>c.copy(input = x)
    }.text("default of input is input")

    opt[String]('o', "output").required().action {
      (x, c) => c.copy(output = x)
    }.text("default of output is output")

    opt[Int]("index").optional().action {
      (x, c) => c.copy(index = x)
    }.text("default of index is 0")

    opt[Int]("partitionnum").optional().action {
      (x, c) => c.copy(partitionnum = x)
    }.text("default of partitionnum is 2")

    opt[Boolean]("debug").optional().action {
      (x, c) => c.copy(debug = x)
    }.text("default of debug is false")
  }

  def init(args: Array[String]) = {
    parser.parse(args, Params()) match {
      case Some(params) =>
        println(params.index);println(params.input);
        println(params.output);println(params.partitionnum)
      case _ => sys.exit()
    }

  }

  def main(args: Array[String]): Unit = {
    init(args)
  }

}
