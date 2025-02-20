package com.ixnah.hmcl.agent;

import com.ixnah.hmcl.api.LoaderApi;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class AgentClassFileTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassNode node = new ClassNode();
        reader.accept(node, LoaderApi.getClassReadFlogs());
        boolean modify = LoaderApi.allTransformer().map(t -> t.transform(loader, className, protectionDomain, node))
                .reduce(Boolean::logicalOr).orElse(false);
        if (modify) {
            ClassWriter writer = new ClassWriter(LoaderApi.getClassWriteFlags());
            node.accept(writer);
            LoaderApi.setClassWriteFlags(0);
            return writer.toByteArray();
        }
        return classfileBuffer;
    }
}
