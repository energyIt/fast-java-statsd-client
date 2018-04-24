package com.energyit.statsd;

public class TagImpl implements Tag {
    private final byte[] name;
    private final byte[] value;

    public TagImpl(byte[] name, byte[] value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public byte[] getName() {
        return name;
    }

    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new String(getName())+':'+ new String(getValue());
    }
}
