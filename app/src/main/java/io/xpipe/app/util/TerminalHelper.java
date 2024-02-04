package io.xpipe.app.util;

import io.xpipe.app.core.AppI18n;
import io.xpipe.app.issue.ErrorEvent;
import io.xpipe.app.prefs.AppPrefs;
import io.xpipe.app.prefs.ExternalTerminalType;
import io.xpipe.app.storage.DataStorage;
import io.xpipe.app.storage.DataStoreEntry;
import io.xpipe.core.process.ProcessControl;
import io.xpipe.core.process.TerminalInitScriptConfig;

public class TerminalHelper {

    public static void open(String title, ProcessControl cc) throws Exception {
        open(null, title, cc);
    }

    public static void open(DataStoreEntry entry, String title, ProcessControl cc) throws Exception {
        var type = AppPrefs.get().terminalType().getValue();
        if (type == null) {
            throw ErrorEvent.unreportable(new IllegalStateException(AppI18n.get("noTerminalSet")));
        }

        var color = entry != null ? DataStorage.get().getRootForEntry(entry).getColor() : null;
        var prefix = entry != null && color != null && type.supportsColoredTitle()
                ? color.getEmoji() + " "
                : "";
        var cleanTitle = (title != null ? title : entry != null ? entry.getName() : "?");
        var adjustedTitle = prefix + cleanTitle;
                var file = ScriptHelper.createLocalExecScript(cc.prepareTerminalOpen(new TerminalInitScriptConfig(adjustedTitle, type.shouldClear(), color != null)));
        var config = new ExternalTerminalType.LaunchConfiguration(entry != null ? color : null, adjustedTitle, cleanTitle, file);
        try {
            type.launch(config);
        } catch (Exception ex) {
            throw ErrorEvent.unreportable(ex);
        }
    }
}
