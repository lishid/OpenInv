package com.lishid.openinv.internal;

import java.util.Iterator;

import com.google.common.collect.Iterators;

import net.minecraft.server.v1_11_R1.EnumDirection;
import net.minecraft.server.v1_11_R1.EnumDirection.EnumDirectionLimit;

public enum EnumDirectionList implements Iterable<EnumDirection> {

    HORIZONTAL(EnumDirectionLimit.HORIZONTAL),
    VERTICAL(EnumDirectionLimit.VERTICAL);

    private final EnumDirectionLimit list;

    EnumDirectionList(EnumDirectionLimit list) {
        this.list = list;
    }

    @Override
    public Iterator<EnumDirection> iterator() {
        return Iterators.forArray(list.a());
    }
}
