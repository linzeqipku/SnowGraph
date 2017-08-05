/**
 * 
 */

var neo4jd3;
var dataset = [];
var relationset = [];
var edgelist = [];
var propertyCnName = {"access": "访问修饰符", "superClass": "父类", "implements":"实现接口", "name": "名称",
	"fullName": "全名",  "extends": "父接口", "isAbstract": "是否抽象类(abstract)", "isFinal": "是否不可变(final)",
	"isStatic": "是否静态", "comment": "注释", "content": "内容"};
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
            async: false,
            success: function (data) {
                dataset.push(data);
            }
        });
        $.ajax({
            type: 'POST',
            url: "OutGoingRelation",
            data: {id: node_ID},
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
        var endid = parseInt(ele.end.substr(35));
        var startid = parseInt(ele.start.substr(35));
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
            var endid = parseInt(ele.end.substr(35));
            var startid = parseInt(ele.start.substr(35));
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
        var endid = parseInt(ele.end.substr(35));
        var startid = parseInt(ele.start.substr(35));
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
	
	document.getElementById("first").value = 0;
	document.getElementById("last").value = max - 1;
	document.getElementById("former").value = current - 1 > -1 ? current - 1 : 0;
	document.getElementById("latter").value = current + 1 < max ? current + 1 : max - 1;
	init(oriJson.searchResult , node_ID);
} 

function init(orijson,node_ID) {
	console.log("asd");
	dataset = [];
	relationset = [];
	edgelist = [];
	neo4jd3 = new Neo4jd3('#neo4jd3', {	
		showClassChnName : false,
		classes: {
			"Class":{
				'englishName':"Class",
				"chnName":"类",
				"nodeFillColor":"#68bdf6" // light blue
			},
			"Method":{
				'englishName':"Method",
				'chnName':"方法",
				"nodeFillColor":'#6dce9e' // green #1
			},
			'Interface':{
				'englishName':"Interface",
				'chnName':"接口",
				"nodeFillColor":'#faafc2' // light pink
			},
			'Field':{
				'englishName':"Field",
				'chnName':"域",
				"nodeFillColor":'#f2baf6' // purple
			},
			'DocxFile':{
				'englishName':"DocxFile",
				'chnName':"docx文件",
				"nodeFillColor":'#ff928c' // light red
			},
			'DocxPlainText':{
				'englishName':"DocxPlainText",
				'chnName':"docx文本",
				"nodeFillColor":'#fcea7e' // light yellow
			},
			'DocxSection':{
				'englishName':"DocxSection",
				'chnName':"docx章节",
				"nodeFillColor":'#ffc766' // light orange
			},
			'DocxTable':{
				'englishName':"DocxTable",
				'chnName':"docx表格",
				"nodeFillColor":'#405f9e' // navy blue
			}
			
		},
		showRlationshipsChnName: false,
		relationships:{
			"api_explained_by":{
				"englishName":"api_explained_by",
				"chnName":"设计文档 / 本文档对应的代码元素"
			},
			"call_field":{
				"englishName":"call_field",
				"chnName":"call_field未定义"
			},
			"call_method":{
				"englishName":"call_method",
				"chnName":"本方法调用的方法 / 调用本方法的方法"
			},
			"extend":{
				"englishName":"extend",
				"chnName":"子类  / 父类"
			},
			"function_designed_by":{
				"englishName":"function_designed_by",
				"chnName":"该用户设计的方法  / 设计该方法的人"
			},
			"hava_field":{
				"englishName":"hava_field",
				"chnName":"本方法拥有的域  / 拥有该域的方法"
			},
			"have_method":{
				"englishName":"have_method",
				"chnName":"本类拥有的方法  / 拥有本方法的类"
			},
			"have_sub_element":{
				"englishName":"have_sub_element",
				"chnName":"需求文档 / 设计文档"
			},
			"implement":{
				"englishName":"implement",
				"chnName":"实现的接口  / 实现的接口的类"
			},
			"param":{
				"englishName":"param",
				"chnName":"参数  / 以本类型为参数的方法"
			},
			"rt":{
				"englishName":"rt",
				"chnName":"返回类型  / 以本类型为返回类型的方法"
			},
			"throw":{
				"englishName":"throw",
				"chnName":"抛出异常  / 抛出本异常的方法"
			},
			"type":{
				"englishName":"type",
				"chnName":"域的类型  / 定义本类型的方法"
			},
			"variable":{
				"englishName":"variable",
				"chnName":"引用  / 引用本类型的方法"
			}
		
		},
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
		//endregion
		images: {
			'Address': 'img/twemoji/1f3e0.svg',
			'Api': 'img/twemoji/1f527.svg',
            'Method': 'img/twemoji/M.svg',
            'Field': 'img/twemoji/F.svg',
            'Class': 'img/twemoji/C.svg',
            'Interface': 'img/twemoji/I.svg',
			'BirthDate': 'img/twemoji/1f382.svg',
			'Cookie': 'img/twemoji/1f36a.svg',
			'CreditCard': 'img/twemoji/1f4b3.svg',
			'Device': 'img/twemoji/1f4bb.svg',
			'Mail': 'img/twemoji/2709.svg',
			'Git': 'img/twemoji/1f5c3.svg',
           // 'gitCommit' : 'img/twemoji/1f5c3.svg',
			'Github': 'img/twemoji/1f5c4.svg',
			'icons': 'img/twemoji/1f38f.svg',
			'Ip': 'img/twemoji/1f4cd.svg',
			'Issue': 'img/twemoji/1f4a9.svg',
            'Patch': 'img/twemoji/1f4a9.svg',
			'Language': 'img/twemoji/1f1f1-1f1f7.svg',
			'Options': 'img/twemoji/2699.svg',
			'Password': 'img/twemoji/1f511.svg',
//                        'Phone': 'img/twemoji/1f4de.svg',
			'Project': 'img/twemoji/2198.svg',
			'Project|name|neo4jd3': 'img/twemoji/2196.svg',
//                        'SecurityChallengeAnswer': 'img/twemoji/1f4ac.svg',
			'User': 'img/twemoji/1f600.svg',
			'MailUser' : 'img/twemoji/user.svg',
            'IssueUser' : 'img/twemoji/user.svg',
            'StackOverflowUser' : 'img/twemoji/user.svg',
            'gitCommitAuthor' : 'img/twemoji/user.svg',
            'StackOverflowQuestion' : 'img/twemoji/stackoverflow.svg',
            'StackOverflowAnswer' : 'img/twemoji/stackoverflow.svg',
            'StackOverflowComment' : 'img/twemoji/stackoverflow.svg'
//                        'zoomFit': 'img/twemoji/2194.svg',
//                        'zoomIn': 'img/twemoji/1f50d.svg',
//                        'zoomOut': 'img/twemoji/1f50e.svg'
		},
		minCollision: 60,
		neo4jData: orijson,
		nodeRadius: 40,
        focusid : node_ID,
        onNodeDoubleClick: function(node) {
        	console.log("dd");
		    if (node.id == node_ID){
            	var checkText=$("#relationSelect").find("option:selected").val();
            node_ID = node.id;
            var ret = getd3GraphById(node_ID,checkText);
            neo4jd3.focusid = node_ID;
            //relationsNumsDisplay(node_ID);
            neo4jd3.updateWithNeo4jData(ret);
            }else{
                node_ID = node.id;
                var index = getdata(node.id);
                var list = relationset[index];
                var obj = dataset[index];
                view(obj, list);
                relationsNumsDisplay(node_ID)
			}
//                    var index = getdata(node.id);
//                    var list = relationset[index];
//                    var obj = dataset[index];
//                    node_ID = node.id;
//                    view(obj, list);
//
//                    neo4jd3.focusid = node_ID;
//					relationsNumsDisplay(node_ID);
//				    var node_ID = node.id;
//				    var index = getdata(node_ID);
//
//                    var list = relationset[index];
//                    var obj = dataset[index];
//                    view(obj,list);
//
//                    var json = {nodes : [], relationships : []};
//                    json.nodes.push({id : obj.metadata.id, labels : obj.metadata.labels, properties : obj.data});
//                    for (var i = times[index] * 5; i < Math.min(times[index] * 5 + 5,list.length); i++) {
//                        var ele = list[i];
//                        var endid = parseInt(ele.end.substr(35));
//                        var startid = parseInt(ele.start.substr(35));
//                        json.relationships.push({
//                            id: ele.metadata.id, type: rename(ele.type.substr(3)), startNode: startid,
//                            endNode: endid, properties: {}
//                        });
//                        var otherid = endid;
//                        if (otherid == node_ID) otherid = startid;
//                        var tmpindex = getdata(otherid);
//                        json.nodes.push({id : dataset[tmpindex].metadata.id, labels : dataset[tmpindex].metadata.labels, properties : dataset[tmpindex].data});
//                        for (var j = 0; j < relationset[tmpindex].length; j++) {
//                            var ele = relationset[tmpindex][j];
//                            var endid = parseInt(ele.end.substr(35));
//                            var startid = parseInt(ele.start.substr(35));
//                            json.relationships.push({
//                                id: ele.metadata.id, type: rename(ele.type.substr(3)), startNode: startid,
//                                endNode: endid, properties: {}
//                            });
//                        }
//                    }
//                        times[index]++;
//                        var jsonson = {graph : json};
//                        var jsonfa = {data : [jsonson]};
//                        var ret = {results : [jsonfa]};
//                        neo4jd3.focusid = node_ID;
//                    	relationsNumsDisplay(node_ID);
//                        neo4jd3.updateWithNeo4jData(ret);
		},
        onNodeClick : function(d){
            //var index = getdata(d.id);
            //var list = relationset[index];
            //var obj = dataset[index];
            //view(obj, list);
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
// multi answer choose to change the all panel
function sendid(node_ID){
    var index = getdata(node_ID);
	var list = relationset[index];
	var obj = dataset[index];
	var json = {nodes: [], relationships: []};
	json.nodes.push({id: obj.metadata.id , labels: obj.metadata.labels , properties : obj.data});
	var relLength = list.length;
	for(var i = 0 ; i < relLength ; i++){
	    var relation = list[i];
	    var temp ;
	    var id_temp = relationset[0][i].end;
	    var node_id = id_temp.substr("http://127.0.0.1:7474/db/data/node/".length);
	    node_id = parseInt(node_id , 10);
	    $.ajax({
			type: 'Post',
			url: "GetNode",
			data:{id : node_id},
			async: false,
			success:function(data){
			    temp = data;
			}
			}
		)
		console.log(relation.metadata.type);
		json.nodes.push({id : temp.metadata.id , labels : temp.metadata.labels , properties:temp.data});
	    json.relationships.push({id : relation.metadata.id , type : relation.metadata.type , startNode : node_ID , endNode : node_id});
	}
	var jsonson = {graph: json};
	var jsonfa = {data: [jsonson]};
	var ret = {results: [jsonfa]};
    //relationsNumsDisplay(node_ID);
	console.log("etry");
	init(ret, node_ID);
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
        data: query,
        async: false,
        success: function (data) {
            list = data;
            //alert(project.name)
        }
    });
    anotherInit(list , 19);
    return list;
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
    var thing = {query: "MATCH (n) WHERE n.name = {nodeName} RETURN n LIMIT 10",
        params: node_name};
    var list = sendcypher(thing);
    //listshow(list);
}
function listshow(list){
    $("#ListSelect").empty();
    if (list.data.length > 0){
        for (var i = 0; i < list.data.length; i++){
            var content = list.data[i][0].metadata.id;
            content  = list.data[i][0].data.fullName;
            if (content == undefined) content = list.data[i][0].data.name;
            if (content == undefined) content = list.data[i][0].data.UUID;
            $("#ListSelect").append("<li><button type=\"button\" class=\"btn btn-default btn-block\"   onclick=\"sendid("+list.data[i][0].metadata.id+");\">" +
                content +"</button></li>");
        }
        sendid(list.data[0][0].metadata.id);
    }
}

function extendtree(node_ID){
    var obj;
    $.ajax({
        type: 'POST',
        url: "GetExtendsTree",
        data: {id: node_ID},
        async: false,
        success: function (data) {
            obj = data;
        }
    });
    var ret = getd3Graph(obj);
    init(ret, node_ID);
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

var otherInfoTypes = [
    {name : "extendtree", show_name : "Extends Tree", types : ["Class", "Interface"]},
    {name : "hasMethod", show_name : "Method Map", types : ["Class", "Interface"]},
    {name : "hasField", show_name : "Field Map", types : ["Class", "Interface"]},
	{name : "hasModifiedCode", show_name : "Modified Code", types : ["Class","Interface"]}];

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
	console.log("haha");
	if (str == "extend") return "父类 / 子类";
	if (str == "implement") return "实现的接口 / 实现本接口的类";
	if (str == "throw") return "抛出异常 / 抛出本异常的方法";
	if (str == "param") return "参数 / 以本类型为参数的方法";
	if (str == "rt") return "返回类型 / 以本类型为返回类型的方法";
	if (str == "have_method") return "声明方法 / 所属类型";
	if (str == "have_field") return "声明域 / 所属域";
	if (str == "call_method") return "本方法调用的方法 / 调用本方法的方法";
	if (str == "type") return "域的类型 / 定义为本类型的域";
	if (str == "variable") return "引用 / 引用本类型的方法";
	if (str == "have_sub_element") return "下级文档元素 / 上级文档元素";
	if (str == "api_explained_by") return "设计文档 / 本文档对应的代码元素";
	if (str == "have_sub_element") return "需求文档 / 设计文档";
	if (str.substr(3) == "extend" && str[0] == 'i') return "子类";
	if (str.substr(3) == "extend" && str[0] == 'o') return "父类 ";
	if (str.substr(3) == "implement" && str[0] == 'i') return "实现本接口的类";
	if (str.substr(3) == "implement" && str[0] == 'o') return "实现的接口";
	if (str.substr(3) == "throw" && str[0] == 'i') return "抛出本异常的方法";
	if (str.substr(3) == "throw" && str[0] == 'o') return "抛出异常";
	if (str.substr(3) == "param" && str[0] == 'i') return "以本类型为参数的方法";
	if (str.substr(3) == "param" && str[0] == 'o') return "参数";
	if (str.substr(3) == "rt" && str[0] == 'i') return "以本类型为返回类型的方法";
	if (str.substr(3) == "rt" && str[0] == 'o') return "返回类型";
	if (str.substr(3) == "have_method" && str[0] == 'i') return "所属类型";
	if (str.substr(3) == "have_method" && str[0] == 'o') return "声明方法";
	if (str.substr(3) == "have_field" && str[0] == 'i') return "所属域";
	if (str.substr(3) == "have_field" && str[0] == 'o') return "声明域";
	if (str.substr(3) == "call_method" && str[0] == 'i') return "调用本方法的方法";
	if (str.substr(3) == "call_method" && str[0] == 'o') return "本方法调用的方法";
	if (str.substr(3) == "type" && str[0] == 'i') return "定义为本类型的域";
	if (str.substr(3) == "type" && str[0] == 'o') return "域的类型";
	if (str.substr(3) == "variable" && str[0] == 'i') return "引用本类型的方法";
	if (str.substr(3) == "variable" && str[0] == 'o') return "引用";
	if (str.substr(3) == "have_sub_element" && str[0] == 'i') return "上级文档元素";
	if (str.substr(3) == "have_sub_element" && str[0] == 'o') return "下级文档元素";
	if (str.substr(3) == "api_explained_by" && str[0] == 'i') return "本文档对应的代码元素";
	if (str.substr(3) == "api_explained_by" && str[0] == 'o') return "设计文档";
	if (str.substr(3) == "have_sub_element" && str[0] == 'i') return "设计文档";
	if (str.substr(3) == "have_sub_element" && str[0] == 'o') return "需求文档";
	return str;
    //return str + "rename";
}
//based on
function view(obj,list){
    $("#otherinfo").empty();
    for (var i = 0; i < otherInfoTypes.length; i++){
        if (otherInfoTypes[i].types.contains(obj.metadata.labels[0])){
            $("#otherinfo").append("<tr> <td class = title>&nbsp;&nbsp;"+otherInfoTypes[i].show_name+
                ":</td> <td>" + "<button type=\"button\" class=\"btn btn-default\" onclick=\""+otherInfoTypes[i].name+"("+obj.metadata.id+");\">Submit</button>" +"</td> </tr>")
        }
	}
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
	var notIntrested = ["lineVec", "transVec", "userId", "ownerUserId"]
	var label = obj.metadata.labels[0]
	var name = obj.data["name"]
	if (name == null)
		name = obj.metadata.id
    $("#data").empty();
	$("#data").append("<tr> <td class = 'title' style='color: blue'>&nbsp;&nbsp;"+label+
		"</td> <td class = title>" + name +"</td> </tr>");
    for(key in obj.data) {
		if ($.inArray(key,notIntrested) != -1) continue;
        var content = obj.data[key];
        if (key == "content" || key == "comment") content = "<pre>" + content + "</pre>";
        $("#data").append("<tr> <td class = 'title'>&nbsp;&nbsp;"+rename(key)+
            ":</td> <td>" + content +"</td> </tr>");
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
	$.ajax({
		type:'Post',
		url:'CypherQuery',
		data:parameters,
		async:false,
		success:function(data){
			list = data;
		}
	});
	$("#relationSelect").empty();
	$("#relationSelect").append("<option>NONE</option>");
	
	$("#data").empty();
	anotherInit(list , 19);
}
//start();