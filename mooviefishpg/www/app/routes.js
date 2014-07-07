angular.module('MovieFish')
        .config(function($stateProvider, $urlRouterProvider) {
            $stateProvider
                    .state('menu', {
                        url: "/menu",
                        abstract: true,
                        templateUrl: "app/views/sidebar.html"
                    })
                    .state('menu.main', {
                        url: "/main",
                        views: {
                            menuContent: {
                                templateUrl: "app/views/main.html",
                                controller: "MainCtrl"
                            }
                        }
                    })
                    .state('menu.filminfo', {
                        url: "/filminfo/:id",
                        views: {
                            menuContent: {
                                templateUrl: "app/views/filminfo.html",
                                controller: "FilmInfoCtrl"
                            }
                        }

                    })
                    .state('menu.watchfilm', {
                        url: "/watchfilm/:id/:lang",
                        views: {
                            menuContent: {
                                templateUrl: "app/views/watchfilm.html",
                                controller: "WatchFilmCtrl"
                            }
                        }
                    })
                    .state('menu.settings', {
                        url: "/settings",
                        views: {
                            menuContent: {
                                templateUrl: "app/views/settings.html",
                                controller: "SettingsCtrl"
                            }
                        }
                    });
            $urlRouterProvider.otherwise("menu/main");
        });
