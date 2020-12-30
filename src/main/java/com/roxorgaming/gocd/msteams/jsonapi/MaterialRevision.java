package com.roxorgaming.gocd.msteams.jsonapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thoughtworks.go.plugin.api.logging.Logger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRevision {

    static private final Pattern PIPELINE_REVISION_PATTERN =
            Pattern.compile("^([^/]+)/(\\d+)/.*");
    static private final Pattern GITHUB_MATERIAL_PATTERN =
            Pattern.compile("^URL: git@github\\.com:(.+)\\.git,.*");

    private Logger LOG = Logger.getLoggerFor(MaterialRevision.class);

    @JsonProperty("changed")
    private boolean changed;

    @JsonProperty("material")
    private Material material;

    @JsonProperty("modifications")
    private List<Modification> modifications;

    public MaterialRevision(boolean changed, List<Modification> modifications) {
        this.changed = changed;
        this.modifications = modifications;
    }

    public void addModification(Modification modification){
        if(this.modifications == null){
            this.modifications = new ArrayList<>();
        }
        this.modifications.add(modification);
    }

    public void addModifications(List<Modification> modificationList) {
        if(this.modifications == null){
            this.modifications = new ArrayList<>();
        }
        this.modifications.addAll(modificationList);
    }

    /**
     * Is this revision a pipeline, or something else (generally a commit
     * to a version control system)?
     */
    public boolean isPipeline() {
        return material.isPipeline();
    }

    /**
     * Return a URL pointing to more information about one of our
     * modifications, if we can figure out how to generate one.  It's an
     * error to call us with a modification that isn't part of this
     * MaterialRevision.  (This is implemented this way because
     * Modification objects are deserialized without any back-pointers to
     * the containing MaterialRevision.)
     */
    public String modificationUrl(Modification modification) {
        if (!material.getType().equals("Git") || material.getDescription() == null
                || modification.getRevision() == null) {
            LOG.info(String.format("Can't build URL for modification (%s)/(%s)/(%s)",
                    material.getType(), material.getDescription(),
                    modification.getRevision()));
            return null;
        }

        // Parse descriptions like:
        // "URL: git@github.com:faradayio/marius.git, Branch: master"
        Matcher matcher = GITHUB_MATERIAL_PATTERN.matcher(material.getDescription());
        if (!matcher.matches()) {
            LOG.info("Can't build URL for non-GitHub repo: " + material.getDescription());
            return null;
        }
        String org_and_repo = matcher.group(1);

        // Shorten our commit ID.
        String commit = modification.getRevision();
        if (commit.length() > 6)
            commit = commit.substring(0, 6);

        return "https://github.com/" + org_and_repo + "/commit/" + commit;
    }


    /**
     * Collect all changed MaterialRevision objects, walking changed
     * "Pipeline" objects recursively instead of including them directly.
     */
    void addChangesRecursively(GoCdClient goCdClient, List<MaterialRevision> outChanges)
            throws  IOException {
        // Give up now if this material hasn't changed.
        if (!changed) {
            return;
        }

        if (!isPipeline()) {
            // Add this change if somebody hasn't added it already (which
            // can happen in complex pipelines).
            if (!outChanges.contains(this))
                outChanges.add(this);
        } else {
            // Recursively walk pipeline.  We're not entirely sure what it
            // would mean to have multiple associated modifications with
            // isPipeline is true, so we walk all of them just to be on the
            // safe side.
            for (Modification m : modifications) {
                // Parse out the pipeline info.
                Matcher matcher = PIPELINE_REVISION_PATTERN.matcher(m.getRevision());
                if (matcher.matches()) {
                    String pipelineName = matcher.group(1);
                    int pipelineCounter = Integer.parseInt(matcher.group(2));

                    // Fetch the pipeline and walk it recursively.
                    Pipeline pipeline =
                            goCdClient.getPipelineInstance(pipelineName, pipelineCounter);
                    pipeline.addChangesRecursively(goCdClient, outChanges);
                } else {
                    LOG.error("Error matching pipeline revision: " + m.getRevision());
                }
            }
        }
    }

    // Override hashCode and equals with implementations generated by
    // Eclipse so we can compare MaterialRevision objects using (for
    // example) list.contains(mr).

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (changed ? 1231 : 1237);
        result = prime * result + ((material == null) ? 0 : material.hashCode());
        result = prime * result + modifications.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MaterialRevision other = (MaterialRevision) obj;
        if (changed != other.changed)
            return false;
        if (material == null) {
            if (other.material != null)
                return false;
        } else if (!material.equals(other.material))
            return false;
        if (!modifications.equals(other.modifications))
            return false;
        return true;
    }
}
