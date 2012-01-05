package com.pocketchangeapp.snippet

import net.liftweb.common.{Full}
import scala.xml.{NodeSeq,Text}
import net.liftweb.util.Helpers._
import com.pocketchangeapp.model._
import java.util.Date

class HomePage {
  // User.currentUser returns a "Box" object, which is either Full
  // (i.e. contains a User), Failure (contains error data), or Empty.
  // The Scala match method is used to select an action to take based
  // on whether the Box is Full, or not ("case _" catches anything
  // not caught by "case Full(user)". See Box in the Lift API. We also
  // briefly discuss Box in Appendix C.
  def summary (xhtml : NodeSeq) : NodeSeq = User.currentUser match {
    case Full(user) => {
      val entries : NodeSeq = user.allAccounts match {
        case Nil => Text("You have no accounts set up")
        case accounts => accounts.flatMap({account =>
          bind("acct", chooseTemplate("account", "entry", xhtml),
            "name" -> <a href={"/account/" + account.name.is}>
              {account.name.is}</a>,
            "balance" -> Text(account.balance.toString))
        })
      }
      bind("account", xhtml, "entry" -> entries)
    }
    case _ => <lift:embed what="welcome_msg" />
  }
}