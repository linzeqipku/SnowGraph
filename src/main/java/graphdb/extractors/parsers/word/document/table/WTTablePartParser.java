package graphdb.extractors.parsers.word.document.table;

import graphdb.extractors.parsers.word.entity.table.*;
import graphdb.extractors.parsers.word.entity.utils.DocumentElementInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class WTTablePartParser {
    private static final int	TABLE_NONE		= 0;
    private static final int	TABLE_TITLE		= 1;
    private static final int	TABLE_HEADING	= 2;
    private static final int	TABLE_CONTENT	= 3;

    public static List<TablePartInfo> parseTableToParts(TableInfo tableInfo) {
        // 要产生的完整表格的分部列表
        List<TablePartInfo> tableParts = new ArrayList<>();

        // 原始表格的行列表
        List<DocumentElementInfo> rowInfos = tableInfo.getSubElements();
        // 实际上只在content分支用一次，而且可以通过行下标访问，所以取消这个变量
        // int prevColSize = -1;
        int state = TABLE_NONE;
        TableSubTableInfo curSubTable = null;

        // 遍历各行
        for (int rowIter = 0; rowIter < rowInfos.size(); rowIter++) {
            TableRowInfo curRowInfo = (TableRowInfo) rowInfos.get(rowIter);

            List<DocumentElementInfo> cellList = curRowInfo.getSubElements();
            int cellSize = cellList.size();
            String signature = WTRowSignature.getSignature(curRowInfo);

            switch (state) {
                case TABLE_NONE:
                case TABLE_TITLE:
                    // 很多title和none状态下的处理方式是一样的，具体的再根据实际情况加if-else来判断，因为大部分逻辑一样所以不分开
                {
                    // 再按照单行列数进行分类讨论
                    if (cellSize == 1) {
                        if (state == TABLE_TITLE) {
                            // 连续两个单格行，第一个单格作为子表title，第二个单格作为一个record。
                            checkHeading(curSubTable, cellSize, tableParts);

                            TableRecordInfo curRecordInfo = generateRecordInfo(curRowInfo, curSubTable);
                            curSubTable.getSubElements().add(curRecordInfo);
                            state = TABLE_CONTENT;

                            // 旧方案：连续两个单格行，第一个单格作为单纯的内容插入全表，第二个等待后续表格内容，预期作为title
                            // TablePlainCellInfo oldTitleCell = new
                            // TablePlainCellInfo(
                            // curSubTable.getHorizontalTitle());
                            // tableParts.add(oldTitleCell);
                        }
                        else {
                            // 普通的单格行，title情况下也要把当前单格行如此炮制
                            curSubTable = new TableSubTableInfo();
                            curSubTable.setHorizontalTitle((TableCellInfo) cellList.get(0));
                            state = TABLE_TITLE;
                        }
                        break;
                    }
                    else if (cellSize == 2) {
                        // 单行两个单元格，很可能是key-value的形式
                        // 先获取两个单元格，检查值是key-value（pair），还是key-key（heading），还是value-value（record）,或者奇怪的value-key（plaincell）
                        TableCellInfo firstCell = (TableCellInfo) (cellList.get(0));
                        TableCellInfo secondCell = (TableCellInfo) (cellList.get(1));
                        if (firstCell.isVMerged()) {
                            // 首列跨行
                            if (state == TABLE_NONE)
                                curSubTable = new TableSubTableInfo();
                            // !!!导致bug，如果跨行只在heading内部，是不设为vertical title的。
                            // curSubTable.setVerticalTitle(firstCell);

                            if (signature.endsWith(WTRowSignature.KEY)) {
                                // 次列为key
                                curSubTable.getHeadingRows().add(curRowInfo);
                                state = TABLE_HEADING;
                                break;
                            }
                            else {
                                // 次列为value，直接开始records
                                checkHeading(curSubTable, cellSize, tableParts);

                                TableRecordInfo curRecordInfo = generateRecordInfo(curRowInfo,
                                        curSubTable);
                                curSubTable.getSubElements().add(curRecordInfo);
                                state = TABLE_CONTENT;
                                break;
                            }
                        }
                        else if (secondCell.isVMerged()) {
                            // 次列跨行,直接开始逐行记录
                            if (state == TABLE_NONE)
                                curSubTable = new TableSubTableInfo();

                            checkHeading(curSubTable, cellSize, tableParts);

                            TableRecordInfo curRecordInfo = generateRecordInfo(curRowInfo, curSubTable);
                            curSubTable.getSubElements().add(curRecordInfo);
                            state = TABLE_CONTENT;
                            break;
                        }
                        else {
                            // 没有跨行merged
                            if (signature.equals(WTRowSignature.KEY + WTRowSignature.VALUE)) {
                                // key-value
                                TableKeyValuePairInfo curPairInfo = new TableKeyValuePairInfo(
                                        new TablePropertyKeyInfo(firstCell), secondCell);
                                if (state == TABLE_NONE) {// 单独的k-v对直接存入完整表格下属
                                    tableParts.add(curPairInfo);
                                    state = TABLE_NONE;
                                }
                                else if (state == TABLE_TITLE) {// 有title格的情况下，k-v对作为子表的一条记录
                                    curSubTable.getSubElements().add(curPairInfo);
                                    state = TABLE_CONTENT;
                                }
                                break;
                            }
                            else if (signature.equals(WTRowSignature.KEY + WTRowSignature.KEY)) {
                                // key-key
                                if (state == TABLE_NONE)
                                    curSubTable = new TableSubTableInfo();
                                curSubTable.getHeadingRows().add(curRowInfo);
                                state = TABLE_HEADING;
                                break;
                            }
                            else {
                                // value-?
                                if (state == TABLE_TITLE) {
                                    // 新设计：单格行之后，作为record
                                    checkHeading(curSubTable, cellSize, tableParts);

                                    TableRecordInfo curRecordInfo = generateRecordInfo(curRowInfo,
                                            curSubTable);
                                    curSubTable.getSubElements().add(curRecordInfo);
                                    // 旧设计：单格行之后的v-？，每个单元格插在子表记录中
                                    // curSubTable.getSubElements().add(new
                                    // TablePlainCellInfo(firstCell));
                                    // curSubTable.getSubElements()
                                    // .add(new TablePlainCellInfo(secondCell));
                                    state = TABLE_CONTENT;
                                }
                                else if (state == TABLE_NONE) {
                                    // 每个单元格独立摘出来归并在表格下
                                    tableParts.add(new TablePlainCellInfo(firstCell));
                                    tableParts.add(new TablePlainCellInfo(secondCell));
                                    state = TABLE_NONE;
                                }
                                break;
                            }
                        }
                    }
                    else if (cellSize >= 3) {
                        // 多列的情形
                        TableCellInfo firstCell = (TableCellInfo) (cellList.get(0));
                        if (WTRowSignature.keyDominateRow(curRowInfo)) {
                            if (firstCell.isVMerged())
                                ;// ?如果首列跨行是在heading结束时处理和判断是否vertical，那么这里应该没必要处理的。
                            if (state == TABLE_NONE)
                                curSubTable = new TableSubTableInfo();
                            curSubTable.getHeadingRows().add(curRowInfo);
                            state = TABLE_HEADING;
                            break;
                        }
                        else if (WTRowSignature.valueDominateRow(curRowInfo)) {
                            // none/title状态下无key支配行，也就是表格没有标题行。
                            if (state == TABLE_NONE)
                                curSubTable = new TableSubTableInfo();

                            if (firstCell.isVMerged()) {
                                // 慎重设置纵向标题，有的时候它仅是多行同值，为了偷懒的情况。
                                // 而且如果一个表格内多次出现，只能设一个vtitle
                                if (curSubTable.getVerticalTitle() == null) {// 无纵向标题的，添加纵向标题
                                    // curSubTable.setVerticalTitle(firstCell);
                                }
                                else if (!curSubTable.getVerticalTitle().equals(firstCell)) {// 有纵向标题且与当前不一致的，跳出。
                                    // 事实上在none/title状态下不可能发生这件事。
                                }
                                else {
                                    // 有纵向标题且与当前一致，不管他。
                                }
                            }

                            checkHeading(curSubTable, cellSize, tableParts);
                            // 无论纵向标题如何，均需创建当前行的record，并将之存入当前子表的contents中
                            TableRecordInfo curRecordInfo = generateRecordInfo(curRowInfo, curSubTable);
                            curSubTable.getSubElements().add(curRecordInfo);
                            state = TABLE_CONTENT;
                            break;
                        }
                        else {
                            // key行，value行之外的“杂合”行?都算在value行？？？
                            // 倾向于取消这个分支，上一分支的else if改成else
                        }
                    }// end of if-else branches for cellSize
                    break;
                }// end of case TABLE_NONE + case TABLE_TITLE
                case TABLE_HEADING: {
                    if (WTRowSignature.keyDominateRow(curRowInfo)) {
                        curSubTable.getHeadingRows().add(curRowInfo);
                        state = TABLE_HEADING;// state实际上不变
                        break;
                    }
                    else if (WTRowSignature.valueDominateRow(curRowInfo)) {
                        // heading到此完结了，分析headingRow，并赋值heading。
                        parseHeading(curSubTable);

                        // 如果开始记录的时候，记录每行首格跨行，则作为vertical title
                        TableCellInfo firstCell = (TableCellInfo) (cellList.get(0));
                        if (firstCell.isVMerged()) {
                            if (curSubTable.getVerticalTitle() == null) {
                                // 慎重设置纵向标题，有的时候它仅是多行同值，为了偷懒的情况。
                                // 而且如果一个表格内多次出现，只能设一个vtitle
                                // curSubTable.setVerticalTitle(firstCell);
                            }
                            else if (!firstCell.equals(curSubTable.getVerticalTitle())) {
                                // impossible
                            }
                        }

                        checkHeading(curSubTable, cellSize, tableParts);

                        // 创建当前行的record，并将之存入当前子表的contents中
                        TableRecordInfo curRecordInfo = generateRecordInfo(curRowInfo, curSubTable);
                        curSubTable.getSubElements().add(curRecordInfo);
                        state = TABLE_CONTENT;
                        break;
                    }
                    break;
                }// end of case TABLE_HEADING
                case TABLE_CONTENT: {
                    // 子表格终止的几个条件：
                    // 1、首列跨行的情况下，跨行结束。（跨行格不一致/新格不跨行）
                    // 2、从值行变成了键行。
                    // 3、行的格式发生变化（a.单元格数目不一致了；b.单元格内容特点不一致了）

                    TableCellInfo firstCell = (TableCellInfo) (cellList.get(0));

                    // 新方案：如果不考虑首格跨行？
                    // if ((!firstCell.isVMerged() && curSubTable.getVerticalTitle()
                    // == null)/* 没有跨行的情况 */
                    // || (firstCell.isVMerged() && firstCell.equals(curSubTable
                    // .getVerticalTitle()))/* 首格跨行时，与当前表格一致 */) {
                    // // 跨行符合继续的条件
                    if (WTRowSignature.valueDominateRow(curRowInfo)) {
                        // 键值比例符合继续的条件
                        if (signature.equals(WTRowSignature.getSignature((TableRowInfo) rowInfos
                                .get(rowIter - 1))))
                            ;// 更强的一种：行的格式一致要求signature也完全一致，

                        // 如果前面的纵向跨行没有结束，或者当前行和前一行单元格数量一致，才认可表格继续
                        // previous columns实际只在此处应用一次
                        if ((curSubTable.getVerticalTitle() != null && curSubTable.getVerticalTitle()
                                .getEndRowNum() >= curRowInfo.getRowNum())
                                || cellSize == rowInfos.get(rowIter - 1).getSubElements().size()) {

                            checkHeading(curSubTable, cellSize, tableParts);

                            // 如果在读表格内容，那么遇到value行且行的格式（单元格数量）一致时，才继续分析下去
                            TableRecordInfo curRecordInfo = generateRecordInfo(curRowInfo, curSubTable);
                            curSubTable.getSubElements().add(curRecordInfo);
                            state = TABLE_CONTENT;
                            if (rowIter == rowInfos.size() - 1) // 表格结束
                                tableParts.add(curSubTable);
                            break;
                        }
                    }
                    // }
                    // 结束当前列表，且将当前行保留在处理流的下一个
                    tableParts.add(curSubTable);
                    curSubTable = null;
                    state = TABLE_NONE;
                    rowIter--;
                    break;
                }// end of case TABLE_CONTENT
            }// end of switch(state){...}

        } // end of for (DocumentElementInfo element : rowInfos) {...}

        if (curSubTable != null && !tableParts.contains(curSubTable))
            tableParts.add(curSubTable);

        return tableParts;
    }
    private static TableRecordInfo generateRecordInfo(TableRowInfo rowInfo,
                                                      TableSubTableInfo subTableInfo) {
        if (rowInfo == null)
            return null;
        TableRecordInfo recordInfo = new TableRecordInfo();

        List<DocumentElementInfo> cellList = rowInfo.getSubElements();
        TableCellInfo firstCell = (TableCellInfo) cellList.get(0);

        int startCursor = 0;
        // 合并的v_title很多时候也要添加进去！！！
        if (subTableInfo.getVerticalTitle() != null)
            startCursor = 1;// 首列是合并的vertical title的时候，从第二个单元格开始
        // 140730 最新方案，只有在标题行第一个单元格纵跨下来的时候，才有vertical title，所以忽略首行没问题

        if (subTableInfo == null || subTableInfo.getHeading() == null
                || subTableInfo.getHeading().getColumnHeads().size() <= 0) {
            // 没有表格信息，或者没有标题行，或者标题行为空，所以也就没有属性的名称（key），只有值。

            for (int i = startCursor; i < cellList.size(); i++) {
                TableCellInfo curCell = (TableCellInfo) cellList.get(i);
                // 没有键值时建一个空的key，但是不能为null
                TableKeyValuePairInfo pair = new TableKeyValuePairInfo(new TablePropertyKeyInfo(),
                        curCell);
                recordInfo.addProperty(pair);
            }
        }
        else {
            TableHeadingInfo heading = subTableInfo.getHeading();
            if (cellList.size() <= 1 && heading.getColumnHeads().size() > 1) {
                // 单格行但是标题行不是单格，则不添加head（empty_head）
                TableKeyValuePairInfo pair = new TableKeyValuePairInfo(new TablePropertyKeyInfo(),
                        (TableCellInfo) cellList.get(0));
                recordInfo.addProperty(pair);
            }
            else {
                int startColNumOfHeads = heading.getColumnHeads().get(0).getStartColNum();
                int startCursorForHeads = cellList.indexOf(rowInfo
                        .getCellAtColumn(startColNumOfHeads));
                if (startCursorForHeads > startCursor)
                    startCursor = startCursorForHeads;// 取单元格第一列和标题行第一列中比较靠后的一个开始

                // 还是按照单元格来给key值
                // 策略1：单元格起始列对应的head，作为key值
                // 策略2：单元格全长覆盖的所有head，连接起来作为key值
                // 似乎策略1更符合情况
                for (int i = startCursor; i < cellList.size(); i++) {
                    TableCellInfo curCell = (TableCellInfo) cellList.get(i);

                    // 找出所有覆盖该cell的表头
                    List<TableColumnHeadInfo> overlappedHeads = heading
                            .getOverlappedColumnHeads(curCell);

                    if (overlappedHeads == null || overlappedHeads.size() == 0) {
                        // System.out.println(heading);
                        // System.out.println(cellList);
                        continue;
                    }

                    // 用第一个表头（策略1）创建一个属性键
                    TableColumnHeadInfo firstHead = overlappedHeads.get(0);

                    TablePropertyKeyInfo keyInfo = new TablePropertyKeyInfo();
                    keyInfo.setKeyCellInfos(firstHead.getHeadCells());
                    // 创建键值对
                    TableKeyValuePairInfo pair = new TableKeyValuePairInfo(keyInfo, curCell);
                    recordInfo.addProperty(pair);
                }
            }
        }

        return recordInfo;
    }
    // 分析subTable中的HeadingRows，解析出vertical title，headings等
    private static void parseHeading(TableSubTableInfo subTableInfo) {
        TableHeadingInfo headingInfo = new TableHeadingInfo();
        List<TableRowInfo> headingRows = subTableInfo.getHeadingRows();

        // 如果标题行最后一行的第一个单元格向下跨列，则建立为verticalTitle
        TableRowInfo lastRow = headingRows.get(headingRows.size() - 1);
        TableCellInfo firstCellOfLastRow = lastRow.getCellAtColumn(1);
        if (firstCellOfLastRow.isVMerged()
                && firstCellOfLastRow.getEndRowNum() > lastRow.getRowNum()) {
            subTableInfo.setVerticalTitle(firstCellOfLastRow);
        }

        List<Integer> startColNumOfHeads = new ArrayList<>();// 记录每个表头的开始位置，划分到最细粒度。
        for (int i = headingRows.size() - 1; i >= 0; i--) {
            // 从后向前遍历所有行
            TableRowInfo curRow = headingRows.get(i);
            for (DocumentElementInfo cellEle : curRow.getSubElements()) {
                int startColNum = ((TableCellInfo) cellEle).getStartColNum();
                if (!startColNumOfHeads.contains(startColNum))
                    startColNumOfHeads.add(startColNum);
            }
        }
        // 从小到大排序
        Collections.sort(startColNumOfHeads);

        // 根据表头划分粒度创建表头列表
        for (int k = 0; k < startColNumOfHeads.size(); k++) {
            // 每个表头分割处理一次
            int startColNum = startColNumOfHeads.get(k);
            int endColNum = (k + 1 == startColNumOfHeads.size()) ? lastRow.getColumnSize()
                    : (startColNumOfHeads.get(k + 1) - 1);// 如果是最后一个表头，设为行尾值

            TableColumnHeadInfo columnHeadInfo = new TableColumnHeadInfo();
            columnHeadInfo.setStartColNum(startColNum);
            columnHeadInfo.setEndColNum(endColNum);

            for (int i = 0; i < headingRows.size(); i++) {
                // 从第一行到最后一行进行处理，这样添加到head的cell也是按行的先后顺序，产生headString的顺序也会是对的。
                TableRowInfo curRow = headingRows.get(i);
                TableCellInfo curCell = curRow.getCellAtColumn(startColNum);
                if (!columnHeadInfo.getHeadCells().contains(curCell))
                    columnHeadInfo.getHeadCells().add(curCell);// 考虑到表头单元格跨行的情况，只在不存在新表头时候才添加。

            }
            // 建好的表头插入列表中
            headingInfo.getColumnHeads().add(columnHeadInfo);
        }
        // 将heading列表设置进表格属性。
        subTableInfo.setHeading(headingInfo);
    }

    // 如果当前子表格的heading为空，则查看紧邻的上一个tablePart是否是subTable且有非空的heading
    private static void checkHeading(TableSubTableInfo curSubTable, int curTableColumnSize,
                                     List<TablePartInfo> tableParts) {
        if (curSubTable.getHeading() == null || curSubTable.getHeading().isEmptyHeading()) {
            if (tableParts.size() > 0) {
                TablePartInfo lastTablePart = tableParts.get(tableParts.size() - 1);
                if (lastTablePart instanceof TableSubTableInfo) {
                    TableSubTableInfo lastSubTableInfo = (TableSubTableInfo) lastTablePart;
                    if (lastSubTableInfo.getHeading() != null
                            && !lastSubTableInfo.getHeading().isEmptyHeading()
					/*
					 * 紧邻上一个子表有heading，且单元格数量一样？ &&
					 * lastSubTableInfo.getHeading().getColumnHeads().size() ==
					 * curTableColumnSize
					 */) {
                        curSubTable.setHeading(lastSubTableInfo.getHeading());
                    }
                }
            }
        }
    }

}
