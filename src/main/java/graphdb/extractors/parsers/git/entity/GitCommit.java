import java.util.regex.Matcher;
import java.util.regex.Pattern;
    private List<MutatedContent> mutatedContents = null;

    //region <getter>
    public List<MutatedContent> getMutatedContents(){
        return mutatedContents;
    }

    //endregion <getter>
            //System.out.print("parsing commit file meta info filed , commit file:" + commitFile.getAbsolutePath());
            //System.out.println(e.getMessage());

    private void addMutatedContent(MutatedContent content){
        if(mutatedContents != null){
            mutatedContents.add(content);
        }else{
            mutatedContents = new ArrayList<MutatedContent>();
            mutatedContents.add(content);
        }
    }

                        //System.out.print("parsing commit file mutated file filed , commit file UUID:" + this.UUID);
                                //System.out.println("there are some class of which the qualified name can not be found.");
                                //System.out.println("the absolute path is: " + file.getAbsolutePath() + " creater:"  + file.getCreaterUUID() +"   modifier:" + file.getModifierUUID() + "  deleter:" + file.getDeleterUUID());
                line = reader.readLine();
                Pattern pattern = Pattern.compile("@@ -([0-9]+),([0-9]+) +([0-9]+),([0-9]+) @@");
                Matcher matcher = pattern.matcher(line);

                if(matcher.find()){
                    MutatedContent content = new MutatedContent();

                    content.setType(file.getMutatedType());
                    content.setCommitUUID(this.UUID);
                    content.setFormerName(file.getFormerName());
                    content.setLatterName(file.getLatterName());

                    int formerStartLineNum = Integer.parseInt( matcher.group(1) );
                    int formerLines = Integer.parseInt( matcher.group(2) );
                    int latterStartLineNum = Integer.parseInt( matcher.group(3) );
                    int latterLines = Integer.parseInt( matcher.group(4));

                    content.setFormerStartLineNum(formerStartLineNum ) ;
                    content.setFormerLines( formerLines );
                    content.setLatterStartLineNum( latterStartLineNum );
                    content.setLatterLines( latterLines ) ;

                    String contentString = "";
                    while(formerLines > 0 || latterLines > 0){
                        line = reader.readLine();
                        if( line.startsWith( "+" ) ) latterLines -- ;
                        else if( line.startsWith( "-" ) ) formerLines --;
                        else{
                            formerLines --;
                            latterLines -- ;
                        }
                        contentString += (line + "\n");
                    }

                    content.setContent(contentString);
                    addMutatedContent(content);
                }


            //System.out.println("parsing commit file mutated file filed , commit file UUID:" + this.UUID);