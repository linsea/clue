package com.github.linsea.clue;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Transformer for Clue
 */
public class ClueTransform extends Transform {

    Project project

    public ClueTransform(Project p) {
        project = p
    }

    @Override
    public String getName() {
        return "ClueTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        Set<QualifiedContent.ContentType> set = new HashSet<>();
        set.add(QualifiedContent.DefaultContentType.CLASSES);
        return set;
    }

    @Override
    public Set<QualifiedContent.ContentType> getOutputTypes() {
        Set<QualifiedContent.ContentType> set = new HashSet<>(1);
        set.add(QualifiedContent.DefaultContentType.CLASSES);
        return set;
    }

    @Override
    public Set<QualifiedContent.Scope> getScopes() {
        Set<QualifiedContent.Scope> set = new HashSet<>();
        set.add(QualifiedContent.Scope.PROJECT);
        set.add(QualifiedContent.Scope.SUB_PROJECTS);
        return set;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        transformInvocation.context.logging.captureStandardOutput(LogLevel.INFO)

        final Logger logger = project.logger

        logger.info("ClueTransform start...")

        Set<QualifiedContent.ContentType> types = new HashSet<>()
        types.add(QualifiedContent.DefaultContentType.CLASSES)
        Set<QualifiedContent.Scope> scopes = new HashSet<>()
        scopes.add(QualifiedContent.Scope.PROJECT)
        scopes.add(QualifiedContent.Scope.SUB_PROJECTS)

        String tempRootDir = transformInvocation.context.temporaryDir.absolutePath

//        Collection<TransformInput> refInputs =
        transformInvocation.referencedInputs.each { transformInput ->
            transformInput.directoryInputs.each { directoryInput ->
                logger.info("refer input dir=" + directoryInput.file.absolutePath)
            }

            transformInput.jarInputs.each { jarInput ->
                logger.info("refer input jar=" + jarInput.file.absolutePath)
            }
        }

        List<JarInfo> jarsInfo = new ArrayList<>()

        logger.info( "\nstart")
        transformInvocation.inputs.each { transformInput ->
            logger.info( "dir_start")
            transformInput.directoryInputs.each { directoryInput ->
                String destDir = transformInvocation.outputProvider.getContentLocation("cluetrans_proj", types, scopes, Format.DIRECTORY)
                logger.info("directoryInput " + directoryInput.file.absolutePath)
                logger.info("directoryInput destDir=" + destDir)
//                logger.log LogLevel.ERROR, "111111 project.file(destDir).mkdirs()=" + (project.file(destDir).mkdirs())

                // //主module仅有输入dir,没有jar. app module:
                // inputDir=app/build/intermediates/classes/debug
                // destDir=app/build/intermediates/transforms/MLogTransform/debug/folders/1/5/cluetrans_proj
                FileUtils.copyDirectory(directoryInput.file, new File(destDir)) //copy client module classes to destDir

                //traversal destDir inject class!!!
                project.file(destDir).traverse { file ->
                    injectClass(file, file)
                }

            }
            logger.info( "dir_end")


            logger.info( "jar_start")
            transformInput.jarInputs.each { jarInput ->
                logger.info("222222 " + jarInput.file.absolutePath)
                //每个子module仅有输入jar,没有dir,如:
                // app/build/intermediates/exploded-aar/com.abc.def/framework/1.0.0-SNAPSHOT/jars/classes.jar
                //jarInput.file.absolutePath=app/build/intermediates/exploded-aar/xxx/yyy/1.0.0-SNAPSHOT/jars/classes.jar
                String extractJarDirName = jarInput.file.getParentFile().getParentFile().getParentFile().getName()  //baseapi
                logger.info("extractJarDirName=" + extractJarDirName)

                File destJar = transformInvocation.outputProvider.getContentLocation(extractJarDirName, types, scopes, Format.JAR)//output jar

//                extractJar(jarInput.file.absolutePath, destDir.absolutePath)
                if (destJar.exists()) {
                    logger.info("file:" + destJar.absolutePath + " has existed when Transform Clue!")
                }

                JarInfo info = new JarInfo(jarInput.file.absolutePath, tempRootDir + File.separator + extractJarDirName, destJar.absolutePath)
                jarsInfo.add(info)

            }

            logger.info( "jar_end")
        }

        //extract every submodule's jar to corresponding dir
        jarsInfo.each { jarInfo ->
            project.file(jarInfo.extract).mkdirs()
            project.exec {
                workingDir jarInfo.extract
                executable 'jar'
                args 'xvf', jarInfo.original
            }
        }


        logger.info("end")

        logger.info("jarsInfo start")
        logger.info(jarsInfo.toString())
        logger.info("jarsInfo end")


        //traversal destDir inject class!!!
        jarsInfo.each { info ->
            project.logger.debug("inject jar dir:" + info.extract)
            project.file(info.extract).traverse { file ->
                project.logger.debug("inject jar class:" + file.absolutePath)
                injectClass(file, file)
            }
        }

//        把父目录中的1.0.0-SNAPSHOT.jar解包到当前目录中
//        app/build/intermediates/transforms/ClueTransform/release/jars/1/5/1.0.0-SNAPSHOT$ jar xvf ../1.0.0-SNAPSHOT.jar
//        把子目录6.0.0-SNAPSHOT下的目录打包进a.jar中,创建的a.jar在当前目录
//        client/build/intermediates/transforms/ClueTransform/release/jars/1/5$ jar -cvfM a.jar  -C 1.0.0-SNAPSHOT .

//            {original='/app/build/intermediates/exploded-aar/com.yy.mobile/api/1.0.5-SNAPSHOT/jars/classes.jar'
//            extract='/app/build/tmp/transformClassesWithClueTransformForDebug/api'
//            output='/app/build/intermediates/transforms/ClueTransform/debug/jars/1/5/api.jar'}

        jarsInfo.each { info ->
            project.logger.debug("create new jar:" + info.output)
            File extractDir = project.file(info.extract)
            String parentDir = extractDir.getParent()   //...tmp/transformClassesWithClueTransformForDebug
            String classesDirName = extractDir.getName()    //api
            String newJarName = project.file(info.output).name  //api.jar
            project.exec {
                workingDir parentDir
                executable 'jar'
                args '-cvfM', newJarName, '-C', "$classesDirName", '.'
            }
            File destJar = project.file(info.output)    //...debug/jars/1/5/api.jar
            if (!destJar.parentFile.exists()) {
                destJar.parentFile.mkdirs()
            }

            File createdJar = project.file(parentDir + '/' + newJarName)
            File outputJar = project.file(info.output)
            if (outputJar.exists()) {
                outputJar.delete()
            }
            FileUtils.moveFile(createdJar, outputJar)
            project.logger.info("transformed new jar from " + createdJar.absolutePath + " to " + outputJar.absolutePath)
        }

    }

    /***
     * inject Clue class or client class that make use of Clue
     * @param inputFile Clue class file or client class
     * @param outputFile new class file
     */
    private void injectClass(File inputFile, File outputFile) {
        if (!inputFile.isDirectory()) {
            if (inputFile.name == 'Clue.class') {
                BytecodeUtil.injectClueClass(inputFile, outputFile)
            } else if (BytecodeUtil.needInject(inputFile.name)) {
                project.logger.debug("inject classfile:" + inputFile.absolutePath)
                if (BytecodeUtil.injectClass(inputFile, outputFile)) {
//                    project.logger.debug("found need inject:" + outputFile.absolutePath)
//                    CheckClassAdapter.main(new String[1]{""})
                }
            }
        }
    }

}

