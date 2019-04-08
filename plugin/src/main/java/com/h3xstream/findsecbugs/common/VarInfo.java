package com.h3xstream.findsecbugs.common;

import com.h3xstream.findsecbugs.common.matcher.TaintTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by OP on 5/27/2018.
 */
public class VarInfo {


    public int index;
    public String type = "";
    public String value = "";
    public int taintType = TaintTypes.UNKNOWN;
    public boolean is_parameter;
    public List<Integer> oListTaintType = new ArrayList<>();
    public String name = "";
    public Map<String, String> attr = new HashMap<>();
    private Map<String, List<Integer>> valueMap = new HashMap<>();
    public List<List<Integer>> oValueList = new ArrayList<List<Integer>>();
    public String _str_value = "";

    public boolean getIsParameter() {
        return is_parameter;
    }

    public void setIsParamter(boolean _is_parameter) {
        this.is_parameter = _is_parameter;
    }

    public VarInfo(int index)
    {
        this.index = index;
    }

    public VarInfo(int index, int taintType)
    {
        this.index = index;
        this.taintType = taintType;

        oListTaintType.add(taintType);
    }

    public VarInfo(String type, String value)
    {
        this.type = type;
        this.value = value;
    }

    // attributes such as the query of the connection object
    public boolean addAttr(String ind, String val)
    {
        boolean ret_val = false;

        try {
            attr.put(ind, val);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return ret_val;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public VarInfo(VarInfo var_info)
    {
        index = var_info.getIndex();
        type = var_info.getType();
        value = var_info.getValue();
        taintType = var_info.getTaintType();
        is_parameter = var_info.isIs_parameter();
        name = var_info.getName();

        for(int integer : var_info.getoListTaintType())
        {
            oListTaintType.add(integer);
        }
        this.attr = var_info.attr;
        this.valueMap = var_info.valueMap;
    }

    public boolean addTaintType(int tempTaintType)
    {
        try
        {
            oListTaintType.add(tempTaintType);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return true;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }



    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getTaintType() {
        return taintType;
    }

    public void setTaintType(int taintType) {
        this.taintType = taintType;
        //addTaintType(taintType);
    }

    public void setAddTaintType(int taintType)
    {
        this.taintType = taintType;
        addTaintType(taintType);
    }

    public String getStringTaintType() {
        String ret_str = "";

        if(taintType == TaintTypes.TAINTED)
            ret_str = "TAINTED";
        else if(taintType == TaintTypes.UN_TAINTED)
            ret_str = "UN_TAINTED";
        else
            ret_str = "UNKNOWN";

        return ret_str;
    }

    public boolean isIs_parameter() {
        return is_parameter;
    }

    public void setIs_parameter(boolean is_parameter) {
        this.is_parameter = is_parameter;
    }

    public void addTaintToList() {

        oListTaintType.add(taintType);

    }

    @Override
    public String toString()
    {
        String ret_val = "";

        ret_val += "INDEX : " + index;
        ret_val += "TAINT VALS : ";

        for(int i = 0; i < oListTaintType.size(); i++)
        {
            Integer oInt = oListTaintType.get(i);
            if(oInt.intValue() == TaintTypes.TAINTED)
                ret_val += "TAINTED";
            else if(oInt.intValue() == TaintTypes.UN_TAINTED)
                ret_val += "UN_TAINTED";
            else
                ret_val += "UNKNOWN";

            if(i < (oListTaintType.size() - 1))
                ret_val += ", ";
        }


        ret_val += "\n\r";

        return ret_val;
    }

    public List<Integer> getoListTaintType() {
        return oListTaintType;
    }

    public void setoListTaintType(List<Integer> oListTaintType) {
        this.oListTaintType = oListTaintType;
    }

    public int getLastValue()
    {
        if(oListTaintType.size() > 0)

            return oListTaintType.get(oListTaintType.size() - 1);

        return TaintTypes.UNKNOWN;
    }

    public String getAttrByName(String query)
    {
        String ret_val = "";

        try
        {
            if(attr.containsKey(query))
                ret_val = attr.get(query);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return ret_val;
    }

    public boolean putMapValue(String temp_key, List<Integer> taint_type)
    {
        boolean ret_val = true;
        System.out.println("{{{VarInfo Map value put key : " + temp_key  + " :: taint_type : " + taint_type + "}}}");
        try
        {
            valueMap.put(temp_key, taint_type);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            ret_val = false;
        }

        return ret_val;
    }

    public List<Integer> getMapValue(String temp_key)
    {
        List<Integer> ret_val = new ArrayList<>();

        try
        {
            if(valueMap.containsKey(temp_key))
            {
                ret_val = valueMap.get(temp_key);
            }
            else
                System.out.println("$$Error in VarInfo, cant find the key : " + temp_key + " for the map value");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return ret_val;
    }

    public void print_all_taint_vals() {

        System.out.println("-=print_all_taint_vals=-");
        for(int tt : oListTaintType)
        {
            System.out.println("-=" + TaintTypes.typeToString(tt));
        }

    }

    public void setTaintOne(boolean untaint_to_none)
    {
        int lastValue = getLastValue();
        oListTaintType.clear();

        if(untaint_to_none  == true && lastValue == TaintTypes.UN_TAINTED)
            lastValue = TaintTypes.UN_TAINTED;

        oListTaintType.add(lastValue);
        taintType = lastValue;
    }

    public void addListTaintVal(List<Integer> value)
    {
        oValueList.add(value);
    }

    public void invokeListMethod_Remove(int int_val)
    {
        if(int_val >= 0 && int_val <= (oValueList.size() -1))
        {
            oValueList.remove(int_val);
        }
    }

    public List<Integer> invokeListMethod_Get(int int_val)
    {
        List<Integer> oList = new ArrayList();

        if(int_val >= 0 && int_val <= (oValueList.size() -1))
        {
            oList = oValueList.get(int_val);
        }
        return oList;
    }

    public void setStringValue(String str_value)
    {
        _str_value = str_value;
    }

    public String get_str_value() {
        return _str_value;
    }

    public void set_str_value(String _str_value) {
        this._str_value = _str_value;
    }

    public Map<String, String> getAttr() {
        return attr;
    }

    public void setAttr(Map<String, String> attr) {
        this.attr = attr;
    }

    public Map<String, List<Integer>> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<String, List<Integer>> valueMap) {
        this.valueMap = valueMap;
    }

    public List<List<Integer>> getoValueList() {
        return oValueList;
    }

    public void setoValueList(List<List<Integer>> oValueList) {
        this.oValueList = oValueList;
    }

    public char callStringMethod_charAt(int ind) {
        char c = 0;

        try
        {
            c = _str_value.charAt(ind);
            // ret_val = Character.toString(c);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }


        return c;
    }

    public int getTaintTypePrioritized() {

        int ret_val = -100;

        for(int i = 0; i < oListTaintType.size(); i++)
        {
            Integer oInt = oListTaintType.get(i);
            if(oInt.intValue() == TaintTypes.TAINTED)
                ret_val = TaintTypes.TAINTED;
            else if(oInt.intValue() == TaintTypes.UN_TAINTED && ret_val != TaintTypes.TAINTED)
                ret_val = TaintTypes.UN_TAINTED;
            else
                ret_val = TaintTypes.UNKNOWN;

        }

        return ret_val;

    }


    public void setUltimateTaintType(int unknown) {
        oListTaintType.clear();
        oListTaintType.add(unknown);
        taintType = unknown;
    }

    public void setTaintTypeIfNotExist(int taintTypePrioritized) {
        //System.out.println("setTaintTypeIfNotExist called : " + taintTypePrioritized);

        boolean exists = false;
        for(int i = 0; i < oListTaintType.size(); i++) {
            Integer oInt = oListTaintType.get(i);
            if(taintTypePrioritized == oInt.intValue())
            {
                //System.out.println("exists at true index : " + i);
                exists = true;
                break;
            }
        }

        if(!exists) {
            addTaintType(taintTypePrioritized);
            //System.out.println("^^^^^^ taint actually added : " + taintTypePrioritized);
        }
    }
}
