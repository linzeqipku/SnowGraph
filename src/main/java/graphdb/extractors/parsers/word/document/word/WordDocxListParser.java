package graphdb.extractors.parsers.word.document.word;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import graphdb.extractors.parsers.word.entity.utils.DocumentElementInfo;
import graphdb.extractors.parsers.word.entity.word.WordItemListInfo;
import graphdb.extractors.parsers.word.entity.word.WordDocumentInfo;
import graphdb.extractors.parsers.word.entity.word.WordListItemInfo;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class WordDocxListParser {

    HashMap<String,List<XWPFParagraph>> map= new HashMap<>();
    HashMap<XWPFParagraph,String> index= new HashMap<>();

    public void addPara(XWPFParagraph para){
        BigInteger numID=para.getNumID();
        if (numID!=null){
            if (!map.containsKey(""+numID.intValue()))
                map.put(""+numID.intValue(), new ArrayList<>());
            map.get(""+numID.intValue()).add(para);
            index.put(para, ""+numID.intValue());
        }
    }

    public void refactor(WordDocumentInfo doc){
        for (DocumentElementInfo e:doc.getSubElements())
            refactor(e);
    }

    public void refactor(DocumentElementInfo element){
        if (element instanceof WordItemListInfo){
            for (DocumentElementInfo e:element.getSubElements())
                refactor(e);
            return;
        }

        List<DocumentElementInfo> r= new ArrayList<>();
        int p=0;
        WordItemListInfo listInfo=null;
        XWPFParagraph para=null;
        while (p<element.getSubElements().size()){
            DocumentElementInfo e=element.getSubElements().get(p);
            if (listInfo==null){
                if (!(e instanceof WordListItemInfo)){
                    r.add(e);
                }
                else{
                    listInfo=new WordItemListInfo();
                    para=((WordListItemInfo)e).getPara();
                    listInfo.getSubElements().add(e);
                    r.add(listInfo);
                }
            }
            else {
                boolean isLast=false;
                String numId=index.get(para);
                if (numId!=null&&para==map.get(numId).get(map.get(numId).size()-1))
                    isLast=true;
                if (!(e instanceof WordListItemInfo)){
                    listInfo.getSubElements().get(listInfo.getSubElements().size()-1).getSubElements().add(e);
                    if (isLast){
                        r.add(e);
                        listInfo=null;
                        para=null;
                    }
                }
                else{
                    String eNumId=((WordListItemInfo)e).getNumId();
                    if (eNumId.equals(numId)){
                        listInfo.addSubDocumentElement(e);
                        para=((WordListItemInfo)e).getPara();
                    }
                    else{
                        listInfo.getSubElements().get(listInfo.getSubElements().size()-1).getSubElements().add(e);
                    }
                }
            }
            p++;
        }
        element.getSubElements().clear();
        element.getSubElements().addAll(r);

        for (DocumentElementInfo e:element.getSubElements())
            refactor(e);

    }

}
