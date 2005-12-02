package org.apache.idaeplugin.frames.table;

import org.apache.idaeplugin.bean.OprationObj;

import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.Iterator;
/*
* Copyright 2004,2005 The Apache Software Foundation.
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
*
*
*/

/**
 * Author: Deepal Jayasinghe
 * Date: Sep 22, 2005
 * Time: 11:57:24 AM
 */
public class ArchiveTableModel extends AbstractTableModel {

    final String[] columnNames = {"Operation Name", "Return Value", "Paramters ", "Select"};
    Object [][] datvalue;
    private HashMap datobjs;

    public ArchiveTableModel(HashMap dataobjetc) {
        int size = dataobjetc.size();
        datvalue = new Object[size][4];
        Iterator itr =  dataobjetc.values().iterator();
        int count =0;
        while (itr.hasNext()) {
            OprationObj oprationObj = (OprationObj) itr.next();
            datvalue[count][0]=oprationObj.getOpName();
            datvalue[count][1]=oprationObj.getReturnVale();
            datvalue[count][2]=oprationObj.getParamters();
            datvalue[count][3]=oprationObj.getSelect();
            count ++;
        }
        this.datobjs = dataobjetc;
    }
    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return datvalue.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return datvalue[row][col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return col >= 3;
    }
    public void setValueAt(Object value, int row, int col) {
        OprationObj obj = (OprationObj)datobjs.get(getValueAt(row,0));
        if(col == 3){
            obj.setSelect((Boolean)value);
        }

        if (datvalue[0][col] instanceof Integer) {
            try {
                datvalue[row][col] = new Integer((String)value);
            } catch (NumberFormatException e) {
                System.out.println("Error");
            }
        } else {
            datvalue[row][col] = value;
        }
//        obj.printMe();
    }
}



