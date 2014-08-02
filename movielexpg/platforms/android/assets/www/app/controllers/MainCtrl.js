angular.module('MoovieFish')
.controller('MainCtrl', function($scope, MAINPAGE_CONFIG, FilmService, LanguageService, LocalStorageAdapter,$ionicSideMenuDelegate) {
    $ionicSideMenuDelegate.canDragContent(true);
    $scope.cropDescr = function(descr) {
        return descr.length < MAINPAGE_CONFIG.DESCRIPTION_LENGTH
            ? (descr.substring(0, MAINPAGE_CONFIG.DESCRIPTION_LENGTH) + "...")
            : descr.substring(0, MAINPAGE_CONFIG.DESCRIPTION_LENGTH);
    };
    $scope.lang_checked = LanguageService.getCurrentLanguage();
    FilmService.getFilms(function(callback) {
        $scope.films = callback;
    });
    $scope.doRefresh = function() {
        FilmService.getFilms(function(callback) {
            $scope.films = callback;
        });
        $scope.$broadcast('scroll.refreshComplete');
        $scope.$apply();
    };
});