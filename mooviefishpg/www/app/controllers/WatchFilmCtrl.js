angular.module('MovieFish').controller('WatchFilmCtrl',
function($scope, $stateParams, FilmService, LocalStorageAdapter, API, DeviceModule, MediaModule, $ionicSideMenuDelegate, $interval) {
    $ionicSideMenuDelegate.canDragContent(false);
    /* get data to render page */
    FilmService.getOneFilm($stateParams.id, function(data) {
        $scope.film = data;
        $scope.translation = data.translations[$stateParams.lang];
    });
    $scope.fakepos = {
        position: 0
    };
    /* load audio file from localStorage and get media object with file Duration and all the stuff */
    MediaModule.init(LocalStorageAdapter.get($stateParams.id + "_" + $stateParams.lang), function(mediaObj){
        $scope.Media = mediaObj;
        $scope.interval = $interval(function(){
            $scope.fakepos.position = mediaObj.position;
        }, 1000);
    });
    /*
     * transalate second to normal view
     * TRIGGERED from view
     */
    $scope.toHHMMSS = function(duration) {
        var 
        hours = Math.floor(duration / 3600),
        minutes = Math.floor((duration - (hours * 3600)) / 60),
        seconds = duration - (hours * 3600) - (minutes * 60);
        if (hours < 10)hours = "0" + hours;
        if (minutes < 10)minutes = "0" + minutes;
        if (seconds < 10)seconds = "0" + seconds;
        return (hours + ':' + minutes + ':' + seconds);
    };
    $scope.rangeChanged = function(value) {//triggered when we manua,lly move the range
        $scope.Media.seekTo(parseInt(value, 10));
    };
    /* RANGE methods END */

    /* PLAYER START */
    $scope.play = function(){
        API("play", {
            id: $stateParams.id,
            uuid: DeviceModule.uuid()
        }, function(result){
            if(result.error)return alert(result.error.message);
            if(!result.permission || result.permission === false || result.permission === "false")return alert("Permission denied");
            $scope.Media.play();
        });
    };
    $scope.pause = function(){
        $
        $scope.Media.pause();
    };
    $scope.stop = function(){
        $scope.Media.stop();
    };
    /* PLAYER END */
});