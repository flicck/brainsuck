package brainsuck

import java.io.File

import brainsuck.RulesExecutor.{Batch, FixedPoint, Once}
import scopt.OptionParser

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class Memory(private val buffer: ArrayBuffer[Int] = ArrayBuffer.fill[Int](1024)(0)) {
  def same(that: Memory) =
    (this eq that) || {
      val commonLength = buffer.length.min(that.buffer.length)
      buffer.take(commonLength) == that.buffer.take(commonLength) &&
      buffer.drop(commonLength).forall(_ == 0) &&
      that.buffer.drop(commonLength).forall(_ == 0)
    }

  private def ensureSpace(atLeast: Int): Unit =
    if (atLeast >= buffer.size) {
      val bytesNeeded = atLeast - buffer.size + 1
      buffer ++= ArrayBuffer.fill[Int](bytesNeeded)(0)
    }

  def apply(pointer: Int): Int = {
    ensureSpace(pointer)
    buffer(pointer)
  }

  def update(pointer: Int, value: Int): Unit = {
    ensureSpace(pointer)
    buffer(pointer) = value
  }

  override def toString = buffer.mkString("|")
}

class Machine(var pointer: Int, val memory: Memory) {
  def same(that: Machine) =
    (this eq that) || {
      pointer == that.pointer && memory.same(that.memory)
    }

  def value = memory(pointer)

  def value_=(n: Int): Unit = memory(pointer) = n

  override def toString = s"$pointer @ $memory"
}

trait Optimizer {
  def batches: Seq[Batch[Instruction]]

  def apply(code: Instruction) = RulesExecutor(code, batches)
}

object Interpreter {
  case class Config(optimizationLevel: Int = 2, input: File = null)

  private def benchmark[T](desc: String)(f: => T) = {
    val start = System.nanoTime()
    val result = f
    println(s"$desc: ${(System.nanoTime() - start).toDouble / 1000000}")
    result
  }

  /**vm options: -Xss8m
   * Program arguments: ../scripts/hanoi.b -O 2
   */
  def main(args: Array[String]): Unit = {
    val optionParser = new OptionParser[Config]("brainsuck") {
      head("brainsuck", "0.1.0")

      opt[Int]('O', "optimize")
        .optional()
        .text("Optimization level.")
        .action { (level, config) => config.copy(optimizationLevel = level) }

      arg[File]("<input file>")
        .maxOccurs(1)
        .required()
        .text("Input file.")
        .action { (input, config) => config.copy(input = input) }
    }

    optionParser.parse(args, Config()).foreach {
      case Config(optimizationLevel, input) =>
        val code = benchmark("Parsing") {
          BrainsuckParser(Source.fromInputStream(getClass.getResourceAsStream(input.toString)).mkString)
        }

        val optimizer = new Optimizer {
          override def batches =
            Seq(
              Batch("Contraction", MergeAdds :: MergeMoves :: Nil, FixedPoint.Unlimited),
              Batch("LoopSimplification", Clears :: Scans :: MultisAndCopies :: Nil, Once)
            ).take(optimizationLevel)
        }

        val optimized = benchmark("Optimization") {
          if (optimizationLevel > 0) optimizer(code) else code
        }

        benchmark("Execution") {
          Instruction.untilHalt(optimized, new Machine(0, new Memory()))
        }
    }
  }
}
