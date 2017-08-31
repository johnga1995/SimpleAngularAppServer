package Database

/**
  * Created by juan on 30/08/17.
  */
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.connectors.{CassandraConnection, ContactPoints}

import scala.concurrent.Future
import com.outworkers.phantom.dsl._
import java.util.UUID

import org.joda.time.DateTime

import scala.concurrent.Future
import com.outworkers.phantom.builder.query.CreateQuery
import play.api.libs.json.{Json, OFormat}

import scala.concurrent.duration._


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

case class User(
                 email: String,
                 full_name: String,
                 age: Int
               )

object User{implicit val jsonFormat: OFormat[User] = Json.format[User]} // used for formatting to Json Possible

abstract class Users extends CassandraTable[Users, User] with RootConnector {

  override val tableName = "user"

  object email extends StringColumn(this) with PartitionKey
  object full_name extends StringColumn(this)
  object age extends IntColumn(this)

  override def fromRow(row: Row): User = {
    User(
      email = email(row),
      full_name = full_name(row),
      age = age(row)
    )
  }

  // define the queries here

  // add new entry to table

  def add(user: User): Future[ResultSet] = {
    insert
      .value(_.email, user.email)
      .value(_.full_name, user.full_name)
      .value(_.age, user.age)
      .future()
  }

  // retrieve user by email (primary key)

  def getByEmail(email: String): Future[Option[User]] = {
    select.
      where(_.email eqs email).
      one()
  }


  // Update current existing entry

  def update(user: User) : Future[ResultSet] = {
    update()
      .where(_.email eqs user.email)
      .modify(_.full_name setTo user.full_name)
        .and(_.age setTo user.age)
      .future()
  }


  // delete user by id

  def delete(email: String) : Future[ResultSet] = {

    delete()
      .where(_.email eqs email)
      .future()

  }

  // select all users

  def getAllUsers(): Future[List[User]] = {

    select.all().fetch()

  }


}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

object Defaults {
  val connector = ContactPoints(Seq("127.0.0.1")).keySpace("simpleapp")
}

class UserDAO (val keyspace: CassandraConnection) extends Database[UserDAO](keyspace) {
  object users extends Users with Connector
  {
    override def autocreate(space: KeySpace): CreateQuery.Default[Users, User] = {
      create.ifNotExists()(space)
        .option(default_time_to_live eqs 10)
        .and(gc_grace_seconds eqs 10.seconds)
        .and(read_repair_chance eqs 0.2)
    }
  }

  def insert(m: User): Future[User] =  users.add(m).map(_ => m)
  def getByEmail(m: User): Future[Option[User]] =  users.getByEmail(m.email)
  def update(m: User): Future[User] =  users.update(m).map(_ => m)
  def delete(m: User): Future[_] =  users.delete(m.email)
  def getAll(): Future[List[User]] =  users.getAllUsers()
}

object UserDAO extends UserDAO(Defaults.connector)