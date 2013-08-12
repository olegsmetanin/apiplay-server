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

  val modules = List("home", "crm");

  def moduleAction(ngModule: String, js: String, backendMode: String = "") = Action {
    module(ngModule, js, backendMode);
  }

  def module(ngModule: String, js: String = "", backendMode: String = "") = {

    val efmode = Play.mode match {
      case Mode.Prod => "prod"
      case Mode.Dev => Play.configuration.getString("devmode", Some(Set("dev", "prod"))).getOrElse("prod")
      case _ => "prod"
    }

    modules.contains(ngModule) match {
      case true => {

        val tpl = efmode match {
          case "dev" => "angular.module('core.templates', [] ); angular.module('" + ngModule + ".templates', [] );"
          case _ => ""
        }

        val mod = backendMode match {
          case "fake" => "core" :: ngModule :: "core.security.backendless" :: Nil
          case _ => "core" :: ngModule :: Nil
        }

        val srcPrefix="src/";
        val sitePrefix="";

        val yepnope = s"""


    (function() {

        // Dynamic section begin

        // Constant part

        var sysConfig = {
            src: function(path) {return "$srcPrefix"+path},
            site: function(path) {return "$sitePrefix"+path},
            module: "$ngModule"
        }

        // in Develop mode
        yepnope({
            load: [sysConfig.src('include.js'), sysConfig.src('core/include.js'), sysConfig.src('backend/include.js'), sysConfig.src(sysConfig.module + '/include.js')],            complete: function() {
                LoadResources(function() {
                    angular.element(document).ready(function() {

                        angular.module('core.templates', []);
                        angular.module(sysConfig.module + '.templates', []);

                        angular.module('core').constant('currentProject', 'play2').constant('userGroups', []).constant('sysConfig', sysConfig);

                        angular.bootstrap(document, ['core', sysConfig.module]);
                    });
                });
            }
        });

        /*        // in Production mode

        var componentsJS = [
            "assets/style.min.css",
            "components.min.js"
        ];

        var coreJS = [
            "core.min.js"
        ];

        var moduleJS = [
            "home.min.js"
        ];

        LoadResources(function() {
            angular.element(document).ready(function() {

                angular.module('core').constant('currentProject', null).constant('userGroups', []).constant('relPrefix', ''); // important to get empty string!

                angular.bootstrap(document, ['core', module]);
            });
        });
*/

        // Dinamic section end

        function LoadResources(completeCb) {

            resourceJS = componentsJS.concat(coreJS, moduleJS);

            var fqResourceJS = [];
            for (var i = 0; i < resourceJS.length; i++) {
                fqResourceJS.push(sysConfig.site(resourceJS[i]));
            }

            var percent = (function() {
                var val = 0;
                return {
                    add: function(addVal) {
                        val += addVal;
                        document.getElementById("percent").setAttribute("style", "width: " + Math.floor(val) + "%");
                    }
                }
            })();

            yepnope({
                load: fqResourceJS,
                callback: function(url, result, key) {
                    percent.add(100 / resourceJS.length);
                },
                complete: function() {
                    var uiView = document.createElement("div"),
                        percent = document.getElementById("percent"),
                        wrap = percent.parentNode,
                        master = wrap.parentNode;

                    uiView.setAttribute("ui-view", "");
                    master.replaceChild(uiView, wrap);
                    completeCb();

                }
            });
        }

    })();




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
