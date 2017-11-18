function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function outgoing(id){
    $.ajax({
        type: 'POST',
        url: "OutGoingRelation",
        data: {id : id},
        success: function (data) {
            return data;
        }
    });
}

function getNode(id){
    $.ajax({
        type: 'POST',
        url: "GetNode",
        data: {id : id},
        success: function (data) {
            alert(data);
            return data;
        }
    });
}

/**
 * Created by Administrator on 2017/5/16.
 */
