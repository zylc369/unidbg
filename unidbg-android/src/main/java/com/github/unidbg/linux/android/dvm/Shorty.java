package com.github.unidbg.linux.android.dvm;

import java.lang.reflect.Array;

public class Shorty {

    /**
     * 数组维度，一维值为1，二维值为2，当这个值大于0的时候代表数组
     */
    private final int arrayDimensions;

    /**
     * 类型
     * 包含：L、B、C、I、S、Z、D、F、J。不包含：[
     * 如果是数组，这个字段会被指定为数组元素的类型：L、B、C、I、S、Z、D、F、J，当arrayDimensions大于0的时候，就是数组
     */
    private final char type;

    /**
     * 对象类型的类型签名，例如：Ljava/lang/String;
     */
    private String binaryName;

    Shorty(int arrayDimensions, char type) {
        this.arrayDimensions = arrayDimensions;
        this.type = type;
    }

    final void setBinaryName(String binaryName) {
        this.binaryName = binaryName;
    }

    public char getType() {
        return arrayDimensions > 0 ? 'L' : type;
    }

    private static Class<?> getPrimitiveType(char c) {
        switch (c) {
            case 'B':
                return byte.class;
            case 'C':
                return char.class;
            case 'I':
                return int.class;
            case 'S':
                return short.class;
            case 'Z':
                return boolean.class;
            case 'F':
                return float.class;
            case 'D':
                return double.class;
            case 'J':
                return long.class;
            default:
                return null;
        }
    }

    public Class<?> decodeType(ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = Shorty.class.getClassLoader();
        }

        Class<?> clazz = getPrimitiveType(getType());
        if (clazz != null) {
            return clazz;
        }
        int dimensions = this.arrayDimensions;
        if (dimensions > 0) {
            try {
                clazz = binaryName == null ? getPrimitiveType(type) : classLoader.loadClass(binaryName.replace('/', '.'));
                if (clazz == null) {
                    throw new IllegalStateException("type=" + type);
                }
                while (dimensions-- > 0) {
                    clazz = Array.newInstance(clazz, 1).getClass();
                }
                return clazz;
            } catch (ClassNotFoundException ignored) {
            }
            return null;
        } else {
            if (binaryName == null) {
                throw new IllegalStateException("binaryName is null");
            }
            try {
                clazz = classLoader.loadClass(binaryName.replace('/', '.'));
            } catch (ClassNotFoundException ignored) {
            }
            return clazz;
        }
    }

    /**
     * 转换为签名，例如：
     * [I
     * Ljava/lang/String;
     * I
     * @return 返回签名
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 拼接数组符号
        for (int i = 0; i < arrayDimensions; i++) {
            sb.append('[');
        }
        sb.append(type);
        if (binaryName != null) {
            sb.append(binaryName).append(';');
        }
        return sb.toString();
    }

}
