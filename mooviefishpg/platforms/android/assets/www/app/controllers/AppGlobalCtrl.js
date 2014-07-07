angular.module('MoovieFish')
.controller('AppGlobalCtrl', function($scope, $rootScope, $ionicSideMenuDelegate, LanguageService) {
    $scope.toggleLeft = function(){$ionicSideMenuDelegate.toggleLeft();};
    $rootScope.lang = LanguageService.translate;
});
