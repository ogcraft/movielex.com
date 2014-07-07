angular.module('MovieFish')
.factory('API', function($http, LocalStorageAdapter, API_CONFIG, API_ROUTES) {
        
    return function(method, params, callback) {
        if(!API_ROUTES[method])return false;
        if(arguments.length === 2 && typeof(params) === "function")var callback = params, params = {};
        /* get params fromconfig*/
        var 
        _REQUEST_PRMS = clone_object(API_CONFIG.REQUEST_PRMS),
        API_PRMS = API_ROUTES[method],
        cache_key = method;
        _REQUEST_PRMS.url = API_CONFIG.REQUEST_URL + API_PRMS.url;
        _REQUEST_PRMS.method = API_PRMS.method;
        
        /* set passed params*/
        for(var i in params){
            _REQUEST_PRMS.url = _REQUEST_PRMS.url.replace( new RegExp("\/\:"+i, "g"), ("/"+params[i]) );
            cache_key+=params[i];
        }
        $http(_REQUEST_PRMS)
        .success(function(data){
            callback(data);
            LocalStorageAdapter.set(cache_key, data);
        })
        .error(function(data, status, headers, config) {
            console.info("no internet connection or server error. Cashed data provided");
            console.info("server log below:");
            console.info("data");
            console.log(data);
            console.info("status");
            console.log(status);
            console.info("headers");
            console.log(headers);
            console.info("config");
            console.log(config);
            LocalStorageAdapter.get(cache_key)
                ? callback( LocalStorageAdapter.get(cache_key) )
                : callback({
                    error:{
                        code: 404,
                        message: "connection failed"
                    }   
                });
                
        });
    };

});