angular.module('MovieFish')
.controller('SettingsCtrl', function($scope, $ionicModal, LanguageService, LocalStorageAdapter, NotificationModule, FileModule) {
    $scope.languages = LanguageService.getLanguageList();
    $scope.myLanguage = LanguageService.getCurrentLanguage();
    $scope.changeLanguage = function(lang) {
        LanguageService.changeLanguage(lang);
    };
    $scope.refreshMovieList = function(){
        NotificationModule.confirm(LanguageService.translate("Are you sure?"), function(res){
            if(res){
                var rememberCurrentLang = LanguageService.getCurrentLanguage();
                LocalStorageAdapter.clear();
                LanguageService.changeLanguage(rememberCurrentLang);
            }
        });
    };
    $scope.modal = $ionicModal.fromTemplateUrl('modal.html', function(modal) {
        $scope.modal = modal;
    }, {
        animation: 'slide-in-up'
    });
       
    $scope.deviceType = function(){
            $scope.android = true;
          if(navigator.userAgent.match(/(iPad|iPhone|iPod)/g) ){
              $scope.android = false;
          }
        
    };
    $scope.deviceType();
    
    
    
   $scope.exit = function()
            {
                navigator.notification.confirm(
            'Do you really want to exit?',  // message
            $scope.exitFromApp,              // callback to invoke with index of button pressed
            'Exit',            // title
            'Cancel,OK'         // buttonLabels
        );
            };
    
    $scope.exitFromApp = function(buttonIndex)
    {
         if (buttonIndex===2){
       navigator.app.exitApp();
   }
    };
 
 

    
    
});