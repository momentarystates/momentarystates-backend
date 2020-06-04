package commons

case class EmailTemplate(subject: String, body: String)

object EmailTemplate {

  def getCreatePublicStateEmailTemplate(invitedBy: String, token: String, createUrl: String, registerUrl: String): EmailTemplate = {
    val body =
      s"""
        |<b>TBD</b>
        |<br/><br/>
        |<p>invited by: $invitedBy</p>
        |<p>token: $token</p>
        |<p>create url: $createUrl</p>
        |<p>register url: $registerUrl</p>
        |<br/><br/>
        |Have Fun ;-)
        |<br/><br/>
        |""".stripMargin
    EmailTemplate("create a public state", body)
  }
}
