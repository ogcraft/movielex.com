angular.module('MediaModule', [])
.factory('MediaModule', function() {

    var 
    PLAYING_MEDIA_PATH = null,
    stopOtherPlayingFile = function(path){
        if(PLAYING_MEDIA_PATH !== path){
            if(AllMedia[PLAYING_MEDIA_PATH])
                AllMedia[PLAYING_MEDIA_PATH].stop();
            PLAYING_MEDIA_PATH = path;
        }
    },
    AllMedia = {},
    
    MediaTrack = function(path){
        var 
        _self = this,
        playerInterval;       
        this.duration = 0;
        this.position = 0;
        this.path = path;
        this.mediaObj = null;
        this.getDuration = function(callback){
            var counter = 0;
            if(_self.mediaObj)var normal_duration = _self.mediaObj.getDuration();
            if(normal_duration && normal_duration > 0)
                callback(Math.ceil(normal_duration));
            else{
               _self.mediaObj.play();_self.mediaObj.stop();
                var timerDur = setInterval(function() {
                    counter = counter + 100;
                    if (counter > 2000)clearInterval(timerDur);
                    var dur = _self.mediaObj.getDuration();
                    if (dur > 0) {
                        callback(Math.ceil(dur));
                        clearInterval(timerDur);
                    }
                }, 100);
            }
        };
        this.init = function(callback){console.log('init');
            if(_self.mediaObj !== null)_self.mediaObj.stop();
            _self.mediaObj = new Media(_self.path, function(){
                console.log('new Media callback');
            }, function(error) {
                callback({success: false, error: true});
                alert('code: ' + error.code + '\n' +
                        'message: ' + error.message + '\n');
            });
            _self.getDuration(function(dur){
                _self.duration = dur;
                callback({
                    success: true,
                    duration: dur
                });
            });
        };
        this.seekTo = function(time, callback) { // time need to * 1000
            if(_self.mediaObj !== null){
                _self.mediaObj.seekTo(time * 1000, callback);
                _self.position = time;
            }
        };
        this.seekToAndPlay = function(time) {
            var _self = this;
            this.seekTo(time, function() {
                _self.play();
            });
        };
        this.play = function() {
            stopOtherPlayingFile(_self.path);
            if(_self.mediaObj){
                _self.mediaObj.play({playAudioWhenScreenIsLocked: true});
                _self.changeStatus("play");
            }
        };
        this.pause = function() {
            if(_self.mediaObj){
                _self.mediaObj.pause();
                _self.changeStatus("pause");
            }
        };
        this.stop = function() {
            if(_self.mediaObj){
                _self.mediaObj.stop();
                _self.changeStatus("stop");
            }
        };
        this.throwError = function(msg){
            console.log(msg);
            return false;
        };  
        this.status = null;
        this.changeStatus = function(status){
            switch(status){
                case "play":
                    playerInterval = setInterval(function(){
                        _self.position++;
                    },1000);
                    break;
                case "stop":
                    _self.position = 0;//and do the following step 'pause' ==> clearInterval\
                    clearInterval(playerInterval);
                case "pause":
                    clearInterval(playerInterval);
                    break;
            }
            _self.status = status;
        };
    };

    return {
        init: function(url, callback){
            if(!(url && callback))return false;
            var _url = url.replace(/^file:\/\/\//, "");
            if(AllMedia[_url])
                callback(AllMedia[_url]);
            else{
                var media = new MediaTrack(_url);
                media.init(function(){
                    AllMedia[_url] = media;
                    callback(media);
                });
            }
        }
    };
    
});