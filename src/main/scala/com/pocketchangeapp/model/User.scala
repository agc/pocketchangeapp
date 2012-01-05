package com.pocketchangeapp.model

import net.liftweb.mapper.{By,MegaProtoUser,MetaMegaProtoUser}
import net.liftweb.common.Full

// User class extiende la clase base Mapper
// MegaProtoUser, proporciona campos y métodos por defecto para un usuario del sitio

// La clase User generado por lifty desciende de LongKeyedMapper
// El objeto User extiende     LongKeyedMetaMapper

class User extends MegaProtoUser[User] {
  def getSingleton = User // reference to the companion object below
  def accounts : List[Account] = Account.findAll(By(Account.owner, this.id))

  def administered : List[Account] = AccountAdmin.findAll(By(AccountAdmin.administrator, this.id)).map(_.account.obj.open_!)

  def editable = accounts ++ administered

  def viewed : List[Account] = AccountViewer.findAll(By(AccountViewer.viewer, this.id)).map(_.account.obj.open_!)

  def allAccounts : List[Account] = accounts ::: administered ::: viewed

}

// Create a "companion object" to the User class (above).
// The companion object is a "singleton" object that shares the same
// name as its companion class. It provides global (i.e. non-instance)
// methods and fields, such as find, dbTableName, dbIndexes, etc.
// For more, see the Scala documentation on singleton objects



object User extends User with MetaMegaProtoUser[User] {
  // nombre de la tabla
  override def dbTableName = "users"

  // Plantilla de login
//  override def loginXhtml =
//    <lift:surround with="default" at="content">
//      { super.loginXhtml }
//    </lift:surround>
//
//    // Plantilla para darse de alta
//  override def signupXhtml(user: User) =
//    <lift:surround with="default" at="content">
//      { super.signupXhtml(user) }
//    </lift:surround>

  override def skipEmailValidation = true

  // Spruce up the forms a bit
  override def screenWrap =
    Full(<lift:surround with="default" at="content"><div id="formBox"><lift:bind /></div></lift:surround>)

  // define the order fields will appear in forms and output
  //override def fieldOrder = id :: firstName :: lastName :: email :: password :: Nil
}




/**
 * An O-R mapped "User" class that includes first name, last name, password and we add a "Personal Essay" to it
 */
