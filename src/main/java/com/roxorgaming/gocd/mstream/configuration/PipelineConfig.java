package com.roxorgaming.gocd.mstream.configuration;

import com.typesafe.config.Config;
import in.ashwanthkumar.utils.collections.Iterables;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Predicate;
import in.ashwanthkumar.utils.lang.StringUtils;

import java.util.*;

import static in.ashwanthkumar.utils.lang.StringUtils.isEmpty;

public class PipelineConfig extends Configuration {
    private String nameRegex;
    private String stageRegex;
    private String groupRegex;
    private Set<String> channelSet = new HashSet<>();
    private Set<String> owners = new HashSet<>();
    private Set<PipelineStatus> status = new HashSet<>();

    public PipelineConfig() {
    }

    public PipelineConfig(PipelineConfig copy) {
        this.nameRegex = copy.nameRegex;
        this.stageRegex = copy.stageRegex;
        this.groupRegex = copy.groupRegex;
        this.channelSet = copy.channelSet;
        this.status = copy.status;
        this.owners = copy.owners;
    }

    public PipelineConfig(String nameRegex, String stageRegex) {
        this.nameRegex = nameRegex;
        this.stageRegex = stageRegex;
    }

    public String getNameRegex() {
        return nameRegex;
    }

    public PipelineConfig setNameRegex(String nameRegex) {
        this.nameRegex = nameRegex;
        return this;
    }

    public String getGroupRegex() {
        return groupRegex;
    }

    public PipelineConfig setGroupRegex(String groupRegex) {
        this.groupRegex = groupRegex;
        return this;
    }

    public String getStageRegex() {
        return stageRegex;
    }

    public PipelineConfig setStageRegex(String stageRegex) {
        this.stageRegex = stageRegex;
        return this;
    }

    public Set<String> getChannel() {
        return this.channelSet;
    }

    public PipelineConfig addChannel(String channel) {
        this.channelSet.add(channel);
        return this;
    }

    public PipelineConfig setChannel(Set<String> channel) {
        this.channelSet = channel;
        return this;
    }

    public Set<PipelineStatus> getStatus() {
        return status;
    }

    public PipelineConfig setStatus(Set<PipelineStatus> status) {
        this.status = status;
        return this;
    }

    public Set<String> getOwners() {
        return owners;
    }

    public PipelineConfig setOwners(Set<String> owners) {
        this.owners = owners;
        return this;
    }

    public boolean matches(String pipeline, String stage, String group, final String pipelineState) {
        return pipeline.matches(nameRegex)
                && stage.matches(stageRegex)
                && matchesGroup(group)
                && Iterables.exists(status, hasStateMatching(pipelineState));
    }

    private boolean matchesGroup(String group) {
        return StringUtils.isEmpty(groupRegex) || group.matches(groupRegex);
    }

    private Predicate<PipelineStatus> hasStateMatching(final String pipelineState) {
        return new Predicate<PipelineStatus>() {
            @Override
            public Boolean apply(PipelineStatus input) {
                return input.matches(pipelineState);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PipelineConfig that = (PipelineConfig) o;

        if (channelSet != null ? !channelSet.equals(that.channelSet) : that.channelSet != null) return false;
        if (nameRegex != null ? !nameRegex.equals(that.nameRegex) : that.nameRegex != null) return false;
        if (groupRegex != null ? !groupRegex.equals(that.groupRegex) : that.groupRegex != null) return false;
        if (stageRegex != null ? !stageRegex.equals(that.stageRegex) : that.stageRegex != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (owners != null ? !owners.equals(that.owners) : that.owners != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = nameRegex != null ? nameRegex.hashCode() : 0;
        result = 31 * result + (groupRegex != null ? groupRegex.hashCode() : 0);
        result = 31 * result + (stageRegex != null ? stageRegex.hashCode() : 0);
        result = 31 * result + (channelSet != null ? channelSet.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (owners != null ? owners.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PipelineRule{" +
                "nameRegex='" + nameRegex + '\'' +
                ", groupRegex='" + groupRegex + '\'' +
                ", stageRegex='" + stageRegex + '\'' +
                ", channel='" + channelSet.toString() + '\'' +
                ", status=" + status +
                ", owners=" + owners +
                '}';
    }

    public static PipelineConfig fromConfig(Config config) {
        PipelineConfig pipelineConfig = new PipelineConfig();
        if(config.hasPath("name")){
            pipelineConfig.setNameRegex(config.getString("name"));
        }
        if (config.hasPath("group")) {
            pipelineConfig.setGroupRegex(config.getString("group"));
        }
        if (config.hasPath("stage")) {
            pipelineConfig.setStageRegex(config.getString("stage"));
        }
        if (config.hasPath("state")) {
            String stateT = config.getString("state");
            String[] states = stateT.split("\\|");
            Set<PipelineStatus> status = new HashSet<PipelineStatus>();
            for (String state : states) {
                status.add(PipelineStatus.valueOf(state.toUpperCase()));
            }
            pipelineConfig.setStatus(status);
        }
        if (config.hasPath("channel")) {
            String channels = config.getString("channel");
            String [] chans = channels.split("\\|");
            for(String c: chans)
                pipelineConfig.addChannel(c);
        }
        if (config.hasPath("owners")) {
            List<String> nonEmptyOwners = Lists.filter(config.getStringList("owners"), new Predicate<String>() {
                @Override
                public Boolean apply(String input) {
                    return StringUtils.isNotEmpty(input);
                }
            });
            pipelineConfig.getOwners().addAll(nonEmptyOwners);
        }

        return pipelineConfig;
    }

    public static PipelineConfig fromConfig(Config config, String channel) {
        PipelineConfig pipelineConfig = fromConfig(config);
        if (pipelineConfig.getChannel() == null || pipelineConfig.getChannel().isEmpty()) {
            pipelineConfig.addChannel(channel);
        }
        return pipelineConfig;
    }

    public static PipelineConfig merge(PipelineConfig pipelineConfig, PipelineConfig defaultRule) {
        PipelineConfig ruleToReturn = new PipelineConfig(pipelineConfig);
        if (isEmpty(pipelineConfig.getNameRegex())) {
            ruleToReturn.setNameRegex(defaultRule.getNameRegex());
        }

        if (isEmpty(pipelineConfig.getGroupRegex())) {
            ruleToReturn.setGroupRegex(defaultRule.getGroupRegex());
        }

        if (isEmpty(pipelineConfig.getStageRegex())) {
            ruleToReturn.setStageRegex(defaultRule.getStageRegex());
        }

        if (pipelineConfig.getChannel() == null || pipelineConfig.getChannel().isEmpty()) {
            ruleToReturn.setChannel(defaultRule.getChannel());
        }

        if (pipelineConfig.getStatus().isEmpty()) {
            ruleToReturn.setStatus(defaultRule.getStatus());
        } else {
            ruleToReturn.getStatus().addAll(pipelineConfig.getStatus());
        }

        if (pipelineConfig.getOwners().isEmpty()) {
            ruleToReturn.setOwners(defaultRule.getOwners());
        } else {
            ruleToReturn.getOwners().addAll(pipelineConfig.getOwners());
        }

        return ruleToReturn;
    }
}
