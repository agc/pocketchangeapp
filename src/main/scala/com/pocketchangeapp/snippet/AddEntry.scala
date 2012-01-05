package com.pocketchangeapp.snippet

import scala.xml.{NodeSeq,Text}

import com.pocketchangeapp.util.Util

import net.liftweb.util.Helpers._

import com.pocketchangeapp.model._

import net.liftweb.http.{FileParamHolder,S,SHtml,StatefulSnippet}

import java.util.Date
import net.liftweb.common.{Empty, Full}

/* date | desc | tags | value */
class AddEntry extends StatefulSnippet {
  // This maps the "addentry" XML element to the "add" method below
  def dispatch = {
    case "addentry" => add _
  }

  var account : Long = _
  var date = ""
  var desc = ""
  var value = ""
  // S.param("tag") returns a "Box" and the "openOr" method returns
  // either the contents of that box (if it is "Full"), or the empty
  // String passed to it, if the Box is "Empty". The S.param method
  // returns parameters passed by the browser. In this instance, the
  // name of the parameter is "tag".
  var tags = S.param("tag") openOr ""

  def add(in: NodeSeq): NodeSeq = User.currentUser match {
    case Full(user) if user.editable.size > 0 => {
      def doTagsAndSubmit(t: String) {
        tags = t
        if (tags.trim.length == 0)
          S.error("We’re going to need at least one tag.")
        else {
          // Get the date correctly, comes in as yyyy/mm/dd
          val entryDate = Util.slashDate.parse(date)
          val amount = BigDecimal(value)
          val currentAccount = Account.find(account).open_!
          // We need to determine the last serial number and balance
          // for the date in question. This method returns two values
          // which are placed in entrySerial and entryBalance
          // respectively
          val (entrySerial, entryBalance) =
            Expense.getLastExpenseData(currentAccount, entryDate)

          val e = Expense.create.account(account)
            .dateOf(entryDate)
            .serialNumber(entrySerial + 1)
            .description(desc)
            .amount(BigDecimal(value)).tags(tags)
            .currentBalance(entryBalance + amount)
            // The validate method returns Nil if there are no errors,
          // or an error message if errors are found.
          e.validate match {
            case Nil => {
              Expense.updateEntries(entrySerial + 1, amount)
              e.save
              val acct = Account.find(account).open_!
              val newBalance = acct.balance.is + e.amount.is
              acct.balance(newBalance).save
              S.notice("Entry added!")
              // remove the statefullness of this snippet
              unregisterThisSnippet()
            }
            case x => sys.error(x.toString())
          }
        }
      }

      val allAccounts = user.allAccounts.map(acct => (acct.id.toString, acct.name.toString))
         // Parse through the NodeSeq passed as "in" looking for tags
      // prefixed with "e". When found, replace the tag with a NodeSeq
      // according to the map below (name -> NodeSeq)
      bind("e", in,
        "account" -> SHtml.select(allAccounts, Empty,id => account = id.toLong),
        "dateOf" -> SHtml.text(Util.slashDate.format(new Date()).toString,
          date = _,
          "id" -> "entrydate"),
        "desc" ->  SHtml.text("Item Description", desc = _),
        "value" ->  SHtml.text("Value", value = _),
        "tags" ->  SHtml.text(tags, doTagsAndSubmit))
    }
    // If no user logged in, return a blank Text node
    case _ => Text("")
  }
}