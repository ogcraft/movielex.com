angular.module('FileModule', [])
        .service('FileModule', function(FS_CONFIG) {
            var _self = this;
            this.current_dir;
            this._init_fs = function(callback) {
                window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function(fs) {
                    _self.ls(fs.root, callback);
                }, function(e) {
                    console.log("error");
                    console.log(e);
                });
            };
            this.ls = function(dir, callback) {
                var directoryReader = dir.createReader(),
                        dirs = [];
                    _self.current_dir = dir;
                    directoryReader.readEntries(function(entries) {
                        entries.forEach(function(v) {
                            if (v.isDirectory === true) {
                                dirs.push(v);
                            }
                        });
                        callback(dirs);

                    }, function(e) {
                        console.log("error");
                        console.log(e);
                    });
            };
            this.get_parent = function(callback){
                _self.current_dir.getParent(function(parent_dir) {
                    callback(parent_dir)
                });
            };

            this.create_dir = function(callback){
                _self.current_dir.getDirectory("MovieFish", {create: true, exclusive: false}, callback);
            };
            
            this.change_dir = function(){
                FS_CONFIG.MEDIA_DIR = _self.current_dir.nativeURL +'/';
            };
        });
