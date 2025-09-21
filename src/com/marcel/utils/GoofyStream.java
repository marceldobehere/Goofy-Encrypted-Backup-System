package com.marcel.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class GoofyStream {
    public InputStream _is;
    private List<Function<InputStream, InputStream>> isFuncs;
    public OutputStream _os;
    private List<Function<OutputStream, OutputStream>> osFuncs;

    public GoofyStream(InputStream is, OutputStream os) {
        _is = is;
        _os = os;
        isFuncs = new ArrayList<>();
        osFuncs = new ArrayList<>();
    }

    public void connectIs(Function<InputStream, InputStream> f) {
        isFuncs.add(f);
    }

    public void connectOs(Function<OutputStream, OutputStream> f) {
        osFuncs.add(f);
    }

    private void applyIsFuncs(boolean flipIs, boolean flipOs) {
        if (flipIs)
            Collections.reverse(isFuncs);
        if (flipOs)
            Collections.reverse(osFuncs);

        for (Function<InputStream, InputStream> f : isFuncs)
            _is = f.apply(_is);
        for (Function<OutputStream, OutputStream> f : osFuncs)
            _os = f.apply(_os);
    }

    // Does the flips so its in the natural order of adding
    // ie first added is first applied (For both input and output streams)
    public void complete() {
        complete(false, true);
    }

    public void complete(boolean flipIs, boolean flipOs) {
        try {
            applyIsFuncs(flipIs, flipOs);
            _is.transferTo(_os);
            _is.close();
            _os.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
