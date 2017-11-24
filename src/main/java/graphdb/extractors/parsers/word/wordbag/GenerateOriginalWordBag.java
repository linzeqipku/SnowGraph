package graphdb.extractors.parsers.word.wordbag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import graphdb.extractors.parsers.word.entity.utils.StyleType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;


class GenerateOriginalWordBag {

	public static void main(String args[]) {
		String projectPath = "C:/csp-copy-all/东方红配置库/01工作库/03设计/02详细设计/02平台服务/authservice";
		dealWithFiles(new File(projectPath));
		AutoWordBagInstance.record();
	}
	
	private static void dealWithFiles(File file){
		if (!file.exists())
			return;
		if (file.isFile()){
			dealWithFile(file);
			return;
		}
		File[] files=file.listFiles();
		for (File f:files)
			dealWithFiles(f);
	}

	private static void dealWithFile(File file) {
		if(file == null){
			return;
		}
		
		//just handle DOCX file
		if(file.getName().startsWith("~")||!file.getName().endsWith(".docx")){
			//do nothing for non-DOCX files
			return;
		}
		
		XWPFDocument doc = null;
		
		try {
			InputStream in = new FileInputStream(file);
			doc = new XWPFDocument(in);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		if(doc == null){
			return;
		}
		
		//获得文档的样式
		XWPFStyles xwpfStyleMap = doc.getStyles();
		
		Iterator<IBodyElement> elementIter = doc.getBodyElementsIterator();
		while(elementIter.hasNext()){
			IBodyElement element = elementIter.next();
			
			if(element instanceof XWPFParagraph){
				XWPFParagraph paragraph = (XWPFParagraph)element;
				String style = paragraph.getStyle();
				String styleType = null;
				
				XWPFStyle xwpfStyle = xwpfStyleMap.getStyle(style);
				
				if(xwpfStyle != null){
					styleType = xwpfStyle.getName();	
				}
				
				//just handle heading
				if(StyleType.isHeading(styleType)){
					String heading = paragraph.getText();
					AutoWordBagInstance.tWordBag.add(heading);
					System.out.println(heading);
				}
			}else if(element instanceof XWPFTable){
				XWPFTable table = (XWPFTable)element;
				List<XWPFTableRow> rowList = table.getRows();
				
				for(XWPFTableRow xwpfTableRow:rowList){
					List<XWPFTableCell> cellList = xwpfTableRow.getTableCells();
					for(XWPFTableCell cell:cellList){
						String cellContent = cell.getText();
						if(!cellContent.trim().isEmpty()){
							AutoWordBagInstance.cWordBag.add(cellContent);
							System.out.println("cellContent:" + cellContent);
						}
					}
				}
			}
		}
	}
}
