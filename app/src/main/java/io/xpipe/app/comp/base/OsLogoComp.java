package io.xpipe.app.comp.base;

import io.xpipe.app.comp.store.StoreEntryWrapper;
import io.xpipe.app.core.AppResources;
import io.xpipe.app.fxcomps.SimpleComp;
import io.xpipe.app.fxcomps.impl.PrettyImageHelper;
import io.xpipe.app.fxcomps.impl.StackComp;
import io.xpipe.app.fxcomps.util.BindingsHelper;
import io.xpipe.core.process.OsNameState;
import io.xpipe.core.store.FileNames;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Region;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsLogoComp extends SimpleComp {

    private final StoreEntryWrapper wrapper;
    private final ObservableValue<SystemStateComp.State> state;

    public OsLogoComp(StoreEntryWrapper wrapper) {
        this(wrapper, new SimpleObjectProperty<>(SystemStateComp.State.SUCCESS));
    }

    public OsLogoComp(StoreEntryWrapper wrapper, ObservableValue<SystemStateComp.State> state) {
        this.wrapper = wrapper;
        this.state = state;
    }

    @Override
    protected Region createSimple() {
        var img = BindingsHelper.persist(Bindings.createObjectBinding(
                () -> {
                    if (state.getValue() != SystemStateComp.State.SUCCESS) {
                        return null;
                    }

                    var ps = wrapper.getPersistentState().getValue();
                    if (!(ps instanceof OsNameState ons)) {
                        return null;
                    }

                    return getImage(ons.getOsName());
                },
                wrapper.getPersistentState(), state));
        var hide = BindingsHelper.map(img, s -> s != null);
        return new StackComp(List.of(
                        new SystemStateComp(state).hide(hide),
                        PrettyImageHelper.ofRasterized(img, 24, 24).visible(hide)))
                .createRegion();
    }

    private static final Map<String, String> ICONS = new HashMap<>();
    private static final String LINUX_DEFAULT = "linux-24.png";

    private String getImage(String name) {
        if (name == null) {
            return null;
        }

        if (ICONS.isEmpty()) {
            AppResources.with(AppResources.XPIPE_MODULE, "img/os", file -> {
                try (var list = Files.list(file)) {
                    list.filter(path -> path.toString().endsWith(".svg") && !path.toString().endsWith(LINUX_DEFAULT))
                            .map(path -> FileNames.getFileName(path.toString())).forEach(path -> {
                        var base = FileNames.getBaseName(path).replace("-dark", "") + "-24.png";
                        ICONS.put(FileNames.getBaseName(base).split("-")[0], "os/" + base);
                    });
                }
            });
        }

        return ICONS.entrySet().stream().filter(e->name.toLowerCase().contains(e.getKey())).findAny().map(e->e.getValue()).orElse("os/" + LINUX_DEFAULT);
    }
}
