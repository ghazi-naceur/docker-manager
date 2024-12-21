package in.oss.docker.manager.fe

import com.raquo.airstream.ownership.OneTimeOwner
import com.raquo.airstream.timing.PeriodicStream
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import org.scalajs.dom.HTMLDivElement

import scala.util.Try

object App {

  def main(args: Array[String]): Unit = {
    val containerNode = dom.document.querySelector("#app") // 'app' is the main div in index.html file
//    render(containerNode, div("Docker Manager")) // 'render' will append the div to the containerNode as its child
    render(
      containerNode,
//      Elements.staticContent
//      Elements.timeUpdated
//      Elements.clickUpdated
//      Elements.clicksQueried
//      Elements.clicksVarWithUpdater
//      Elements.clicksVarWithWriter
      Elements.clicksVarWithSet
    )
  }
}

object Elements {

  val staticContent: ReactiveHtmlElement[HTMLDivElement] = div(
    styleAttr := "color:red",
    p(
      "This is a simple web application to manage Docker containers."
    )
  )

  // Reactive variables:
//  1- EventStream: produces values of the same type
  val ticks: PeriodicStream[Int] = EventStream.periodic(1000)
  // To consume the stream, we need a 'Subscription', for that we need the library 'AirStream', which only manages reactive values
  // like Event streams, Events buses, Signals and variables
  val subscription: Subscription = ticks.addObserver(new Observer[Int] {
    override def onError(err: Throwable): Unit = ()

    override def onTry(nextValue: Try[Int]): Unit = ()

    override def onNext(nextValue: Int): Unit = dom.console.log(s"Ticks: $nextValue")
  })(new OneTimeOwner(() => ()))
  // Now we need to add the observer to an event stream, I need to add an 'Owner' which a data structure that will manage
  // a subscription so that we don't have a leaky resources.
  // To kill resources manually, you can use 'steTimeout'
  scala.scalajs.js.timers.setTimeout(10000)(subscription.kill())

  // In Laminar, you don't need to create subscriptions and observers yourself. Rather, you will bind event streams to attributes
  // of your HTML components
  val timeUpdated: ReactiveHtmlElement[HTMLDivElement] = {
    div(
      span("Time since loaded: "),
      child <-- ticks.map(number => s"$number seconds") // "<--" binds 'child' to the event stream 'ticks'
      // 'ticks.map(number => s"$number seconds")' becomes a new event stream of Strings that will emit a new element, once the
      // 'ticks' event stream emits a new Int element. and the new value will always be produced as a child element of this 'div'.
      // This subscription which is added automatically by binding 'child' to the event stream, is killed only when this
      // 'child' is evicted or cut out from the 'div'
    )
  }

//  2- EventBus: like EventStream, but you can push new elements to the stream
  val clickEvents: EventBus[Int] = EventBus[Int]()
  val clickUpdated: ReactiveHtmlElement[HTMLDivElement] = div(
    span("Clicks since loaded: "),
    button(
      `type`    := "button",
      styleAttr := "display: block", // display on another row
      onClick.map(_ => 1) --> clickEvents, // 'onClick' can be mapped or bound to an event bus. Now click elements are fed into
      // the event bus using '-->', with the only different 'on Clicks' are of type 'mouse event' and 'clickEvents' are
      // of type 'Int', so we need to '.map' the 'onClick' to be transformed into 'Int', by attributing '1' to every click event.
      "Add a click"
    ),
    child <-- clickEvents.events.scanLeft(0)(_ + _).map(number => s"$number clicks") // 'scanLeft' is similar to 'foldLeft'.
    // It's summing all the click numbers.
  )

//  3- Signal: Similar to EventStream. The only difference between them, is that EventStream doesn't have a current value.
  // EventStreams will produce new elements, but there is no such thing as a current or latest value or something like that
  // that you can query or that you can inspect. EventStream can only emit elements and you can listen for these events.
  // Signal, in the other hand, has a current value, which is very useful because we can model state with it.
  // When you bind an attribute like 'child' or a piece of text or an attribute to a 'Signal', that thing will only change
  // when the current value or state changes, so not necessarily when it emits a new value.
  // 'Signal' produces changes, only when the 'current value' or the 'state' is different or has changed.
  // 'clickEvents.events.scanLeft(0)(_ + _)' is actually a Signal.
  val countSignal: OwnedSignal[Int] = clickEvents.events.scanLeft(0)(_ + _).observe((new OneTimeOwner(() => ())))
  // This can be inspected for the current state anytime you want, if Laminar/Airstream knows that it has an owner
//  val currentState: Int = countSignal.now() // 'now' method can only be invoked on things that are proven to have an owner
  val queryEvents: EventBus[Unit] = EventBus[Unit]()
  val clicksQueried: ReactiveHtmlElement[HTMLDivElement] = div(
    span("Clicks since loaded: "),
    button(
      `type`    := "button",
      styleAttr := "display: block",
      onClick.map(_ => 1) --> clickEvents,
      "Add a click"
    ),
    button(
      `type`    := "button",
      styleAttr := "display: block",
      onClick.mapTo(()) --> queryEvents, // We're sending Units to 'queryEvents'
      "Refresh count"
    ),
    child <-- queryEvents.events.map(_ => countSignal.now()) // Whenever we receive a Unit(or whatever), we're going to
    // refresh the count with the current click value. So we control the refresh using the second button
  )

//  4- Var: Reactive Variable - the most powerful tool, as it allows to read and write data, and also store the current value
  val countVar: Var[Int] = Var[Int](0)
  val clicksVarWithUpdater: ReactiveHtmlElement[HTMLDivElement] = div(
    span("Clicks so far: "),
    button(
      `type`    := "button",
      styleAttr := "display: block",
      onClick --> countVar.updater((current, event) => current + 1), // 'updater' is an observer of clicks. It will take
      // the 'click element' of type 'MouseEvent' and take the current value of the variable as well and it will produce
      // a new value that will be stored inside this var 'countVar'
      "Add a click"
    ),
    child <-- countVar.signal.map(_.toString) // 'signal' allows to listen to changes for this variable 'countVar'
  )

  val clicksVarWithWriter: ReactiveHtmlElement[HTMLDivElement] = div(
    span("Clicks so far: "),
    button(
      `type`    := "button",
      styleAttr := "display: block",
      onClick --> countVar.writer.contramap(event => countVar.now() + 1), // 'writer' is an observer of type Int
      // 'contramap' turns mouse clicks or events into Int
      // Wherever the button is clicked, the countVar will turn the click button event into the function 'event => countVar.now() + 1'
      "Add a click"
    ),
    child <-- countVar.signal.map(_.toString) // 'signal' allows to listen to changes for this variable 'countVar'
  )

  val clicksVarWithSet: ReactiveHtmlElement[HTMLDivElement] = div(
    span("Clicks so far: "),
    button(
      `type`    := "button",
      styleAttr := "display: block",
      onClick --> (_ => countVar.set(countVar.now() + 1)), // Binding the 'onClick' to a lambda function
      "Add a click"
    ),
    child <-- countVar.signal.map(_.toString)
  )

  /*
            |        no state           |       with state
  ----------|-------------------------------------------------------
     read   |       EventStream         |         Signal
  ----------|-------------------------------------------------------
     write  |         EventBus          |          Var
  ------------------------------------------------------------------
   */
}
