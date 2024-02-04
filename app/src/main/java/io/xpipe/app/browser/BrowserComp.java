package io.xpipe.app.browser;

import atlantafx.base.controls.RingProgressIndicator;
import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import io.xpipe.app.browser.icon.DirectoryType;
import io.xpipe.app.browser.icon.FileIconManager;
import io.xpipe.app.browser.icon.FileType;
import io.xpipe.app.comp.base.MultiContentComp;
import io.xpipe.app.comp.base.SideSplitPaneComp;
import io.xpipe.app.core.AppLayoutModel;
import io.xpipe.app.fxcomps.Comp;
import io.xpipe.app.fxcomps.SimpleComp;
import io.xpipe.app.fxcomps.impl.FancyTooltipAugment;
import io.xpipe.app.fxcomps.impl.PrettyImageHelper;
import io.xpipe.app.fxcomps.impl.VerticalComp;
import io.xpipe.app.fxcomps.util.BindingsHelper;
import io.xpipe.app.fxcomps.util.PlatformThread;
import io.xpipe.app.fxcomps.util.SimpleChangeListener;
import io.xpipe.app.storage.DataStorage;
import io.xpipe.app.util.BooleanScope;
import io.xpipe.app.util.ThreadHelper;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static atlantafx.base.theme.Styles.DENSE;
import static atlantafx.base.theme.Styles.toggleStyleClass;
import static javafx.scene.control.TabPane.TabClosingPolicy.ALL_TABS;

public class BrowserComp extends SimpleComp {

    private final BrowserModel model;

    public BrowserComp(BrowserModel model) {
        this.model = model;
    }

    @Override
    protected Region createSimple() {
        FileType.loadDefinitions();
        DirectoryType.loadDefinitions();
        ThreadHelper.runAsync(() -> {
            FileIconManager.loadIfNecessary();
        });

        var bookmarksList = new BrowserBookmarkList(model).vgrow();
        var localDownloadStage = new BrowserTransferComp(model.getLocalTransfersStage()).hide(
                PlatformThread.sync(Bindings.createBooleanBinding(() -> {
                    if (model.getOpenFileSystems().size() == 0) {
                        return true;
                    }

                    if (model.getMode().isChooser()) {
                        return true;
                    }

                    // Also show on local
                    if (model.getSelected().getValue() != null) {
                        // return model.getSelected().getValue().isLocal();
                    }

                    return false;
                }, model.getOpenFileSystems(), model.getSelected())));
        localDownloadStage.prefHeight(200);
        localDownloadStage.maxHeight(200);
        var vertical = new VerticalComp(List.of(bookmarksList, localDownloadStage));

        var splitPane = new SideSplitPaneComp(vertical, createTabs()).withInitialWidth(
                AppLayoutModel.get().getSavedState().getBrowserConnectionsWidth()).withOnDividerChange(
                AppLayoutModel.get().getSavedState()::setBrowserConnectionsWidth).apply(struc -> {
            struc.getLeft().setMinWidth(200);
            struc.getLeft().setMaxWidth(500);
        });
        var r = addBottomBar(splitPane.createRegion());
        r.getStyleClass().add("browser");
        // AppFont.small(r);
        return r;
    }

    private Region addBottomBar(Region r) {
        if (!model.getMode().isChooser()) {
            return r;
        }

        var selectedLabel = new Label("Selected: ");
        selectedLabel.setAlignment(Pos.CENTER);
        var selected = new HBox();
        selected.setAlignment(Pos.CENTER_LEFT);
        selected.setSpacing(10);
        model.getSelection().addListener((ListChangeListener<? super BrowserEntry>) c -> {
            PlatformThread.runLaterIfNeeded(() -> {
                selected.getChildren().setAll(c.getList().stream().map(s -> {
                    var field = new TextField(s.getRawFileEntry().getPath());
                    field.setEditable(false);
                    field.setPrefWidth(500);
                    return field;
                }).toList());
            });
        });
        var spacer = new Spacer(Orientation.HORIZONTAL);
        var button = new Button("Select");
        button.setPadding(new Insets(5, 10, 5, 10));
        button.setOnAction(event -> model.finishChooser());
        button.setDefaultButton(true);
        var bottomBar = new HBox(selectedLabel, selected, spacer, button);
        HBox.setHgrow(selected, Priority.ALWAYS);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.getStyleClass().add("chooser-bar");

        var layout = new VBox(r, bottomBar);
        VBox.setVgrow(r, Priority.ALWAYS);
        return layout;
    }

    private Comp<?> createTabs() {
        var multi = new MultiContentComp(Map.<Comp<?>, ObservableValue<Boolean>>of(Comp.of(() -> createTabPane()),
                BindingsHelper.persist(Bindings.isNotEmpty(model.getOpenFileSystems())),
                new BrowserWelcomeComp(model).apply(struc -> StackPane.setAlignment(struc.get(), Pos.CENTER_LEFT)),
                Bindings.createBooleanBinding(() -> {
                    return model.getOpenFileSystems().size() == 0 && !model.getMode().isChooser();
                }, model.getOpenFileSystems())));
        return multi;
    }

    private TabPane createTabPane() {
        var tabs = new TabPane();
        tabs.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        tabs.setTabMinWidth(Region.USE_PREF_SIZE);
        tabs.setTabMaxWidth(400);
        tabs.setTabClosingPolicy(ALL_TABS);
        Styles.toggleStyleClass(tabs, TabPane.STYLE_CLASS_FLOATING);
        toggleStyleClass(tabs, DENSE);

        var map = new HashMap<OpenFileSystemModel, Tab>();

        // Restore state
        model.getOpenFileSystems().forEach(v -> {
            var t = createTab(tabs, v);
            map.put(v, t);
            tabs.getTabs().add(t);
        });
        tabs.getSelectionModel().select(model.getOpenFileSystems().indexOf(model.getSelected().getValue()));

        // Used for ignoring changes by the tabpane when new tabs are added. We want to perform the selections manually!
        var modifying = new SimpleBooleanProperty();

        // Handle selection from platform
        tabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (modifying.get()) {
                return;
            }

            if (newValue == null) {
                model.getSelected().setValue(null);
                return;
            }

            var source = map.entrySet()
                    .stream()
                    .filter(openFileSystemModelTabEntry -> openFileSystemModelTabEntry.getValue().equals(newValue))
                    .findAny()
                    .map(Map.Entry::getKey)
                    .orElse(null);
            model.getSelected().setValue(source);
        });

        // Handle selection from model
        model.getSelected().addListener((observable, oldValue, newValue) -> {
            PlatformThread.runLaterIfNeeded(() -> {
                if (newValue == null) {
                    tabs.getSelectionModel().select(null);
                    return;
                }

                var toSelect = map.entrySet()
                        .stream()
                        .filter(openFileSystemModelTabEntry -> openFileSystemModelTabEntry.getKey().equals(newValue))
                        .findAny()
                        .map(Map.Entry::getValue)
                        .orElse(null);
                if (toSelect == null || !tabs.getTabs().contains(toSelect)) {
                    tabs.getSelectionModel().select(null);
                    return;
                }

                tabs.getSelectionModel().select(toSelect);
            });
        });

        model.getOpenFileSystems().addListener((ListChangeListener<? super OpenFileSystemModel>) c -> {
            while (c.next()) {
                for (var r : c.getRemoved()) {
                    PlatformThread.runLaterIfNeeded(() -> {
                        try (var b = new BooleanScope(modifying).start()) {
                            var t = map.remove(r);
                            tabs.getTabs().remove(t);
                        }
                    });
                }

                for (var a : c.getAddedSubList()) {
                    PlatformThread.runLaterIfNeeded(() -> {
                        try (var b = new BooleanScope(modifying).start()) {
                            var t = createTab(tabs, a);
                            map.put(a, t);
                            tabs.getTabs().add(t);
                        }
                    });
                }
            }
        });

        tabs.getTabs().addListener((ListChangeListener<? super Tab>) c -> {
            while (c.next()) {
                for (var r : c.getRemoved()) {
                    var source = map.entrySet()
                            .stream()
                            .filter(openFileSystemModelTabEntry -> openFileSystemModelTabEntry.getValue().equals(r))
                            .findAny()
                            .orElse(null);

                    // Only handle close events that are triggered from the platform
                    if (source == null) {
                        continue;
                    }

                    model.closeFileSystemAsync(source.getKey());
                }
            }
        });
        return tabs;
    }

    private Tab createTab(TabPane tabs, OpenFileSystemModel model) {
        var tab = new Tab();

        var ring = new RingProgressIndicator(0, false);
        ring.setMinSize(16, 16);
        ring.setPrefSize(16, 16);
        ring.setMaxSize(16, 16);
        ring.progressProperty().bind(Bindings.createDoubleBinding(() -> model.getBusy().get() ? -1d : 0, PlatformThread.sync(model.getBusy())));

        var image = model.getEntry().get().getProvider().getDisplayIconFileName(model.getEntry().getStore());
        var logo = PrettyImageHelper.ofFixedSquare(image, 16).createRegion();

        tab.graphicProperty().bind(Bindings.createObjectBinding(() -> {
            return model.getBusy().get() ? ring : logo;
        }, PlatformThread.sync(model.getBusy())));
        tab.setText(model.getName());

        tab.setContent(new OpenFileSystemComp(model).createSimple());

        var id = UUID.randomUUID().toString();
        tab.setId(id);

        SimpleChangeListener.apply(tabs.skinProperty(), newValue -> {
            if (newValue != null) {
                Platform.runLater(() -> {
                    Label l = (Label) tabs.lookup("#" + id + " .tab-label");
                    var w = l.maxWidthProperty();
                    l.minWidthProperty().bind(w);
                    l.prefWidthProperty().bind(w);

                    var close = (StackPane) tabs.lookup("#" + id + " .tab-close-button");
                    close.setPrefWidth(30);

                    StackPane c = (StackPane) tabs.lookup("#" + id + " .tab-container");
                    c.getStyleClass().add("color-box");
                    var color = DataStorage.get().getRootForEntry(model.getEntry().get()).getColor();
                    if (color != null) {
                        c.getStyleClass().add(color.getId());
                    }
                    new FancyTooltipAugment<>(new SimpleStringProperty(model.getTooltip())).augment(c);
                    c.addEventHandler(DragEvent.DRAG_ENTERED, mouseEvent -> Platform.runLater(() -> tabs.getSelectionModel().select(tab)));
                });
            }
        });

        return tab;
    }
}
