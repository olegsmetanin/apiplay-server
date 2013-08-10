angular.module('home')
    .config(['$routeProvider', '$locationProvider', '$stateProvider', '$urlRouterProvider', 'sysConfig',
        function($routeProvider, $locationProvider, $stateProvider, $urlRouterProvider, sysConfig) {

            var projectsList = {
                name: 'page2C.projectList',
                url: '/projects/listview',
                views: {
                    'sidebar': {
                        templateUrl: sysConfig.srcPrefix+ 'home/projects/listview/projectsListFilter.tpl.html'
                    },
                    'content': {
                        templateUrl: sysConfig.srcPrefix+ 'home/projects/listview/projectsListGrid.tpl.html'
                    }
                }
            };

            $stateProvider
                .state(projectsList);

        }
    ])
    .service("projectsService", ['$q','$http','sysConfig',
        function($q, $http, sysConfig) {
            this.getProjects = function(filter) {
                var deferred = $q.defer();
                 $http.post("/api/v1", {
                    action: "get",
                    model: "projects",
                    filter: filter
                }).success(function (data, status, headers, config) {
                     deferred.resolve(data);
                }).error(function (data, status, headers, config) {
                    // TODO
                });
                return deferred.promise;
            };
        }
    ])
    .controller('projectsListGridCtrl', ['$scope', 'projectsService', 'pageConfig', 'sysConfig',
        function($scope, $projectsService, $pageConfig, sysConfig) {
            $pageConfig.setConfig({
                breadcrumbs: [{
                    name: 'Projects',
                    url: '/#!/projects/listview'
                }]
            });
            $scope.projects = [];
            $projectsService.getProjects({}).then(function (res) {
                $scope.projects = res.projects;
             });

            $scope.templatesConfig = function(projectId) {
                if (projectId && projectId.indexOf('play') >= 0) {
                    return sysConfig.srcPrefix+'home/projects/listview/details/playProjectDetails.tpl.html';
                } else {
                    return sysConfig.srcPrefix+'home/projects/listview/details/otherProjectDetails.tpl.html';
                }
            };
            $scope.projectDetailsTemplate = '';

            $scope.showDetails = function(projectId) {
                $scope.selectedProjectId = projectId;
                $scope.projectDetailsTemplate = $scope.templatesConfig(projectId);
            };
        }
    ]);