angular.module('FileModule', [])
        .service('FileModule', function(FS_CONFIG) {
            var _self = this;
            this.current_dir;
            this._init_fs = function(callback) {
                console.log("_init_fs called");
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
                    console.log("_self.current_dir: " + dir);
                    directoryReader.readEntries(function(entries) {
                        entries.forEach(function(v) {
                            if (v.isDirectory === true) {
                                console.log("dirs.push: " + v);
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
                _self.current_dir.getDirectory("MoovieFish", {create: true, exclusive: false}, callback);
            };
            
            this.change_dir = function(){
                console.log("_self.current_dir.nativeURL: " + _self.current_dir.nativeURL);
                FS_CONFIG.MEDIA_DIR = _self.current_dir.nativeURL +'/';
            };
        });
