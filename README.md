# Software Knowledge Graph

SnowGraph is tool for data analytics, knowledge mining and question answering in software development, maintenance and reuse activities.

* Data analytics

  * From multi-source and heterogeneous software engineering data (e.g., source code, version control, documentation, mailing list, issue tracker and online forum) to a uniform graph
    
    * Entities: classes, methods, commits, document sections, users, emails, issue reports, forum posts, ...
    
    * Traces (relations): inheritance, method_invocation, have_document_section, code_element_mentioned_in, ...
    
  * Graph query language and graphical browsing interface
  
  * High extendability for various data types

* Knowledge mining

  * Extract knowledge from the graph
  
    * API usage examples, API semantic representations, business topics, domain ontologies, ...
    
  * High extendability for various knowledge mining algorithms

* Question answering

  * Graph-based question answering

    * Question example: "Which developer answered the most emails about class RAMDirectory?"
  
  * Text-based question answering

    * Find passages from the graph to answer questions
    
    * Question example: "How to get all field names in an IndexReader?"
