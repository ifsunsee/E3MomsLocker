// Copyright (c) 2016 Soocheol Lee(soocheol.wind@lge.com). All rights reserved.
// Use of this source code is governed by a LGPL ver 3.0 license that can be
// found in the LICENSE file.

package com.laize.e3momslocker;

import java.util.ArrayList;

public interface PatternViewDelegate {
    abstract public void onRecordedPattern(boolean successed, ArrayList<Integer> patterns);
    abstract public void onCreatedPattern(boolean successed, ArrayList<Integer> patterns);
    abstract public void onSelectedColor(boolean selected, int color);
}
