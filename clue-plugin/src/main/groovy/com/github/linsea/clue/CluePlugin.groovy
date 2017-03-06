package com.github.linsea.clue

import org.gradle.api.Plugin
import org.gradle.api.Project;

/**
 * Transform Clue log Plugin
 */
public class CluePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        if (!hasAndroidPlugin(project)) {
            project.logger.info 'No Android plugin detecting. Skipping Clue Plugin.'
            return
        }

        //:app:transformClassesWithClueTransformForDebug
        def android = project.extensions.getByName("android")
        android.registerTransform(new ClueTransform(project))
    }

    static def hasAndroidPlugin(Project project) {
        return (project.pluginManager.hasPlugin("com.android.application")
                || project.pluginManager.hasPlugin("com.android.library"))
    }

}