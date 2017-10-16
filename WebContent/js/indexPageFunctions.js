/**
 *
 */

var neo4jd3;
var dataset = [];
var relationset = [];
var edgelist = [];
var classesJson;
var relationshipsJson;
var ou_relationshipsJson;
var in_relationshipsJson;
var codePropertyCnName ;
var docPropertyCnName;
var labelCnName ;
function keyLogin(){
    if(event.keyCode == 13 && document.getElementById("search").val() != "") {
        document.getElementById("search").click();
    }
}

function getdata(node){
    var node_ID = node;
    for (var i = 0; i < dataset.length; i++) if (dataset[i].metadata.id == node) return i;
    $.ajax({
        type: 'POST',
        url: "GetNode",
        data: {id: node_ID},
        dataType: 'json',
        async: false,
        success: function (data) {
            dataset.push(data);
        }
    });
    $.ajax({
        type: 'POST',
        url: "OutGoingRelation",
        data: {id: node_ID},
        dataType: 'json',
        async: false,
        success: function (data) {
            relationset.push(data);
        }
    });
    getcataandnums(node_ID);
    return dataset.length - 1;
}
function getcataandnums(node_ID){
    var index = getdata(node_ID);
    var list = relationset[index];
    var edge = [];
    for (var i = 0; i < list.length; i++){
        if (i == 0){
            edge.push({nums : 1, begin : 0, end : 0, cata : list[i].type});
        }else{
            if (list[i].type == list[i-1].type){
                edge[edge.length-1].nums ++;
                //edge[edge.length-1].end ++;
            }else {
                edge.push({nums : 1, begin : edge[edge.length-1].end + edge[edge.length-1].nums, end : edge[edge.length-1].end + edge[edge.length-1].nums, cata : list[i].type});
            }
        }
    }
    edgelist.push(edge);
}
function getd3GraphById(node_ID,type){
    var index = getdata(node_ID);
    var list = relationset[index];
    var obj = dataset[index];
    var json = {nodes : [], relationships : []};
    json.nodes.push({id : obj.metadata.id, labels : obj.metadata.labels, properties : obj.data});
    var edgeindex = 0;
    for (var i = 0; i < edgelist[index].length; i++) if (edgelist[index][i].cata == type){
        edgeindex = i; break;
    }
    var starte = edgelist[index][i].end;
    var ende = edgelist[index][edgeindex].end + 5;
    if (ende > edgelist[index][edgeindex].begin + edgelist[index][edgeindex].nums)
        ende = edgelist[index][edgeindex].begin + edgelist[index][edgeindex].nums;
    edgelist[index][edgeindex].end = ende;
    for (var i = starte; i < ende; i++) {
        var ele = list[i];
        var endid = parseInt(ele.end.substr(ele.end.lastIndexOf("/")+1));
        var startid = parseInt(ele.start.substr(ele.start.lastIndexOf("/")+1));
        json.relationships.push({
            id: ele.metadata.id, type: ele.type.substr(3), startNode: startid,
            endNode: endid, properties: {}
        });
        var otherid = endid;
        if (otherid == node_ID) otherid = startid;
        var tmpindex = getdata(otherid);
        json.nodes.push({id : dataset[tmpindex].metadata.id, labels : dataset[tmpindex].metadata.labels, properties : dataset[tmpindex].data});
        for (var j = 0; j < relationset[tmpindex].length; j++) {
            var ele = relationset[tmpindex][j];
            var endid = parseInt(ele.end.substr(ele.end.lastIndexOf("/")+1));
            var startid = parseInt(ele.start.substr(ele.start.lastIndexOf("/")+1));
            json.relationships.push({
                id: ele.metadata.id, type: ele.type.substr(3), startNode: startid,
                endNode: endid, properties: {}
            });
        }
    }
    var jsonson = {graph : json};
    var jsonfa = {data : [jsonson]};
    var ret = {results : [jsonfa]};
    return ret;
}

function getd3Graph(obj){
    var json = {nodes: [], relationships: []};
    for (var i = 0; i < obj.nodes.length; i++) {
        json.nodes.push({id: obj.nodes[i].metadata.id, labels: obj.nodes[i].metadata.labels, properties: obj.nodes[i].data});
    }
    for (var i = 0; i < obj.relationships.length; i++) {
        ele = obj.relationships[i];
        var endid = parseInt(ele.end.substr(ele.end.lastIndexOf("/")+1));
        var startid = parseInt(ele.start.substr(ele.start.lastIndexOf("/")+1));
        json.relationships.push({id : ele.metadata.id, type : ele.type.substr(3), startNode : startid,
            endNode : endid, properties : {}});
    }
    var jsonson = {graph: json};
    var jsonfa = {data: [jsonson]};
    var ret = {results: [jsonfa]};
    return ret;
}

function anotherInit(oriJson , node_ID){
    var current = oriJson.index;
    var max = oriJson.max;

    init(oriJson.searchResult , node_ID);
}

function init(orijson,node_ID) {

    dataset = [];
    relationset = [];
    edgelist = [];

    $.ajax({  
        type : "get",  
        url : "data/translation.json",  
        dataType: 'json',
        async : false,  
        success : function(data){  
        	classesJson = data.classes;
        	relationshipsJson = data.relationships;
        	in_relationshipsJson = data.in_relationships;
        	ou_relationshipsJson = data.ou_relationships;
        	codePropertyCnName = data.codePropertyCnName;
        	docPropertyCnName = data.docPropertyCnName;
        	labelCnName = data.labelCnName;
        }  
        });  

    neo4jd3 = new Neo4jd3('#neo4jd3', {
        showClassChnName : false,
        classes: classesJson,
        showRlationshipsChnName: false,
        relationships: relationshipsJson,
        highlight: [
            {
                class: 'Project',
                property: 'name',
                value: 'neo4jd3'
            }, {
                class: 'User',
                property: 'userId',
                value: 'eisman'
            }
        ],
        //region
        icons: {
//                        'Address': 'home',
            'Api': 'gear',
            'Method': '',
            'Field': '',
            'Class': '',
            'Interface': '',
//                        'BirthDate': 'birthday-cake',
            'Cookie': 'paw',
//                        'CreditCard': 'credit-card',
//                        'Device': 'laptop',
            'Email': 'at',
            'Git': 'git',
            'Github': 'github',
            'gitCommit': 'github',
            'Google': 'google',
//                        'icons': 'font-awesome',
            'Ip': 'map-marker',
            'Issues': 'exclamation-circle',
            'Language': 'language',
            'Options': 'sliders',
            'Password': 'lock',
            'Phone': 'phone',
            'Project': 'folder-open',
            'SecurityChallengeAnswer': 'commenting',
            'User': '',
            'MailUser' : '',
            'IssueUser' : '',
            'StackOverflowUser' : '',
            'gitCommitAuthor': '',
            "mutatedFile": "file-o",
            'zoomFit': 'arrows-alt',
            'zoomIn': 'search-plus',
            'zoomOut': 'search-minus'
        },
        minCollision: 60,
        neo4jData: orijson,
        nodeRadius: 40,
        focusid : node_ID,
        onNodeDoubleClick: function(node) {

            if (node.id == node_ID){
                var checkText=$("#relationSelect").find("option:selected").val();
                node_ID = node.id;
                var ret = getd3GraphById(node_ID,checkText);
                neo4jd3.focusid = node_ID;
                neo4jd3.updateWithNeo4jData(ret);
            }else{
                node_ID = node.id;
                var index = getdata(node.id);
                var list = relationset[index];
                var obj = dataset[index];
                view(obj, list);
                relationsNumsDisplay(node_ID)
            }
        },
        onNodeClick : function(d){
        },
        onRelationshipDoubleClick: function(relationship) {
            console.log('double click on relationship: ' + JSON.stringify(relationship));
        },
        zoomFit: false,
        infoPanel : false,
        lineColor:"black",//"#E6E6FA", // LightBlue
        lineWidth:"3",
        relationTextColor:"black",
        relationTextFontSize:"15px",
        //nodeOutlineFillColor:"black",
        nodeFillColor:"#F0F8FF" // ALICEBLUE
    });
}

function relationsNumsDisplay(node_ID){
    var index = getdata(node_ID);
    var list = relationset[index];
    var cata = [];
    var nums = [];
    for (var i = 0; i < list.length; i++){
        if (i == 0){
            cata.push(list[i].type);
            nums.push(1);
        }else{
            if (list[i].type == list[i-1].type){
                nums[nums.length-1] ++;
            }else {
                cata.push(list[i].type);
                nums.push(1);
            }
        }
    }
    //chartsplay(cata,nums,node_ID);
    listsplay(node_ID);
}

function sendcypher(query){
    var list;
    $.ajax({
        type: 'POST',
        url: "CypherQuery",
        dataType: "json",
        data: query,
        async: false,
        success: function(response) {list = response;}
    });
    if(!list.searchResult) {
        alert("No API SubGraph Found!");
    }else{
        anotherInit(list , 10000);
    }
}
function queryByText(){
    var query = $("#queryText").val();
    if ((query.indexOf("match") > 0 || query.indexOf("MATCH") > 0) &&
        (query.indexOf("where") > 0 || query.indexOf("WHERE") > 0) &&
        (query.indexOf("return") > 0 || query.indexOf("RETURN") > 0)){
        queryByChpher();
        return;
    }
    var flag = true;
    for (var i = 0; i < query.length; i++){
        if (query[i] > '9' || query[i] < '0') {
            flag = false;
            break;
        }
    }
    queryByName();
}
// query by type and name
function queryByName() {
    var node_name = $("#queryText").val();
    var thing = {"query":node_name};
    sendcypher(thing);
}

function hasRelations(node_ID,type){
    var ret = getd3GraphById(node_ID,function(obj){
        return obj.type == type;
    });
    init(ret, node_ID);
}
function addRelations(type,node_ID){
    var ret = getd3GraphById(node_ID,type);
    neo4jd3.focusid = node_ID;
    relationsNumsDisplay(node_ID);
    neo4jd3.updateWithNeo4jData(ret);
}

Array.prototype.contains = function(obj) {
    var i = this.length;
    while (i--) {
        if (this[i] == obj) {
            return true;
        }
    }
    return false;
}

function rename(str){
	if (relationshipsJson[str] && relationshipsJson[str]["englishName"]) return relationshipsJson[str].englishName;
	if (in_relationshipsJson[str] && in_relationshipsJson[str]["englishName"]) return in_relationshipsJson[str].englishName;
	if (ou_relationshipsJson[str] && ou_relationshipsJson[str]["englishName"]) return ou_relationshipsJson[str].englishName;
	if (str.substring(0,3) == "in_") return str.substring(3)+"(incoming)";
	if (str.substring(0,3) == "ou_") return str.substring(3)+"(outgoing)";
	return str;
    //return str + "rename";
}
//based on
function view(obj,list){
    $("#otherinfo").empty();
    // for (var i = 0; i < otherInfoTypes.length; i++){
    //    if (otherInfoTypes[i].types.contains(obj.metadata.labels[0])){
    //        $("#otherinfo").append("<tr> <td class = title>&nbsp;&nbsp;"+otherInfoTypes[i].show_name+
    //            ":</td> <td>" + "<button type=\"button\" class=\"btn btn-default\" onclick=\""+otherInfoTypes[i].name+"("+obj.metadata.id+");\">Submit</button>" +"</td> </tr>")
    //    }
    // }
    $("#relation").empty();
    for (var i = 0; i < list.length; i++){
        var ele = list[i];
        if (ele.type == "haveSoAnswer"){
            $("#relation").append("<tr> <td class = title>&nbsp;&nbsp;"+"haveSoAnswer"+
                ":</td> <td>" + ele.end +"</td> </tr>");
        }
    }
    for (var i = 0; i < list.length; i++){
        var ele = list[i];
        if (ele.type == "soAuthor"){
            $("#relation").append("<tr> <td class = title>&nbsp;&nbsp;"+"soAuthor"+
                ":</td> <td>" + ele.start +"</td> </tr>");
        }
    }

    var label = obj.metadata.labels[0]
    var name = obj.data["name"]
    if (name == null)
        name = obj.metadata.id
    $("#data").empty();
    $("#data").append("<tr> <td class = 'title'>&nbsp;&nbsp;Type:</td> <td>"
        + label + "</td> </tr>");
    if (label == "Class" || label == "Interface" || label == "Method" || label == "Field"){
        showProperty(obj, codePropertyCnName)
    } else {
        showProperty(obj, docPropertyCnName)
    }
}

function showProperty(obj, propertyCnName){
    for (key in propertyCnName){
        if (obj.data.hasOwnProperty(key)){
            var content = obj.data[key];
            /*if (key == "content" || key == "comment")*/
                content = "<pre>" + content + "</pre>";
            $("#data").append("<tr> <td class = 'title'>&nbsp;&nbsp;"+key+
                ":</td> <td>" + content +"</td> </tr>");
        }
    }
    for (key in obj.data){
    	if (!propertyCnName.hasOwnProperty(key)){
    		var content = obj.data[key];
                content = "<pre>" + content + "</pre>";
            $("#data").append("<tr> <td class = 'title'>&nbsp;&nbsp;"+key+
                ":</td> <td>" + content +"</td> </tr>");
    	}
    }
}


function listsplay(node_ID){
    $("#relationTypes").empty();
    $("#relationSelect").empty();
    var index = getdata(node_ID);
    for (var i = 0; i < edgelist[index].length; i++){
        var edgeele = edgelist[index][i];
        //$(" #relationTypes").append("<tr> <td class = title>&nbsp;&nbsp;"+edgeele.cata+
        //    ":</td> <td>" + "<button type=\"button\" class=\"btn btn-default\" onclick=\"addRelations(\'"+edgeele.cata+"\',"+node_ID+");\">Submit</button>" +"</td> </tr>");
        $("#relationSelect").append("<option value=\""+edgeele.cata+"\">"+rename(edgeele.cata)+"</option>");
    }
}
function getGraph(element){
    var list;
    var parameters={
        "type":"getGraph",
        index: element.value
    }
    $.ajax('CypherQuery', {
    	dataType : 'json',
    	contentType : 'application/json',
    	type : 'POST',
    	data : parameters
    }).then(function(response) {
    	list = response;
    });
    $("#relationSelect").empty();
    $("#relationSelect").append("<option>NONE</option>");

    $("#data").empty();
    anotherInit(list , 19);
}