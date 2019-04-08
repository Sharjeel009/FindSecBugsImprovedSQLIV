/**
 * Find Security Bugs
 * Copyright (c) Philippe Arteau, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.h3xstream.findsecbugs.common;

import java.util.ArrayList;
import java.util.List;

public class KripkeStructure
{
    List<KripkeState> _states  = new ArrayList<KripkeState>();
    int _trans_var;	// transition variable : tbc can also limit the values by enum
    int _start_state_index = 0;	// start state index



    public String getNuSMVCode()
    {
        String code = "";

        // todo: check if there are states

        code += "MODULE main" + getNewLine(2);

        // VARS
        code += "VAR" + getNewLine(1);
        code += "taint : boolean;" + getNewLine(1);
        code += "taint_kill : boolean;" + getNewLine(1);
        code += "taint_sink : boolean;" + getNewLine(1);
        code += "state : " + getStateCode() + ";" + getNewLine(1);

        code += getNewLine(2);

        //IVAR
        code += "IVAR" + getNewLine(1);
        code += "trans_var : boolean;" + getNewLine(1);

        code += getNewLine(2);
        code += "ASSIGN" + getNewLine(1) + getInitCode();

        code += getNewLine(2);
        code += "next(state) := case" + getNewLine(1);
        for(int i = 0; i < _states.size(); i++)
        {
            KripkeState curr_state = _states.get(i);
            List<Connector> connectors = curr_state.get_outgoing_links();

            for(int j = 0; j < connectors.size(); j++)
            {
                Connector currConnector = connectors.get(j);
                code += "\t" + "state = " + curr_state.get_state_name() + " & " + " trans_var = " + currConnector.get_trans_var_bool() + " : " + currConnector.get_next_state().get_state_name() + " ;" + getNewLine(1);
            }
        }
        code += "\t" + "TRUE : state;" + getNewLine(1);
        code += "esac;";

        // ---- taint variable
        code += getNewLine(2);
        code += "next(taint) := case" + getNewLine(1);
        for(int i = 0; i < _states.size(); i++)
        {
            KripkeState curr_state = _states.get(i);
            List<Connector> connectors = curr_state.get_outgoing_links();

            for(int j = 0; j < connectors.size(); j++)
            {
                Connector currConnector = connectors.get(j);
                code += "\t" + "taint = " + curr_state.get_taint_bool() + " & " + " trans_var = " + currConnector.get_trans_var_bool() + " : " + currConnector.get_next_state().get_taint_bool() + " ;"  + getNewLine(1);
            }
        }
        code += "\t" + "TRUE : taint;" + getNewLine(1);
        code += "esac;";

        // ---- taint_kill variable
        code += getNewLine(2);
        code += "next(taint_kill) := case" + getNewLine(1);
        for(int i = 0; i < _states.size(); i++)
        {
            KripkeState curr_state = _states.get(i);
            List<Connector> connectors = curr_state.get_outgoing_links();

            for(int j = 0; j < connectors.size(); j++)
            {
                Connector currConnector = connectors.get(j);
                code += "\t" + "taint_kill = " + curr_state.get_taint_kill_bool() + " & " + " trans_var = " + currConnector.get_trans_var_bool() + " : " + currConnector.get_next_state().get_taint_kill_bool() + " ;"  + getNewLine(1);
            }
        }
        code += "\t" + "TRUE : taint_kill;" + getNewLine(1);
        code += "esac;";

        // ---- taint_sink variable
        code += getNewLine(2);
        code += "next(taint_sink) := case" + getNewLine(1);
        for(int i = 0; i < _states.size(); i++)
        {
            KripkeState curr_state = _states.get(i);
            List<Connector> connectors = curr_state.get_outgoing_links();

            for(int j = 0; j < connectors.size(); j++)
            {
                Connector currConnector = connectors.get(j);
                code += "\t" + "taint_sink = " + curr_state.get_taint_sink_bool() + " & " + " trans_var = " + currConnector.get_trans_var_bool() + " : " + currConnector.get_next_state().get_taint_sink_bool() + " ;"  + getNewLine(1);
            }
        }
        code += "\t" + "TRUE : taint_sink;" + getNewLine(1);
        code += "esac;";


        return code;
    }

    private String getInitCode()
    {
        String str = "";

        // doing the init state init
        // if out of bounds set to zero
        if(_start_state_index > _states.size() - 1)
            _start_state_index = 0;

        KripkeState start_state = _states.get(_start_state_index);

        str += "init(state) := " + start_state.get_state_name() + ";" + getNewLine(1);

        str += "init(taint) := " + start_state.get_taint_bool() + ";" + getNewLine(1);
        str += "init(taint_kill) := " + start_state.get_taint_kill_bool() + ";" + getNewLine(1);
        str += "init(taint_sink) := " + start_state.get_taint_sink_bool() + ";" + getNewLine(1);

        return str;
    }

    private String getStateCode()
    {
        String str = "{";

        for(int i = 0; i < _states.size(); i++)
        {
            KripkeState curr_state = _states.get(i);
            str += curr_state.get_state_name();

            if(i < _states.size() - 1)
                str += ",";
        }

        str += "}";

        return str;
    }

    public String getNewLine(int n)
    {
        String str = "";

        for(int i = 0; i < n; i++)
            str += "\n\r";

        return str;
    }


    public List<KripkeState> get_states() {
        return _states;
    }


    public void set_states(List<KripkeState> _states) {
        this._states = _states;
    }

    public void add_state(KripkeState _state)
    {
        this._states.add(_state);
    }

    public int get_trans_var() {
        return _trans_var;
    }


    public void set_trans_var(int _trans_var) {
        this._trans_var = _trans_var;
    }


    public int get_start_state_index() {
        return _start_state_index;
    }


    public void set_start_state_index(int _start_state_index) {
        this._start_state_index = _start_state_index;
    }





}
