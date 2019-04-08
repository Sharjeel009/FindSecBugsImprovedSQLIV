package com.h3xstream.findsecbugs.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import com.google.common.collect.ArrayListMultimap;


public class Kripke {

    List<String> Q = new ArrayList<>(); // set of states
    String q0 = "";
    List<String> InputSymbols = new ArrayList<>();  // submissino sign
    //List<Integer> Sigma = new ArrayList<>();
    List<KripkeSigma> Sigma = new ArrayList<>();
    Map<String, List<Integer>> L = new HashMap<>(); // equal to lemda

    public Kripke() {
    }

    public boolean addInputSymbol(String input_symbol)
    {
        boolean ret_val = true;

        try
        {
            if(!InputSymbols.contains(input_symbol))
                InputSymbols.add(input_symbol);
        }
        catch(Exception ex)
        {
            ret_val = false;
        }

        return ret_val;
    }

    public void setQ0(String q0) {
        this.q0 = q0;
    }

    public List<String> getInputSymbols() {
        return InputSymbols;
    }

    public void setInputSymbols(List<String> inputSymbols) {
        InputSymbols = inputSymbols;
    }

    public Kripke(List<String> q) {
        Q = q;
    }


    public boolean addS(String state_id)
    {
        boolean ret_val = false;

        if(!Q.contains(state_id))
        {
            Q.add(state_id);
            ret_val = true;
        }

        return ret_val;
    }

    public boolean setI(String stateid)
    {
        q0 = stateid;
        return true;
    }

/*    public boolean addR(String first_state, String second_state)
    {
        boolean addR = true;

        // contains is handled here
        if(R.containsKey(first_state))
        {
            List<String> s = R.get(first_state);
            if(s.contains(second_state))
                addR = false;
        }
        else
        {
            R.put(first_state, new ArrayList<>());
        }

        if(addR)
        {
            List<String> s = R.get(first_state);
            s.add(second_state);
        }

        return true;
    }
*/
    public boolean addL(String i, List<Integer> oList)
    {
        L.put(i, oList);

        return true;
    }



    public List<String> getQ()
    {
        return Q;
    }

    public void setQ(List<String> q)
    {
        Q = q;
    }

    public String getQ0()
    {
        return q0;
    }

    public List<KripkeSigma> getSigma()
    {
        return Sigma;
    }

    public void setSigma(List<KripkeSigma> sigma)
    {
        Sigma = sigma;
    }
/*
    public Map<String, List<String>> getR() {
        return R;
    }

    public void setR(Map<String, List<String>> r) {
        R = r;
    }
*/
    public Map<String, List<Integer>> getL() {
        return L;
    }

    public void setL(Map<String, List<Integer>> l) {
        L = l;
    }

    @Override
    public String toString()
    {
        String ret_str = "";

        ret_str += "States (Q) : ";

        for(String s : Q)
        {
            ret_str += s + ",";
        }

        ret_str += "\n\r";

        ret_str += "Initial State (q0) : " + getQ0() + "\n\r";

        ret_str  += "Relation (R) \n\r";

        /*
        for (Map.Entry<String, List<String>> entry : R.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            ret_str += key + " : ";

            for(int i = 0; i < value.size(); i++)
            {
                String ul = value.get(i);
                ret_str += ul + ", ";
            }

            ret_str += "\n\r";
        }
        */

        ret_str += "Labeling (L) : \n\r";

        for (Map.Entry<String, List<Integer>> entry : L.entrySet())
        {
            String key = entry.getKey();
            List<Integer> value = entry.getValue();
            ret_str += key + " : ";
            for(int i = 0; i < value.size(); i++)
            {
                int ul = value.get(i);

                ret_str += ul;
                if(i % 2 != 0)
                    ret_str += ", ";
            }
            ret_str += "\n\r";
        }

        return ret_str;
    }

    public boolean addSigma(KripkeSigma newSigma)
    {
        boolean ret_val = true;

        try
        {
            System.out.println("--=-=-==" + newSigma.getInput_symbol());

            addInputSymbol(newSigma.getInput_symbol());
            newSigma.setInput_symbol(newSigma.getInput_symbol());

            // if there exists input var and init state
            for(int i = 0; i < Sigma.size(); i++)
            {
                KripkeSigma kripkeSigma = Sigma.get(i);
                boolean add_input_var = false;
                while(kripkeSigma.getInput_symbol().equals(newSigma.getInput_symbol()) && kripkeSigma.getState().equals(newSigma.getState())) {
                    newSigma.setInput_symbol(newSigma.getInput_symbol() + "x");
                    add_input_var = true;
                }
                if(add_input_var)
                    addInputSymbol(newSigma.getInput_symbol());
            }

            Sigma.add(newSigma);
        }
        catch(Exception ex)
        {
            ret_val = false;
        }

        return ret_val;
    }
}