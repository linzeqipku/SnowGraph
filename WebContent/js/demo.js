/**
 * Copyright 2015 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* global $ */
'use strict';
// jQuery nodes
var $serviceResults = $('.service--results'),
  $standardResults = $('.standard--results'),
  $template = $('.result--template'),
  $output = $('.output'),
  $query = $('#query');
function queryByTextInDemo(){
    $query.empty();
	rank(null,$("#queryText").val(),true);
}
function rank(repsonseRandom,query,flag){

	$('.error').hide();
	$output.hide();
	
	var queryJson = {"query":query};
    /*$query.text(query.query);*/
    $.ajax({
        type: 'POST',
        url: "Rank",
        data: queryJson,
        async: false,
        success: function(response) {
      if (!flag)$query.append(response.query2);
  	  queryByText();
      $standardResults.empty();
      var aid = -1;
      if (!flag) aid = response.answerId;
      response.solrResults.map(createResultNode.bind(null, $template, false, aid))
      .forEach(function(e){
        $standardResults.append(e);
      });

      // service results - solr + ranker
      $serviceResults.empty();
      response.rankedResults.map(createResultNode.bind(null, $template, true, aid))
      .forEach(function(e){
        $serviceResults.append(e);
      });

      $output.show();
    }
    });
    function createResultNode($template, showRanking, standId, result, index) {
      var node = $template.last().clone().show();

      node.find('.results--item-text').prepend(result.title);
      node.find('.results--item-details').append(result.body);
      if (standId == result.answerId){
    	  node.find('.results--item-text').css("background-color","#41D6C3"); 
      }

      // ranking result
      if (showRanking){
        var iconClass = 'results--increase-icon_UP icon-arrow_up';
        var rank = result.solrRank - index -1;
        if (rank < 0)
          iconClass = 'results--increase-icon_DOWN icon-arrow_down';
        node.find('.results--rank').text("No. "+(index+1));
        node.find('.results--increase-icon').addClass(iconClass);
        node.find('.results--increase-value').text(rank > 0 ? rank : Math.abs(1 + index - result.solrRank) );
        
      } else {
        node.find('.results--item-rank').remove();
      }
	  
      node.find('.results--item-score-bar').each(function(i, score) {
        if ((result.relevance - i - 1) >= 0)
          $(score).addClass('green');
      });

      var $moreInfo = node.find('.results--more-info');
      node.find('.results--see-more').click(function() {
        if ($moreInfo.css('display') === 'none')
          $moreInfo.fadeIn('slow');
        else
          $moreInfo.fadeOut(500);
      });
      return node;
    }
  }
function _error(xhr) {
  $('.error').show();
  var response = JSON.parse(xhr.responseText);
  console.log(response.error);
  $('.error h4').text(response.error || 'Internal server error.');
}


$(document).ready(function() {
  // namespace variables
  var queries = [];
  var query = null;



  $output.hide();
  $('#graphDisplayer').hide();
  $(".btn-group-graph").click(function(){
      $('#rankDisplayer').hide();
      $('#graphDisplayer').show();	  
  });
  $(".btn-group-rank").click(function(){
      $('#graphDisplayer').hide();
      $('#rankDisplayer').show();	  
  });  
    /*$(".mui-switch").change(function() {
        if ($(".mui-switch")[0].checked){
            $('#graphDisplayer').hide();
            $('#rankDisplayer').show();
        }else{

            $('#rankDisplayer').hide();
            $('#graphDisplayer').show();
        }
    });*/
  $.get('data/queries.json').then(function(data){
    queries = data.queries;
  }, _error);

  /**
   * Event handler for reset button
   */
  $('.reset-button').click(function() {
    location.reload();
  });
  $('.input--question-generator').click(function(){
	    $.ajax('Random', {
	    	contentType : 'application/json',
	    	type : 'POST'
	    }).then(function(response) {
	        $('#queryText').val(response.query);
	        $query.empty();
	        $query.append(response.query2);
	    	rank(response,response.query,false);
	    });
  });
  
});

