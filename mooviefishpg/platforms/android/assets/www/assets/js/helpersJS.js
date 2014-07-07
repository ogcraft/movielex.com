function strfield_to_array(field, data){
    if(is_array(data)){
        var _data = [];
        data.forEach(function(v, k){
            var f_split = [];
            if(is_array(field))
                field.forEach(function(iv){
                    f_split.push(v[iv].split(","));
                });
            else
                f_split = v[field].split(",");
            _data.push(clone_object(v, field, f_split)); // images string to array
        });
        return _data;
    }else{
        var f_split = [];
        if(is_array(field))
            field.forEach(function(iv){
                if(empty(data[iv]))
                    f_split.push([]);
                else
                    f_split.push(data[iv].split(","));
            });
        else
            f_split = data[field].split(",");
        return clone_object(data, field, f_split);
    }
};

function objectKeyValue(k,v){
    var obj = {};
    obj[k] = v;
    return obj;
}

function clone_object(o, field_name, replace_with){
    var oo = {};
    for(var i in o){
        oo[i] = o[i];

        if(arguments.length === 3)
            if(is_array(field_name)){
                if(field_name.indexOf(i) >= 0){
                    oo[i] = trim_ar_strings(replace_with[field_name.indexOf(i)]);
                }
            }else{
                oo[field_name] = trim_ar_strings(replace_with);
            }
    }
    return oo;
}

function clone_array(o){
    var oo = [];
    for(var i in o)
        oo[i] = o[i];
    return oo;
}

function trim_ar_strings(ar){
    for(var i = 0; i< ar.length; ++i)
        if(is_array(ar[i]))
            for(var k = 0; k < ar[i].length; ++k)
                ar[i][k] = ar[i][k].replace(/^\s+|\s+$/g, "");
        else        
            ar[i] = ar[i].replace(/^\s+|\s+$/g, "");
    return ar;
}

String.prototype.trim = function(){
    return this.replace(/^\s+|\s+$/g, "");
};

Array.range = function(a, b, step){
    var A= [];
    if(typeof a == 'number'){
        A[0]= a;
        step= step || 1;
        while(a+step<= b){
            A[A.length]= a+= step;
        }
    }
    else{
        var s= 'abcdefghijklmnopqrstuvwxyz';
        if(a=== a.toUpperCase()){
            b=b.toUpperCase();
            s= s.toUpperCase();
        }
        s= s.substring(s.indexOf(a), s.indexOf(b)+ 1);
        A= s.split('');        
    }
    return A;
};

//function clone_object(o, field_name, replace_with){
//    var oo = {};
//    for(var i in o)
//        oo[i] = o[i];
//    if(arguments.length === 3)
//        oo[field_name] = trim_ar_strings(replace_with);
//    return oo;
//}

function clone_object_array_fields_to_str(o, field_name){
    var oo = {}, field = "";
    for(var i in o){
        oo[i] = o[i];
        if(arguments.length === 2 && is_array(o[i])){
            o[i].forEach(function(arEl){
                if(typeof(arEl) === "object")
                    for(var k in arEl)
                        if(k === field_name)
                            field+= (field === "" ? arEl[k] : (","+arEl[k]));
            });
            oo[i] = field.trim();
            field = "";
        }
    }
    return oo;
}

function filter_fields(data, fields, add){
    if(!is_array(fields) || arguments.length < 2)return false;
    if(is_array(data)){
        var result = [];
        for(var i=0; i<data.length; ++i){
            result.push(filter_object_fields(data[i],fields, add));
        }
        return result;
    }else return filter_object_fields(data, fields, add);
}

function filter_object_fields(data, fields, add){
    if(!is_array(fields) || arguments.length < 2)return false;
    for(var i in data){
        if(fields.indexOf(i) === -1)
            delete data[i];
        if(is_set(add))
            data[Object.keys(add)[0]] = add[Object.keys(add)[0]];
    }
    return data;
}

function is_set(){ //many arguments
    try {
        for(var i = 0; i<arguments.length; ++i)
            if(arguments[i] === false || arguments[i] === null || typeof(arguments[i]) === "undefined" || arguments[i] === "")
                return false;
        return true;
    } catch(e) {
        return false;
    }
}

function objectLenght(obj){
    try{
        return Object.keys(obj).length;
    }catch(e){
        return false;
    }
}

function empty(){
    try {
        if(arguments.length === 0)return true;
        for(var i = 0; i<arguments.length; ++i)
            if(!is_set(arguments[i]))
                return true;
            else
                if(is_array(arguments[i]) && arguments[i].length === 0)
                    return true;
                else if(typeof(arguments[i]) === "object" && Object.keys(arguments[i]).length === 0)
                    return true;
                else if(typeof(arguments[i]) === "string" && arguments[i] === "")
                    return true;
                else if(typeof(arguments[i]) === "number" && arguments[i] === "")
                    return true;
        return false;
    } catch(e) {
        return true;
    }
}

function is_array(ar){
    return (ar instanceof Array);
}

function group_objects(){
    var result = {};
    for(var i = 0; i<arguments.length; ++i)
        for(var k in arguments[i])
            result[k] = arguments[i][k];
    console.log(result);
    return result;
}