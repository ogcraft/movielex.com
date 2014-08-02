angular.module('MoovieFish')
        .factory('TranslationService', function(FS_CONFIG, LocalStorageAdapter) {

            var
                    loadings = {},
                    FS = null,
                    initFS = function(callback) {
                        FS = FS_CONFIG.MEDIA_DIR;
                        callback(FS);
                    };

            return {
                /*
                 * statuses:
                 * 2 ===> download
                 * 1 ===> downloading
                 * 0 ===> any
                 * 
                 */
                checkStatus: function(key) {
                    if (LocalStorageAdapter.get(key))
                        return "download";
                    if (loadings[key])
                        return "downloading";
                    return "any";
                },
                download: function(url, key, callback) {
                    initFS(function(fs) {
                        var
                                fileTransfer = new FileTransfer(),
                                _urlrs = url.split("/"),
                                filename = _urlrs[_urlrs.length - 1];

                        loadings[key] = url; // mark current download as downloading

                        fileTransfer.download(
                                encodeURI(url),
                                encodeURI(fs) + filename,
                                function(theFile) {
                                    delete loadings[key];
                                    callback(theFile.nativeURL);
                                },
                                function(error) {
                                    delete loadings[key];
                                }, true);
                    });
                }
            };
        });