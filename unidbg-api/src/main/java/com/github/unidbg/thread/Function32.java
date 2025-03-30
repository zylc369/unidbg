package com.github.unidbg.thread;

import com.github.unidbg.AbstractEmulator;
import com.github.unidbg.arm.ARM;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.memory.Memory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unicorn.ArmConst;

import java.util.Arrays;

public class Function32 extends MainTask {

    private static final Logger log = LoggerFactory.getLogger(Function32.class);

    private final long address;
    private final boolean paddingArgument;
    private final Number[] arguments;

    public Function32(int pid, long address, long until, boolean paddingArgument, Number... arguments) {
        super(pid, until);
        this.address = address;
        this.paddingArgument = paddingArgument;
        this.arguments = arguments;
    }

    @Override
    protected Number run(AbstractEmulator<?> emulator) {
        Backend backend = emulator.getBackend();
        Memory memory = emulator.getMemory();

        // 初始化参数
        ARM.initArgs(emulator, paddingArgument, arguments);

        // 获得栈顶
        long sp = memory.getStackPoint();
        if (sp % 8 != 0) {
            log.info("SP NOT 8 bytes aligned", new Exception(emulator.getStackPointer().toString()));
        }
        /*
        1. 背景知识：链接寄存器（LR）
        将一个值写入 ARM 架构的 链接寄存器（LR，Link Register）
        在 ARM 架构中，链接寄存器（LR，Link Register）是一个非常重要的寄存器，主要用于支持函数调用和返回机制。它的主要用途包括：
        (1) 函数返回地址
            当调用一个函数时，ARM 处理器会自动将返回地址存储到 LR 中。
            返回地址是指调用函数后需要继续执行的下一条指令的地址。
            在函数结束时，可以通过将 LR 的值加载回程序计数器（PC，Program Counter）来实现返回。
        (2) 保存上下文
            在某些情况下，LR 可能被用来保存临时数据或上下文信息。
            如果函数需要调用其他函数（即嵌套调用），LR 的值通常会被保存到栈中，以避免覆盖。
        (3) 特殊用途
            在异常处理或中断处理中，LR 可能被用来存储异常返回地址。

        2. 代码解析
        UC_ARM_REG_LR 是 Unicorn 引擎（或其他类似模拟器）中定义的一个常量，表示 ARM 架构的链接寄存器（LR）。
        until 是要写入 LR 的值。这个值通常是一个地址，表示函数调用结束后需要跳转的目标地址（即返回地址）。

        3. 使用场景
        (1) 模拟函数调用
            在动态分析工具（如 Unidbg 或 Unicorn）中，模拟函数调用时需要手动设置 LR 的值。
            例如：backend.reg_write(ArmConst.UC_ARM_REG_LR, returnAddress);
            returnAddress 是函数调用结束后需要跳转的地址。
            这样可以确保函数执行完毕后能够正确返回。
        (2) 修改控制流
            在某些情况下，可能需要修改程序的控制流，通过设置 LR 的值来改变函数的返回地址。
            例如，在调试或逆向工程中，可以通过修改 LR 来跳过某些代码逻辑。
        (3) 异常处理
            在模拟异常或中断处理时，LR 可能被用来存储异常返回地址。
            通过设置 LR 的值，可以模拟异常处理后的返回行为。

        4. 注意事项
        (1) 寄存器覆盖
            如果在函数调用过程中未正确保存 LR 的值，可能会导致返回地址被覆盖，从而引发程序崩溃或未定义行为。
            在嵌套调用中，通常会将 LR 的值保存到栈中：PUSH {LR}
        (2) 架构差异
            在 ARM64 架构中，链接寄存器被称为 X30，而不是 LR。
            不同架构的寄存器名称和编号可能不同，需要根据具体架构调整代码。
        (3) 地址有效性
            确保写入 LR 的地址是有效的，并且指向合法的指令。
            如果地址无效，可能会导致程序崩溃或异常。
         */
        backend.reg_write(ArmConst.UC_ARM_REG_LR, until);

        // 开始模拟
        return emulator.emulate(address, until);
    }

    @Override
    public String toThreadString() {
        return "Function32 address=0x" + Long.toHexString(address) + ", arguments=" + Arrays.toString(arguments);
    }

}
