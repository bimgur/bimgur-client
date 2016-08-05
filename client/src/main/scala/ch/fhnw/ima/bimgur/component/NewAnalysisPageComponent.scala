package ch.fhnw.ima.bimgur.component

import ch.fhnw.ima.bimgur.component.pages.Page
import ch.fhnw.ima.bimgur.controller.BimgurController.RefreshMasterFormData
import ch.fhnw.ima.bimgur.model.activiti._
import diode.data.Pot
import diode.react.ModelProxy
import diode.react.ReactPot._
import japgolly.scalajs.react.{Callback, _}
import japgolly.scalajs.react.vdom.prefix_<^._

object NewAnalysisPageComponent {

  case class Props(proxy: ModelProxy[Pot[FormData]])

  class Backend($: BackendScope[Props, Unit]) {

    def lazyLoadMasterFormData(proxy: ModelProxy[Pot[FormData]]) =
      Callback.when(proxy().isEmpty)(proxy.dispatch(RefreshMasterFormData()))


    def render(p: Props) = {
      <.div(
        <.h3(Page.NewAnalysisPage.pageTitle),
        p.proxy().renderFailed(ex => <.div("Loading failed (Console log for details)")),
        p.proxy().renderPending(_ > 500, _ => <.div("Loading...")),
        p.proxy().renderReady(_ => FormDataComponent(p.proxy.zoom(_.head)))
      )
    }

  }

  private val component = ReactComponentB[Props](getClass.getSimpleName)
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.lazyLoadMasterFormData(scope.props.proxy))
    .build

  def apply(proxy: ModelProxy[Pot[FormData]]) = component(Props(proxy))

}
