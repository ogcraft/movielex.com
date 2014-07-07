angular.module('MovieFish')
        .controller('ModalCtrl', function($scope, $ionicScrollDelegate, FileModule) {

            FileModule._init_fs(function(dirs, err) {
                console.log("dirs init")
                console.log(dirs)
                $scope.dirs = dirs;
            });
            $scope.goto = function(dir) {
                FileModule.ls(dir, function(data) {
                    $scope.dirs = data;
                    $scope.current_dir = FileModule.current_dir;
                    $ionicScrollDelegate.scrollTop(true);
                    $scope.$apply();
                });
            };

            $scope.go_back = function() {
                FileModule.get_parent(function(parent_dir) {
                    FileModule.ls(parent_dir, function(data) {
                        $scope.dirs = data;
                        $scope.current_dir = FileModule.current_dir;
                        $scope.$apply();
                        $ionicScrollDelegate.scrollTop(true);
                    });
                });
            };
            $scope.create_dir = function() {
                FileModule.create_dir(function() {
                    FileModule.ls($scope.current_dir, function(data){
                        $scope.dirs = data;
                        $scope.current_dir = FileModule.current_dir;
                        $ionicScrollDelegate.scrollTop(true);
                        $scope.$apply();
                    });
                });

            };
            
            $scope.change_dir = function(){
                FileModule.change_dir();
                $scope.modal.hide();
            };

        });
