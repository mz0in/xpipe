package io.xpipe.core.process;

import io.xpipe.core.store.ShellStore;
import io.xpipe.core.store.StatefulDataStore;
import io.xpipe.core.util.*;
import lombok.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ShellControl extends ProcessControl {

    List<UUID> getExitUuids();

    Optional<ShellStore> getSourceStore();

    ShellControl withSourceStore(ShellStore store);

    List<ScriptSnippet> getInitCommands();

    ShellControl withTargetTerminalShellDialect(ShellDialect d);

    ShellDialect getTargetTerminalShellDialect();

    default boolean hasLocalSystemAccess() {
        return getSystemId() != null && getSystemId().equals(XPipeSystemId.getLocal());
    }

    boolean isLocal();

    ShellControl changesHosts();

    ShellControl getMachineRootSession();

    ShellControl withoutLicenseCheck();

    String getOsName();

    boolean isLicenseCheck();

    UUID getSystemId();

    ReentrantLock getLock();

    ShellControl onInit(FailableConsumer<ShellControl, Exception> pc);

    ShellControl onPreInit(FailableConsumer<ShellControl, Exception> pc);

    default <T extends ShellStoreState> ShellControl withShellStateInit(StatefulDataStore<T> store) {
        return onInit(shellControl -> {
            var s = store.getState();
            s.setOsType(shellControl.getOsType());
            s.setShellDialect(shellControl.getShellDialect());
            s.setRunning(true);
            s.setOsName(shellControl.getOsName());
            store.setState(s);
        });
    }

    default <T extends ShellStoreState> ShellControl withShellStateFail(StatefulDataStore<T> store) {
        return onFail(shellControl -> {
            var s = store.getState();
            s.setRunning(false);
            store.setState(s);
        });
    }

    ShellControl onExit(Consumer<ShellControl> pc);

    ShellControl onFail(Consumer<Throwable> t);

    ShellControl withExceptionConverter(ExceptionConverter converter);

    ShellControl withErrorFormatter(Function<String, String> formatter);

    String prepareTerminalOpen(TerminalInitScriptConfig config) throws Exception;

    String prepareIntermediateTerminalOpen(String content, TerminalInitScriptConfig config) throws Exception;

    String getSystemTemporaryDirectory();

    String getSubTemporaryDirectory();

    void checkRunning();

    default CommandControl osascriptCommand(String script) {
        return command(String.format(
                """
                osascript - "$@" <<EOF
                %s
                EOF
                """,
                script));
    }

    default byte[] executeSimpleRawBytesCommand(String command) throws Exception {
        try (CommandControl c = command(command).start()) {
            return c.readRawBytesOrThrow();
        }
    }

    default String executeSimpleStringCommand(String command) throws Exception {
        try (CommandControl c = command(command).start()) {
            return c.readStdoutOrThrow();
        }
    }

    default Optional<String> executeSimpleStringCommandAndCheck(String command) throws Exception {
        try (CommandControl c = command(command).start()) {
            var out = c.readStdoutDiscardErr();
            return c.getExitCode() == 0 ? Optional.of(out) : Optional.empty();
        }
    }

    default boolean executeSimpleBooleanCommand(String command) throws Exception {
        try (CommandControl c = command(command).start()) {
            return c.discardAndCheckExit();
        }
    }

    default void executeSimpleCommand(CommandBuilder command) throws Exception {
        try (CommandControl c = command(command).start()) {
            c.discardOrThrow();
        }
    }

    default void executeSimpleCommand(String command) throws Exception {
        try (CommandControl c = command(command).start()) {
            c.discardOrThrow();
        }
    }

    default void executeSimpleCommand(String command, String failMessage) throws Exception {
        try (CommandControl c = command(command).start()) {
            c.discardOrThrow();
        } catch (ProcessOutputException out) {
            throw ProcessOutputException.withPrefix(failMessage, out);
        }
    }

    ElevationResult buildElevatedCommand(CommandConfiguration input, String prefix) throws Exception;

    void restart() throws Exception;

    OsType getOsType();

    ElevationConfig getElevationConfig() throws Exception;

    ShellControl elevated(String message, FailableFunction<ShellControl, Boolean, Exception> elevationFunction);

    default ShellControl elevationPassword(SecretValue value) {
        return elevationPassword(() -> value);
    }
    ShellControl elevationPassword(FailableSupplier<SecretValue> value);

    ShellControl withInitSnippet(ScriptSnippet snippet);

    ShellControl additionalTimeout(int ms);

    default ShellControl disableTimeout() {
        return additionalTimeout(Integer.MAX_VALUE);
    }

    FailableSupplier<SecretValue> getElevationPassword();

    default ShellControl subShell(@NonNull ShellDialect type) {
        return subShell(p -> type.getLoginOpenCommand(), (sc) -> type.getLoginOpenCommand())
                .elevationPassword(getElevationPassword());
    }

    interface TerminalOpenFunction {

        String prepareWithoutInitCommand(ShellControl sc) throws Exception;

        default String prepareWithInitCommand(ShellControl sc, @NonNull String command) throws Exception {
            return command;
        }
    }

    default ShellControl identicalSubShell() {
        return subShell(p -> p.getShellDialect().getLoginOpenCommand(),
                (sc) -> sc.getShellDialect().getLoginOpenCommand()
        ).elevationPassword(getElevationPassword());
    }

    default <T> T enforceDialect(@NonNull ShellDialect type, FailableFunction<ShellControl, T, Exception> sc) throws Exception {
        if (isRunning() && getShellDialect().equals(type)) {
            return sc.apply(this);
        } else {
            try (var sub = subShell(type).start()) {
                return sc.apply(sub);
            }
        }
    }

    ShellControl subShell(
            FailableFunction<ShellControl, String, Exception> command, TerminalOpenFunction terminalCommand);

    void writeLineAndReadEcho(String command) throws Exception;

    void writeLineAndReadEcho(String command, boolean log) throws Exception;

    void cd(String directory) throws Exception;

    @Override
    ShellControl start();

    CommandControl command(FailableFunction<ShellControl, String, Exception> command);

    CommandControl command(
            FailableFunction<ShellControl, String, Exception> command,
            FailableFunction<ShellControl, String, Exception> terminalCommand);

    default CommandControl command(String... command) {
        var c = Arrays.stream(command).filter(s -> s != null).toArray(String[]::new);
        return command(shellProcessControl -> String.join("\n", c));
    }

    default CommandControl buildCommand(Consumer<CommandBuilder> builder) {
        return command(sc-> {
            var b = CommandBuilder.of();
            builder.accept(b);
            return b.buildString(sc);
        });
    }

    default CommandControl command(List<String> command) {
        return command(shellProcessControl -> ShellDialect.flatten(command));
    }

    default CommandControl command(CommandBuilder builder) {
        return command(shellProcessControl -> builder.buildString(shellProcessControl));
    }

    void exitAndWait() throws IOException;
}
