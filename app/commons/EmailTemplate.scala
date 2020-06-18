package commons

case class EmailTemplate(subject: String, body: String)

object EmailTemplate {

  def getRegisterEmail(username: String, confirmUrl: String): EmailTemplate = {
    val body =
      s"""
        |<p>Hello $username</p>
        |
        |<p>You made it.</p>
        |<p>Welcome to Momentary States, a hybrid social game about making creating and negotiating rules.</p>
        |<p>The first step is done. Now, the universe is open for you.</p>
        |<p>Now you can create, together with others, your momentary states.</p>
        |<br/>
        |<p>But wait. Just in case you are new, you can finish the registration process by clicking this <a href="$confirmUrl">link</a> in order to confirm your email address.</p>
        |<br/>
        |<p>All the best, we are looking forward to see you on Momentary States.</p>
        |<br/>
        |""".stripMargin
    EmailTemplate("Momentary States - Registrierung", body)
  }

  def getCreatePublicStateEmailTemplate(invitedBy: String, token: String, createUrl: String, registerUrl: String): EmailTemplate = {
    val body =
      s"""
        |<b>Hello future goddess,</b>
        |<p>you have been invited to Momentary States- a hybrid social game about making creating and negotiating societies and their rules. You are about to become GODDESS in the game.</p>
        |<br/>
        |<p>Follow this <a href="$createUrl">link</a> and start creating a speculation of possible futures.</p>
        |<br/>
        |<p>All the best</p>
        |<p>Your game master $invitedBy</p>
        |""".stripMargin
    EmailTemplate("Become a Goddess", body)
  }

  def getCreatePrivateStateInviteEmailTemplate(invitedBy: String, speculationName: String, createUrl: String, registerUrl: String): EmailTemplate = {
    val body =
      s"""
        |<b>Hello</b>
        |<br/>
        |<p>You have been invited to the speculation $speculationName.</p>
        |<br/>
        |<p>Speculation $speculationName is part of Momentary States- a hybrid social game about creating and negotiating societies and their rules. You are about to become CITIZEN of a so-called private state in the game. And YOU are going to start that state- and invite others to build it together with you.</p>
        |<br/>
        |<p>Follow this <a href="$createUrl">link</a> to create your private state.</p>
        |<br/>
        |<p>Then you can start building a private state together with other players.</p>
        |<br/>
        |<p>All the best,</p>
        |<p>your goddess $invitedBy</p>
        |<br/>
        |""".stripMargin
    EmailTemplate("Create Private State", body)
  }

  def getJoinPrivateStateInviteEmailTemplate(invitedBy: String, speculationName: String, joinUrl: String, registerUrl: String): EmailTemplate = {
    val body =
      s"""
        |<b>Hello future citizen</b>
        |<br/>
        |<p>You have been invited to the speculation $speculationName.</p>
        |<p>Speculation $speculationName is part of Momentary States- a hybrid social game about creating and negotiating societies and their rules. You are about to become CITIZEN of a so-called private state in the game. And YOU are going to start that state- and invite others to build it together with you.
        |<br/>
        |<p>Follow this <a href="$joinUrl">link</a> to create your private state.</p>
        |<br/>
        |<p>Then you can start building a private state together with other players.</p>
        |<br/>
        |<p>All the best,</p>
        |<p>your $invitedBy</p
        |""".stripMargin
    EmailTemplate("Join a private state", body)
  }
}
