package pl.allegro.tech.build.axion.release.domain.scm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class TagsOnCommit {

    private final String commitId;
    private final List<String> tags;
    private final boolean isHead;

    public TagsOnCommit(String commitId, List<String> tags, boolean isHead) {
        this.commitId = commitId;
        this.tags = Collections.unmodifiableList(new ArrayList<>(tags));
        this.isHead = isHead;
    }

    public static TagsOnCommit empty() {
        return new TagsOnCommit(null, Collections.emptyList(), false);
    }

    public boolean hasOnlyMatching(Pattern pattern) {
        return !tags.isEmpty() && tags.stream().allMatch(p -> pattern.matcher(p).matches());
    }

    public String getCommitId() {
        return commitId;
    }

    public List<String> getTags() {
        return tags;
    }

    public final boolean isIsHead() {
        return isHead;
    }
}
