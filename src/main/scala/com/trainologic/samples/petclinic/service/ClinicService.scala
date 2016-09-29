package com.trainologic.samples.petclinic.service
import com.trainologic.samples.petclinic._
import model.Owner
import repository.OwnerRepository
import scalaz.\/
import scalaz.concurrent.Task
import scalaz.Reader
import org.atnos.eff._
import org.atnos.eff.syntax.eff._
import org.atnos.eff.ReaderEffect._
import org.atnos.eff.ValidateEffect._
import scalaz.Monad

abstract class ClinicService[M[_] : Monad] {
  
	type S = Fx.fx4[M, DataAccessException \/  ?, Validate[String, ?], Reader[OwnerRepository[M], ?]]
  
  def saveOwner(owner: Owner):  Eff[S, Owner]
  
  def findOwnerByLastName(lastName: String) : Eff[S, Seq[Owner]] 
  
  def findOwnerById(id: Int): Eff[S, Owner]
  
}

class ClinicServiceImpl[M[_] : Monad] extends ClinicService[M] {
  
  
  override def findOwnerById(id: Int) = for {
	  c <- ask[S, OwnerRepository[M]]
    owner <- c.findById(id).into[S]
  } yield owner
  
	override def findOwnerByLastName(lastName: String) = for {
		c <- ask[S, OwnerRepository[M]]
		r <- c.findByLastName(lastName).into[S]
	} yield r
  
  
	def validateOwner(owner: Owner) = for {
    _ <- validateCheck(owner.lastName.nonEmpty, "last name should not be empty")
    _ <- validateCheck(owner.firstName.nonEmpty, "first name should not be empty")
    } yield ()
	
	
  override def saveOwner(owner: Owner):  Eff[S, Owner] = for {
    c <- ask[S, OwnerRepository[M]]
    _ <- validateOwner(owner).into[S]
    newOwner <- c.save(owner).into[S]
  } yield newOwner 
  
}