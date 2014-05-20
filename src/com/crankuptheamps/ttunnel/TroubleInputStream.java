package com.crankuptheamps.ttunnel;

import com.crankuptheamps.ttunnel.filters.Filter;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TroubleInputStream extends FilterInputStream {

    private final Filter[] filters;
    private final boolean hasFilters;

    public TroubleInputStream(InputStream in, Filter[] filters) {
        super(in);
        this.filters = filters;
        hasFilters = filters != null && filters.length > 0;
    }


    public int read() throws IOException {
        int ret = super.read();
        if (ret != -1 && hasFilters) {
            for (int i = 0; i < filters.length; ++i) {
                ret = filters[i].filter(ret);
            }
        }
        return ret;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int ret = super.read(b, off, len);
        if (ret != -1 && hasFilters) {
            for (Filter f : filters) {
                ret = f.filter(b, off, ret);
            }
        }
        return ret;
    }


    public int read(byte[] b) throws IOException {
        int ret = super.read(b);
        if (ret != -1 && hasFilters) {
            for (int i = 0; i < filters.length; ++i) {
                ret = filters[i].filter(b, 0, ret);
            }
        }
        return ret;
    }

}
