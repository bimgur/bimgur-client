package ch.fhnw.ima.bimgur
package service

import ch.fhnw.ima.bimgur.model.activiti._
import org.scalajs.dom.ext.Ajax

import scala.concurrent.Future
import cats.data.Xor
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

case class FormService(activitiRestUrl: String) {

  val url = activitiRestUrl + "/form/form-data"

  def getFormData(processDefinitionId: ProcessDefinitionId): Future[Xor[Error, FormData]] = {
    val urlWithParams = s"$url?processDefinitionId=$processDefinitionId"
    val requestFuture = Ajax.get(urlWithParams)
    requestFuture.map(response => decode[FormData](response.responseText))
  }

}