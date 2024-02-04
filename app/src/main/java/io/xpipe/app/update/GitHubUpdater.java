package io.xpipe.app.update;

import io.xpipe.app.core.AppProperties;
import javafx.scene.layout.Region;
import org.kohsuke.github.GHRelease;

import java.nio.file.Files;
import java.time.Instant;

public class GitHubUpdater extends UpdateHandler {

    public GitHubUpdater(boolean startBackgroundThread) {
        super(startBackgroundThread);
    }

    @Override
    public Region createInterface() {
        return null;
    }

    public void prepareUpdateImpl() {
        var downloadFile = AppDownloads.downloadInstaller(
                lastUpdateCheckResult.getValue().getAssetType(),
                lastUpdateCheckResult.getValue().getVersion(),
                false);
        if (downloadFile.isEmpty()) {
            return;
        }

        var changelogString =
                AppDownloads.downloadChangelog(lastUpdateCheckResult.getValue().getVersion(), false);
        var changelog = changelogString.orElse(null);
        var rel = new PreparedUpdate(
                AppProperties.get().getVersion(),
                XPipeDistributionType.get().getId(),
                lastUpdateCheckResult.getValue().getVersion(),
                lastUpdateCheckResult.getValue().getReleaseUrl(),
                lastUpdateCheckResult.getValue().getReleaseDate(),
                downloadFile.get(),
                changelog,
                lastUpdateCheckResult.getValue().getAssetType());
        preparedUpdate.setValue(rel);
    }

    public void executeUpdateAndCloseImpl() throws Exception {
        var downloadFile = preparedUpdate.getValue().getFile();
        if (!Files.exists(downloadFile)) {
            return;
        }

        AppInstaller.installFileLocal(preparedUpdate.getValue().getAssetType(), downloadFile);
    }

    public synchronized AvailableRelease refreshUpdateCheckImpl() throws Exception {
        var rel = AppDownloads.getLatestSuitableRelease();
        event("Determined latest suitable release "
                + rel.map(GHRelease::getName).orElse(null));

        if (rel.isEmpty()) {
            lastUpdateCheckResult.setValue(null);
            return null;
        }

        var isUpdate = isUpdate(rel.get().getTagName());
        var assetType = AppInstaller.getSuitablePlatformAsset();
        var ghAsset = rel.orElseThrow().listAssets().toList().stream()
                .filter(g -> assetType.isCorrectAsset(g.getName()))
                .findAny();
        if (ghAsset.isEmpty()) {
            return null;
        }

        event("Selected asset " + ghAsset.get().getName());
        lastUpdateCheckResult.setValue(new AvailableRelease(
                AppProperties.get().getVersion(),
                XPipeDistributionType.get().getId(),
                rel.get().getTagName(),
                rel.get().getHtmlUrl().toString(),
                ghAsset.get().getBrowserDownloadUrl(),
                assetType,
                Instant.now(),
                rel.get().getCreatedAt() != null ? rel.get().getCreatedAt().toInstant() : null,
                isUpdate));
        return lastUpdateCheckResult.getValue();
    }
}
