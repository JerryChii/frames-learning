package com.chii.akka.stream.sample.QuickStartGuide

import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult, OverflowStrategy, ThrottleMode}
import akka.util.ByteString

import scala.concurrent.Future


/**
  * Describe:
  * Author:  JerryChii.
  * Date:    2016/9/4
  */
object SimpleOper extends App{
  private implicit val system = ActorSystem("QuickStart")
  private implicit val materializer = ActorMaterializer()

  /**
    * Source is just a description of what you want to run, and like an architect’s blueprint it can be reused,
    * incorporated into a larger design
    */
  val source: Source[Int, NotUsed] = Source(1 to 100)
  source.runForeach(i => println(i))

  /**
    * we use the scan combinator to run a computation over the whole stream: starting with the number 1 (BigInt(1))
    * we multiple by each of the incoming numbers, one after the other;
    */
  val factorials = source.scan(BigInt(1))((acc, next) =>  acc * next)


  /** we convert the resulting series of numbers into a stream of ByteString objects describing lines in a text file. */
  val result: Future[IOResult] =
  factorials.map(num => ByteString(s"$num\n"))
    /** runWith the same with toMat(FileIO.toPath(Paths.get("factorials.txt")))(Keep.right).run()*/
    .runWith(FileIO.toPath(Paths.get("factorials.txt")))

  /** we need a starting point that is like a source but with an “open” input.
    * In Akka Streams this is called a Flow
    * It is good for reuse
    */
  def lineSink(fileName: String): Sink[String, Future[IOResult]] =
  Flow[String]
    .map(s => ByteString(s + "\n"))
    .toMat(FileIO.toPath(Paths.get(fileName)))(Keep.right)

  factorials.map(_.toString).runWith(lineSink("factorials.txt"))

  /**************************Time-Based Processing***************************/

  /**
    * print one element per second
    * idx represent element of factorials
    * num represent element of Source(0 to 100)
    **/
  import scala.concurrent.duration._
  factorials
    .zipWith(Source(0 to 2))((num, idx) => s"$idx! = $num")
    .throttle(1, 1 seconds, 1, ThrottleMode.shaping)
    .runForeach(println)

  /**************************Buffer***************************/
  Source(1 to 20)
    .buffer(10, OverflowStrategy.dropHead)
    .runForeach(println)
}