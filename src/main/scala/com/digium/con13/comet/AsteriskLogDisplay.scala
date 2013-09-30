package com.digium.con13.comet

import net.liftweb.http.{SHtml, CometListener, CometActor}
import com.digium.con13.model._
import net.liftweb.json
import net.liftweb.http.js.{JsMember, JsExp, JE, JsCmd}
import net.liftweb.http.js.jquery.{JqJE, JqJsCmds}

class AsteriskLogDisplay extends CometActor with CometListener {
  private[this] var msgs: List[LogItem] = Nil
  private[this] var idCounter = 0

  protected def registerWith = AsteriskLog

  override def lowPriority = {
    case Logs(items) => msgs = items; reRender()
  }

  def collapser = {
    val id = s"log-$idCounter"
    idCounter += 1
    val cmd = (JqJE.JqId(JE.Str(id)) ~> new JsExp with JsMember {
      def toJsCmd = "collapse('toggle')"
    }).cmd
    (id, SHtml.ajaxButton("+", () => cmd))
  }

  def renderLogItem(logItem: LogItem): xml.NodeSeq = logItem match {
    case event: AriEvent =>
      val (id, button) = collapser
      <div>
        <div class="log-head">{button} {event.eventHeader}</div>
        <div class="log-body collapse" id={id}><pre>{json.pretty(json.render(event.msg))}</pre></div>
      </div>
    case response: AriResponse =>
      <div>
        <div class="log-head">{s"${response.method} ${response.url} - ${response.code} (${response.codeText})"}</div>
        <div class="log-body collapse in"><pre>{json.pretty(json.render(response.body))}</pre></div>
      </div>
    case AriMessage(msg) =>
      <div>
        <div class="log-head">{msg}</div>
      </div>
  }

  def render = ".log-line" #> msgs.map(renderLogItem)
}