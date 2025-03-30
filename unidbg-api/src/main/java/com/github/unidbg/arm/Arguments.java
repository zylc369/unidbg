package com.github.unidbg.arm;

import com.github.unidbg.ByteArrayNumber;
import com.github.unidbg.StringNumber;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.pointer.UnidbgPointer;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Arguments {

    private static final Logger log = LoggerFactory.getLogger(Arguments.class);

    /**
     * 存储处理后的参数。
     * 每个元素可能是原始参数（如整数、浮点数等），也可能是栈指针地址（对于字符串或字节数组）。
     */
    public final Number[] args;

    /**
     * 字符串和数组参数写入栈（方法内会调用memory.writeStackXXX），null变成0，其他类型不变
     *
     * @param memory 虚拟机的内存
     * @param args   参数列表
     */
    Arguments(Memory memory, Number[] args) {
        int i = 0;
        while (args != null && i < args.length) {
            // 将参数数组中的字符串（StringNumber）和字节数组（ByteArrayNumber）写入模拟器的栈中。
            if (args[i] instanceof StringNumber) {
                StringNumber str = (StringNumber) args[i];

                // 将字符串写入栈中，并返回一个指向栈内存的指针（UnidbgPointer）。
                UnidbgPointer pointer = memory.writeStackString(str.value);
                if (log.isDebugEnabled()) {
                    log.debug("map string arg{}: {} -> {}", i + 1, pointer, args[i]);
                }

                // 将原始参数（StringNumber）替换为指针的地址值（pointer.peer）。
                args[i] = pointer.peer;

                // 将指针地址添加到 pointers 列表中，便于后续管理。
                pointers.add(pointer.peer);
            } else if (args[i] instanceof ByteArrayNumber) {
                ByteArrayNumber array = (ByteArrayNumber) args[i];

                // 调用 memory.writeStackBytes() 将字节数组写入栈中，并返回一个指向栈内存的指针。
                UnidbgPointer pointer = memory.writeStackBytes(array.value);
                if (log.isDebugEnabled()) {
                    log.debug("map bytes arg{}: {} -> {}", i + 1, pointer, Hex.encodeHexString(array.value));
                }

                // 将原始参数（ByteArrayNumber）替换为指针的地址值。同样将指针地址添加到 pointers 列表中。
                args[i] = pointer.peer;
                pointers.add(pointer.peer);
            } else if (args[i] == null) {
                // 如果参数为 null，将其替换为数值 0，表示空指针。
                args[i] = 0;
            }
            i++;
        }

        this.args = args;
    }

    /**
     * 用于记录所有分配到栈中的指针地址。
     * 它的主要作用是便于后续清理栈空间或跟踪指针的使用情况。
     */
    public final List<Number> pointers = new ArrayList<>(10);

}
