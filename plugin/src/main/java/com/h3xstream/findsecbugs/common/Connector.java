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


public class Connector
{
    KripkeState _next_state; // variable coz it can be
    int _trans_var = 0; // transition variable

    public Connector()
    {

    }

    public Connector(KripkeState _next_state, int _trans_var) {
        super();
        this._next_state = _next_state;
        this._trans_var = _trans_var;
    }

    public KripkeState get_next_state() {
        return _next_state;
    }
    public void set_next_state(KripkeState _next_state) {
        this._next_state = _next_state;
    }
    public int get_trans_var() {
        return _trans_var;
    }
    public void set_trans_var(int _trans_var) {
        this._trans_var = _trans_var;
    }

    public String get_trans_var_bool() {
        return (_trans_var == 0) ? "FALSE" : "TRUE";
    }
}
