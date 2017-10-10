/**
 * 
 */

var neo4jd3;
var relationset = [];


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
