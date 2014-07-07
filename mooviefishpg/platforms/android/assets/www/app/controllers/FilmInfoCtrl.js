angular.module('MoovieFish').controller('FilmInfoCtrl',
function($scope, $stateParams, FilmService, TranslationService, LocalStorageAdapter, $ionicSideMenuDelegate, $timeout) {
    $ionicSideMenuDelegate.canDragContent(true);
    FilmService.getOneFilm($stateParams.id, function(data) {
        $scope.film = data;
        $scope.film.translations.forEach(function(tr, i){
            $scope.film.translations[i][ TranslationService.checkStatus($stateParams.id+"_"+tr.lang) ] = true;
        });
    });
    $scope.download = function(tr) {
        tr.play = false;
        $timeout(function(){ tr.download = false; });
       
        $timeout(function(){tr.downloading = true;},300);
        
        TranslationService.download(tr.file, $stateParams.id+"_"+tr.lang, function(file_path) {
            LocalStorageAdapter.set($stateParams.id+"_"+tr.lang, file_path);
            
             $timeout(function(){ tr.downloading = false; });
             $timeout(function(){tr.play = true;},300);
            $scope.$apply();
        });
    };
});
