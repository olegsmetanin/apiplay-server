package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import Play.current

import play.api.templates._

import models._
import views._

object Application extends Controller {

  val modulesSrc = Map(
    "core.dev" -> List(

      "/ng-app/components/bootstrap-2.3.2/css/bootstrap.min.css",
      "/ng-app/components/bootstrap-2.3.2/css/bootstrap-responsive.min.css",
      "/ng-app/components/select2-3.4.1/select2.css",
      "/ng-app/components/jquery-ui-1.10.3/themes/smoothness/jquery-ui.css",
      "/ng-app/assets/ago-filter-builder.css",

      "/ng-app/components/jquery-1.9.1/jquery.min.js",
      "/ng-app/components/jquery-ui-1.10.3/ui/minified/jquery-ui.min.js",
      "/ng-app/components/bootstrap-2.3.2/js/bootstrap.min.js",
      "/ng-app/components/select2-3.4.1/select2.min.js",
      "/ng-app/components/angular-1.0.7/angular.js",
      "/ng-app/components/angular-mocks/angular-mocks.js",
      "/ng-app/components/angular-ui-router-0.0.1/release/angular-ui-router.js",
      "/ng-app/components/angular-ui-date-0.0.3/src/date.js",
      "/ng-app/components/angular-ui-select2-0.0.2/src/select2.js",
      "/ng-app/components/angular-ui-bootstrap-0.5.0/ui-bootstrap-0.5.0.min.js",
      "/ng-app/components/angular-ui-bootstrap-0.5.0/ui-bootstrap-tpls-0.5.0.min.js",

      "/ng-app/modules/core/_module.js",
      "/ng-app/modules/core/breadcrumbs/breadcrumbsCtrl.js",
      "/ng-app/modules/core/tabbar/tabbar.js",
      "/ng-app/modules/core/security/backendless.js",
      "/ng-app/modules/core/security/index.js",
      "/ng-app/modules/core/security/authorization.js",
      "/ng-app/modules/core/security/interceptor.js",
      "/ng-app/modules/core/security/retryQueue.js",
      "/ng-app/modules/core/security/security.js",
      "/ng-app/modules/core/security/login/login.js",
      "/ng-app/modules/core/security/login/LoginFormController.js",
      "/ng-app/modules/core/security/login/toolbar.js",
      "/ng-app/modules/core/services/services.js",
      "/ng-app/modules/core/services/localizedMessages.js",

      "/ng-app/modules/core/filters/ago-filter-builder.js",
      "/ng-app/modules/core/filters/ago-jquery-helpers.js",
      "/ng-app/modules/core/filters/ago-jquery-structured-filter.js",
      "/ng-app/modules/core/filters/ago-jquery-custom-properties-filter.js",
      "/ng-app/modules/core/filters/complex.js",

      "/ng-app/modules/core/systemmenu/systemMenuCtrl.js"),
    "core.prod" -> List(
      "/ng-app/css/styles.min.css",
      "/ng-app/js.min.js",
      "/ng-app/core.min.js"),
    "home.dev" -> List(
      "/ng-app/modules/home/_module.js",
      "/ng-app/modules/home/projects/projects.js"),
    "home.prod" -> List(
      "/ng-app/home.min.js"),
    "crm.dev" -> List(
      "/ng-app/modules/crm/_module.js",
      "/ng-app/modules/crm/contracts/list/contractListFilterCtrl.js"),
    "crm.prod" -> List(
      "/ng-app/crm.min.js"))

  def moduleAction(ngModule: String, js: String, backendMode: String = "") = Action {
    module(ngModule, js, backendMode);
  }

  def module(ngModule: String, js: String = "", backendMode: String = "") = {

    val efmode = Play.mode match {
      case Mode.Prod => "prod"
      case Mode.Dev => Play.configuration.getString("devmode", Some(Set("dev", "prod"))).getOrElse("prod")
      case _ => "prod"
    }

    modulesSrc.get(ngModule + "." + efmode) match {
      case Some(res) => {

        val tpl = efmode match {
          case "dev" => "angular.module('core.templates', [] ); angular.module('" + ngModule + ".templates', [] );"
          case _ => ""
        }

        val mod = backendMode match {
          case "fake" => "core" :: ngModule :: "core.security.backendless" :: Nil
          case _ => "core" :: ngModule :: Nil
        }

        val yepnope = """
var resourceList =
""" + Json.stringify(Json.toJson((modulesSrc.get("core." + efmode)).get ::: res)) + """;
var mod =
""" + Json.stringify(Json.toJson(mod)) + """;
var completeCb = function () {
angular.element(document).ready(function() {
    """ + tpl + js + """
    angular.bootstrap(document, mod);
});

};
""";
        Ok(html.main(title = "qwe", yepnopeScripts = Html(yepnope)));
      }
      case _ => NotFound
    }

  }

  def project(projectid: String, backendMode: String) = Action { request =>
    {
      Project.findByFolder(projectid) match {
        case Some(project: Project) => {
          val groups = request.session.get("user") match {
            case Some(user: String) => Project.findUserGroups(project, user)
            case _ => List()
          }
          module(project.prjtype,
            "window.app = {project:\"" + projectid + "\"}; \n" +
              "angular.module('core').constant('currentProject', '" + projectid + "')\n" +
              ".constant('userGroups', " + Json.stringify(Json.toJson(groups)) + ")",
            backendMode);
        }

        case _ => NotFound
      }

    }

  }

  def redirect(url: String) = Action {
    Redirect(url)
  }

}
