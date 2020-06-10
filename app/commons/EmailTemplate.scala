package commons

case class EmailTemplate(subject: String, body: String)

object EmailTemplate {

  def getCreatePublicStateEmailTemplate(invitedBy: String, token: String, createUrl: String, registerUrl: String): EmailTemplate = {
    val body =
      s"""
        |<b>TBD</b>
        |<br/>
        |<p>Invitation for creating a new public state</p>
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

  def getCreatePrivateStateInviteEmailTemplate(invitedBy: String, createUrl: String, registerUrl: String): EmailTemplate = {
    val body =
      s"""
        |<b>TBD</b>
        |<br/>
        |<p>Invitation for creating a new private state</p>
        |<br/><br/>
        |<p>invited by: $invitedBy</p>
        |<p>create url: $createUrl</p>
        |<p>register url: $registerUrl</p>
        |<br/><br/>
        |Have Fun ;-)
        |<br/><br/>
        |""".stripMargin
    EmailTemplate("create private state", body)
  }

  def getJoinPrivateStateInviteEmailTemplate(invitedBy: String, joinUrl: String, registerUrl: String): EmailTemplate = {
    val body =
      s"""
        |<b>TBD</b>
        |<br/>
        |<p>Invitation for creating a new private state</p>
        |<br/><br/>
        |<p>invited by: $invitedBy</p>
        |<p>create url: $joinUrl</p>
        |<p>register url: $registerUrl</p>
        |<br/><br/>
        |Have Fun ;-)
        |<br/><br/>
        |""".stripMargin
    EmailTemplate("create private state", body)
  }
}
