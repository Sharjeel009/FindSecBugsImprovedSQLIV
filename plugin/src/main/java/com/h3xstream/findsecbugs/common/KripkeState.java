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

public class KripkeState
{
    int _taint = 0;
    int _taint_kill = 0;
    int _taint_sink = 0;

    String _state_name;

    List<Connector> _outgoing_links = new ArrayList<Connector>();

    public KripkeState(String _state_name) {
        super();
        this._state_name = _state_name;
    }



    public String get_state_name() {
        return _state_name;
    }



    public void set_state_name(String _state_name) {
        this._state_name = _state_name;
    }



    public int get_taint() {
        return _taint;
    }

    public void set_taint(int _taint) {
        this._taint = _taint;
    }

    public int get_taint_kill() {
        return _taint_kill;
    }

    public void set_taint_kill(int _taint_kill) {
        this._taint_kill = _taint_kill;
    }

    public int get_taint_sink() {
        return _taint_sink;
    }

    public void set_taint_sink(int _taint_sink) {
        this._taint_sink = _taint_sink;
    }

    public List<Connector> get_outgoing_links() {
        return _outgoing_links;
    }

    public void set_outgoing_links(List<Connector> _outgoing_links) {
        this._outgoing_links = _outgoing_links;
    }

    public void add_outgoing_link(Connector connector)
    {
        this._outgoing_links.add(connector);
    }

    public String get_taint_bool() {
        return (_taint == 0) ? "FALSE" : "TRUE";
    }



    public String get_taint_kill_bool() {
        return (_taint_kill == 0) ? "FALSE" : "TRUE";
    }



    public String get_taint_sink_bool() {
        return (_taint_sink == 0) ? "FALSE" : "TRUE";
    }
}
