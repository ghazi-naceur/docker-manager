package in.oss.docker.manager.components

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLDivElement

import scala.scalajs.js.timers.setTimeout

object Notification {

  private val errorsVar: Var[String] = Var("")

  def storeError(message: String): Unit = {
    errorsVar.set(message)
    setTimeout(5000) {
      if (errorsVar.now() == message) errorsVar.set("")
    }
  }

  def display(): ReactiveHtmlElement[HTMLDivElement] = {
    div(
      cls       := "toast-container position-fixed top-0 end-0 p-3",
      styleAttr := "z-index: 1055;",
      child <-- errorsVar.signal.map {
        case "" => emptyNode
        case error =>
          div(
            cls         := "toast show align-items-center text-bg-danger border-0",
            role        := "alert",
            aria.live   := "assertive",
            aria.atomic := true,
            div(
              cls := "d-flex",
              div(cls := "toast-body", error),
              button(
                tpe        := "button",
                cls        := "btn-close btn-close-white me-2 m-auto",
                aria.label := "Close",
                onClick.mapTo("") --> errorsVar.writer
              )
            )
          )
      }
    )
  }
}
