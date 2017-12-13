import graphdb.extractors.linkers.apimention.ApiMentionExtractor
import graphdb.extractors.linkers.ref.ReferenceExtractor
import graphdb.extractors.miners.codeembedding.line.LINEExtractor
import graphdb.extractors.miners.text.TextExtractor
import graphdb.extractors.parsers.git.GitExtractor
import graphdb.extractors.parsers.javacode.JavaCodeExtractor
import graphdb.extractors.parsers.jira.JiraExtractor
import graphdb.extractors.parsers.mail.MailListExtractor
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor
import graphdb.framework.Extractor
import utils.GraphUtil

fun snow_graph(block:SnowGraphBuilder.()->Unit): SnowGraphBuilder {
    var graph=SnowGraphBuilder()
    graph.block()
    return graph
}

class SnowGraphBuilder {
    var input:String? = null
    var output:String? = null
    internal var extractors:MutableList<Extractor> = mutableListOf<Extractor>()
    var java:String? = null
            set(value){
                var extractor=JavaCodeExtractor()
                extractor.setSrcPath(value)
                extractors.add(extractor)
            }
    var git:String? = null
        set(value){
            var extractor=GitExtractor()
            extractor.setGitFolderPath(value)
            extractors.add(extractor)
        }
    var mbox:String? = null
            set(value){
                var extractor=MailListExtractor()
                extractor.setMboxPath(value)
                extractors.add(extractor)
            }
    var jira:String? = null
            set(value){
                var extractor=JiraExtractor()
                extractor.setIssueFolderPath(value)
                extractors.add(extractor)
            }
    var stackoverflow:String? = null
            set(value){
                var extractor=StackOverflowExtractor()
                extractor.setFolderPath(value)
                extractors.add(extractor)
            }
    var code_graph_embedding:Boolean? = null
            set(value) { if (value==true) {extractors.add(LINEExtractor())} }
    var text_extraction:Boolean? = null
        set(value) { if (value==true) {extractors.add(TextExtractor())} }
    var reference_trace:Boolean? = null
        set(value) { if (value==true) {extractors.add(ReferenceExtractor())} }
    var mention_trace:Boolean? = null
        set(value) { if (value==true) {extractors.add(ApiMentionExtractor())} }
    fun build(){
        GraphUtil.buildGraph(input,output,extractors)
    }
}

fun main(args: Array<String>) {
    var snowGraph = snow_graph {
        //input = "E:/SnowGraphData/lucene/graphdb-base"
        output = "E:/SnowGraphData/lucene/graphdb-kotlin"
        java = "E:/SnowGraphData/lucene/sourcecode"
        git = "E:/SnowGraphData/lucene/git"
        mbox = "E:/SnowGraphData/lucene/mbox"
        jira = "E:/SnowGraphData/lucene/jira"
        stackoverflow = "E:/SnowGraphData/lucene/stackoverflow"
        code_graph_embedding = true
        text_extraction = true
        reference_trace = true
        mention_trace = true
    }
    snowGraph.build()
}