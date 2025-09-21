package com.marcel.utils.CryptoStuff;

import java.util.ArrayList;
import java.util.List;

public class ArrConv {
    public static byte[] ByteCListToBytePArr(List<Byte> input) {
        byte[] arr = new byte[input.size()];
        for (int i = 0; i < arr.length; i++)
            arr[i] = input.get(i);
        return arr;
    }

    public static List<Byte> ByteBArrToByteCList(byte[] input) {
        List<Byte> list = new ArrayList<>();
        for (byte x : input)
            list.add(x);
        return list;
    }
}
