package pl.allegro.tech.build.axion.release.domain;

import com.github.zafarkhaja.semver.Version;
import pl.allegro.tech.build.axion.release.domain.properties.NextVersionProperties;
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties;
import pl.allegro.tech.build.axion.release.domain.properties.VersionProperties;
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition;
import pl.allegro.tech.build.axion.release.domain.scm.ScmRepository;
import pl.allegro.tech.build.axion.release.domain.scm.TagsOnCommit;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Returned structure is:
 * * previousVersion: version read from last release tag
 * * version: either:
 * * forced version
 * * version read from last next version tag
 * * version read from last release tag and incremented when not on tag
 * * version read from last release tag when on tag
 */
public class VersionResolver {

    private final ScmRepository repository;
    private final VersionSorter sorter;

    public VersionResolver(ScmRepository repository) {
        this.repository = repository;
        this.sorter = new VersionSorter();
    }

    public VersionContext resolveVersion(VersionProperties versionProperties, TagProperties tagProperties, NextVersionProperties nextVersionProperties) {
        ScmPosition position = repository.currentPosition();

        VersionFactory versionFactory = new VersionFactory(versionProperties, tagProperties, nextVersionProperties, position);

        VersionInfo versions;
        if (versionProperties.isUseHighestVersion()) {
            versions = readVersionsByHighestVersion(versionFactory, tagProperties, nextVersionProperties, versionProperties);
        } else {
            versions = readVersions(versionFactory, tagProperties, nextVersionProperties, versionProperties);
        }


        ScmState scmState = new ScmState(
            versions.onReleaseTag,
            versions.onNextVersionTag,
            versions.noTagsFound,
            repository.checkUncommittedChanges()
        );

        VersionFactory.FinalVersion finalVersion = versionFactory.createFinalVersion(scmState, versions.current);

        return new VersionContext(finalVersion.version, finalVersion.snapshot, versions.previous, position);
    }

    private VersionInfo readVersions(
        VersionFactory versionFactory,
        TagProperties tagProperties,
        NextVersionProperties nextVersionProperties,
        VersionProperties versionProperties
    ) {

        Pattern releaseTagPattern = Pattern.compile("^" + tagProperties.getPrefix() + ".*");
        Pattern nextVersionTagPattern = Pattern.compile(".*" + nextVersionProperties.getSuffix() + "$");
        boolean forceSnapshot = versionProperties.isForceSnapshot();

        TagsOnCommit latestTags = repository.latestTags(releaseTagPattern);

        VersionSorter.Result currentVersionInfo = versionFromTaggedCommits(Arrays.asList(latestTags), false, nextVersionTagPattern, versionFactory, forceSnapshot);

        TagsOnCommit previousTags = latestTags;
        while (previousTags.hasOnlyMatching(nextVersionTagPattern)) {
            previousTags = repository.latestTags(releaseTagPattern, previousTags.getCommitId());
        }

        VersionSorter.Result previousVersionInfo = versionFromTaggedCommits(Arrays.asList(previousTags), true, nextVersionTagPattern, versionFactory, forceSnapshot);

        Version currentVersion = currentVersionInfo.version;
        Version previousVersion = previousVersionInfo.version;

        return new VersionInfo(
            currentVersion,
            previousVersion,
            (currentVersionInfo.isHead && !currentVersionInfo.isNextVersion),
            currentVersionInfo.isNextVersion,
            currentVersionInfo.noTagsFound
        );
    }

    private VersionInfo readVersionsByHighestVersion(VersionFactory versionFactory, final TagProperties tagProperties, final NextVersionProperties nextVersionProperties, VersionProperties versionProperties) {

        Pattern releaseTagPattern = Pattern.compile("^" + tagProperties.getPrefix() + ".*");
        Pattern nextVersionTagPattern = Pattern.compile(".*" + nextVersionProperties.getSuffix() + "$");
        boolean forceSnapshot = versionProperties.isForceSnapshot();

        List<TagsOnCommit> allTaggedCommits = repository.taggedCommits(releaseTagPattern);

        VersionSorter.Result currentVersionInfo = versionFromTaggedCommits(allTaggedCommits, false, nextVersionTagPattern, versionFactory, forceSnapshot);
        VersionSorter.Result previousVersionInfo = versionFromTaggedCommits(allTaggedCommits, true, nextVersionTagPattern, versionFactory, forceSnapshot);

        Version currentVersion = currentVersionInfo.version;
        Version previousVersion = previousVersionInfo.version;

        return new VersionInfo(
            currentVersion,
            previousVersion,
            (currentVersionInfo.isHead && !currentVersionInfo.isNextVersion),
            currentVersionInfo.isNextVersion,
            currentVersionInfo.noTagsFound
        );
    }

    private VersionSorter.Result versionFromTaggedCommits(List<TagsOnCommit> taggedCommits, boolean ignoreNextVersionTags, Pattern nextVersionTagPattern, VersionFactory versionFactory, boolean forceSnapshot) {
        return sorter.pickTaggedCommit(taggedCommits, ignoreNextVersionTags, forceSnapshot, nextVersionTagPattern, versionFactory);
    }

    private static final class VersionInfo {
        final Version current;
        final Version previous;
        final boolean onReleaseTag;
        final boolean onNextVersionTag;
        final boolean noTagsFound;

        VersionInfo(Version current, Version previous, boolean onReleaseTag, boolean onNextVersionTag, boolean noTagsFound) {
            this.current = current;
            this.previous = previous;
            this.onReleaseTag = onReleaseTag;
            this.onNextVersionTag = onNextVersionTag;
            this.noTagsFound = noTagsFound;
        }
    }
}
