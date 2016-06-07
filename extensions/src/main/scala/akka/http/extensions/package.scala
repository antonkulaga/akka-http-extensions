package akka.http

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl._
import akka.stream.stage._

import scala.concurrent.Future

package object extensions {

  implicit class FlowOpsFutureExt[Inp, T, M](val flow: Flow[Inp, Future[T], M]) {
    def sync: Flow[Inp, T, M] = flow.mapAsync(1)(identity(_))
    def async(parallel: Int): Flow[Inp, T, M] = flow.mapAsync(parallel)(identity(_))
  }


  implicit class FlowOpsExt[Inp, T, M](val flow: Flow[Inp, T, M]) {

    def upTo(fun: T => Boolean): Flow[Inp, T, M] = flow.via(FlowUpTo(fun))

    def upToExcl(fun: T => Boolean): Flow[Inp, T, M] = flow via FlowUpTo(fun, inclusive = false)

    /**
      * Works similar to ZipWith but zips its input with output of the flow through which it goes to
      * It is useful to making requests somewhere and tracking original value
      * @param other Flow to go through
      * @param combine combine my input with output
      * @tparam U Type of other flow output
      * @tparam O Result of combination
      * @return
      */
    def inputZipWith[U, O](other: Flow[T, U, M])(combine: (Inp, U)=> O): Flow[Inp, O, NotUsed] = Flow.fromGraph(GraphDSL.create(){ implicit builder=>
      import GraphDSL.Implicits._
      val b = builder.add(Broadcast[Inp](2))
      val me = builder.add(flow)
      val pipe: FlowShape[T, U] = builder.add(other)
      val zip = builder.add(ZipWith[Inp, U, O](combine))
      b.out(0) ~> zip.in0
      b.out(1) ~> me ~> pipe ~> zip.in1
      FlowShape[Inp, O](b.in, zip.out)
    })
  }

  implicit class SourceExt[T, M](val source: Source[T, M]) {

    def upTo(fun: T => Boolean): Source[T, M] = source via FlowUpTo(fun)

    def upToExcl(fun: T => Boolean): Source[T, M] = source via FlowUpTo(fun, inclusive = false)

  }

}

class MapPartial[Input, Output](fun: PartialFunction[Input, Output]) extends FlowStage[Input, Output]{
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    self=>

    setHandler(in, new InHandler {
      @throws[Exception](classOf[Exception])
      override def onPush(): Unit = {
        val element = self.grab(in)
        if(fun.isDefinedAt(element)) {
          val value = fun(element)
          push(out, value)
        } else self.completeStage()
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit ={
        pull(in)
      }
    })

  }
}



//inclusive flow
class UpToStage[T](fun: T => Boolean, inclusive: Boolean = true) extends FlowStage[T, T]{


  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    self=>

    setHandler(in, new InHandler {
      @throws[Exception](classOf[Exception])
      override def onPush(): Unit = {
        val element = self.grab(in)
        if(fun(element)) {
          if(inclusive) push(out, element)
          self.completeStage()
        } else push(out, element)
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit ={
        pull(in)
      }
    })

  }
}

object FlowUpTo {

  def apply[T](fun: T => Boolean): Flow[T, T, NotUsed] = Flow.fromGraph(new UpToStage[T](fun))

  def apply[T, M](fun: T => Boolean, inclusive: Boolean): Flow[T, T, NotUsed] = Flow.fromGraph(new UpToStage[T](fun, inclusive))

}

trait FlowStage[In, Out] extends GraphStage[FlowShape[In, Out]]{

  val in = Inlet[In]("Input")

  val out= Outlet[Out]("Output")

  val shape = new FlowShape[In, Out](in, out)
}