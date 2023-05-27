package io.xpipe.core.util;

import io.xpipe.core.impl.FileNames;
import io.xpipe.core.process.ShellControl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class XPipeSystemId {

    private static UUID localId;

    public static void init() {
        try {
        var file = Path.of(System.getProperty("user.home")).resolve(".xpipe").resolve("system_id");
        if (!Files.exists(file)) {
            Files.writeString(file, UUID.randomUUID().toString());
        }
            localId = UUID.fromString(Files.readString(file).trim());
        } catch (Exception ex) {
            localId = UUID.randomUUID();
        }
    }

    public static UUID getLocal() {
        return localId;
    }

    public static UUID getSystemId(ShellControl proc) throws Exception {
        var file = proc.getOsType().getSystemIdFile(proc);

        if (!proc.getShellDialect().createFileExistsCommand(proc, file).executeAndCheck()) {
            proc.executeSimpleCommand(
                    proc.getShellDialect().getMkdirsCommand(FileNames.getParent(file)),
                    "Unable to access or create directory " + file);
            var id = UUID.randomUUID();
            proc.getShellDialect().createScriptTextFileWriteCommand(proc, id.toString(), file).execute();
            return id;
        }

        return UUID.fromString(proc.executeSimpleStringCommand(proc.getShellDialect().getFileReadCommand(file)).trim());
    }
}
