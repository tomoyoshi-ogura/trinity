package org.sisioh.trinity.domain.mvc.action

import org.sisioh.trinity.domain.mvc.http.{Response, Request}
import scala.concurrent.Future

trait SimpleAction extends Action[Request, Response]

object SimpleAction {

  def apply(f: (Request) => Future[Response]): SimpleAction = new SimpleAction {
    def apply(request: Request): Future[Response] = f(request)
  }

}