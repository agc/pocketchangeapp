package bootstrap.liftweb

import com.pocketchangeapp.model._
import net.liftweb._
import net.liftweb.mapper.{Schemifier,DB,StandardDBVendor,DefaultConnectionIdentifier}
import http.{LiftRules, NotFoundAsTemplate, ParsePath}

import sitemap.{SiteMap, Menu, Loc}

import net.liftweb.sitemap.Loc.If
import util.{Props, NamedPF}

class Boot {
  def boot {

    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor =
        new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
          Props.get("db.url") openOr
            "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
          Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }
  
    // where to search snippet
    LiftRules.addToPackages("com.pocketchangeapp")
   // val IfLoggedIn = net.liftweb.sitemap.Loc.If(() =>User.loggedIn_?, ()=>"You must be logged in")

    // build sitemap
    val entries = (List(Menu("Home") / "index") :::
      List[Menu](
        Menu(Loc("ManageAccounts",List("cuentas","manage"),"Gestionar cuentas")) ,
        Menu(Loc("AddAccount",List("cuentas","editAcct"),"Añadir cuentas")) ,
        Menu(Loc("ViewAccount",List("cuentas","viewAcct"),"Ver cuentas")) ,

        Menu("Help") / "help" / "index") :::
                  User.sitemap :::
                  
                  Nil)
    
    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) => NotFoundAsTemplate(
        ParsePath(List("exceptions","404"),"html",false,false))
    })
    
    LiftRules.setSiteMap(SiteMap(entries:_*))
    
    // set character encoding
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    Schemifier.schemify(true, Schemifier.infoF _, User,Account,AccountAdmin,AccountNote,AccountViewer,Expense,Tag)
    
    
    
  }
}