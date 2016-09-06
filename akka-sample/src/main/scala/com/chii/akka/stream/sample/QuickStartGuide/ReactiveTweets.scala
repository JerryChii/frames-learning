package com.chii.akka.stream.sample.QuickStartGuide

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, OverflowStrategy}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.Future

/**
  * Describe:   
  * Author:  JerryChii.
  * Date:    2016/9/4
  */
object ReactiveTweets extends App{
  private implicit val system = ActorSystem("reactive-tweets")
  private implicit val materializer = ActorMaterializer()

  val akka = Hashtag("#akka")

  //todo to get obvious test result, you should evaluate the source
  val tweets: Source[Tweet, NotUsed] = Source.empty
  val authors: Source[Author, NotUsed] = tweets.filter(_.hashtags.contains(akka))
    .map(_.author)

  /** the same as authors.runForeach(println) */
  authors.runWith(Sink.foreach(println))

  /**  similarly like flatMap
    * Please note that the mapConcat requires the supplied function to return a strict collection (f:Out=>immutable.Seq[T]),
    * whereas flatMap would have to operate on streams all the way through.
    */
  val hashtags: Source[Hashtag, NotUsed]  = tweets.mapConcat(_.hashtags.toList)

  /****************************Broadcasting a stream*********************************/
  /**
    * Elements that can be used to form such "fan-out" (or "fan-in") structures are referred to as "junctions" in Akka Streams.
    */
  val writeAuthors: Sink[Author, Unit] = ???
  val writeHashtags: Sink[Hashtag, Unit] = ???

  /** Both Graph and RunnableGraph are immutable, thread-safe, and freely shareable.*/
  val g = RunnableGraph.fromGraph(
    /**return type -> Graph[ClosedShape, Unit]*/
    GraphDSL.create() {
    implicit b =>
      import GraphDSL.Implicits._

      //number of output ports
      val bcast = b.add(Broadcast[Tweet](2))

      /**
        * ~> "edge operator" (also read as "connect" or "via" or "to").
        * The operator is provided implicitly by importing GraphDSL.Implicits._.
        */
      tweets ~> bcast.in
      bcast.out(0) ~> Flow[Tweet].map(_.author) ~> writeAuthors
      bcast.out(1) ~> Flow[Tweet].mapConcat(_.hashtags.toList) ~> writeHashtags
      /**
        * ClosedShape means that it is a fully connected graph or "closed" - there are no unconnected inputs or outputs.
        *
        */
      ClosedShape
  })
  g.run()

  /***********************************buffer******************************************/
  val slowComputationOrSomethingElse = (x: Any) => x

  tweets
    .buffer(10, OverflowStrategy.dropHead)
    .map(slowComputationOrSomethingElse)
    .runWith(Sink.ignore)

  /**********************Materialized values*****************************/
  /**
    * count the num of tweets
    */
  val count: Flow[Tweet, Int, NotUsed] = Flow[Tweet].map(_ => 1)
  val sumSink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_+_)

  val counterGraph: RunnableGraph[Future[Int]] = tweets.via(count).toMat(sumSink)(Keep.right)

  val sum: Future[Int] = counterGraph.run()

  sum.foreach(c => println(s"Total tweets processed: $c"))

  /**the above code can be replaced by the flow one*/
  private val sum0: Future[Int] = tweets.map(t => 1).runWith(sumSink)
  sum0.foreach(c => println(s"Total tweets processed: $c"))

}
/*********************Reactive Tweets*******************/
final case class Author(handle: String)

final case class Hashtag(name: String)

final case class Tweet(author: Author, timestamp: Long, body: String) {
  def hashtags: Set[Hashtag] =
    body.split(" ").collect { case t if t.startsWith("#") => Hashtag(t) }.toSet
}