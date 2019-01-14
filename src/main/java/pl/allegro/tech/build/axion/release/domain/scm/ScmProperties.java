package pl.allegro.tech.build.axion.release.domain.scm;

import java.io.File;

public class ScmProperties {

    private final String type;
    private final File directory;
    private final String remote;
    private final boolean pushTagsOnly;
    private final boolean fetchTags;
    private final boolean attachRemote;
    private final String remoteUrl;
    private final ScmIdentity identity;

    public ScmProperties(
        String type,
        File directory,
        String remote,
        boolean pushTagsOnly,
        boolean fetchTags,
        boolean attachRemote,
        String remoteUrl,
        ScmIdentity identity
    ) {
        this.type = type;
        this.directory = directory;
        this.remote = remote;
        this.pushTagsOnly = pushTagsOnly;
        this.fetchTags = fetchTags;
        this.attachRemote = attachRemote;
        this.remoteUrl = remoteUrl;
        this.identity = identity;
    }

    public ScmPushOptions pushOptions() {
        return new ScmPushOptions(remote, pushTagsOnly);
    }

    public final String getType() {
        return type;
    }

    public final File getDirectory() {
        return directory;
    }

    public final String getRemote() {
        return remote;
    }

    public final boolean isPushTagsOnly() {
        return pushTagsOnly;
    }

    public final boolean isFetchTags() {
        return fetchTags;
    }

    public final boolean isAttachRemote() {
        return attachRemote;
    }

    public final String getRemoteUrl() {
        return remoteUrl;
    }

    public final ScmIdentity getIdentity() {
        return identity;
    }
}
