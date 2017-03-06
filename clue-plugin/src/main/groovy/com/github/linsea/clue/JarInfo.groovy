package com.github.linsea.clue

public class JarInfo {

    public String original   // path and file
    public String extract   // or changed dir
    public String output    // path and file

//    original='.../app/build/intermediates/exploded-aar/.../jars/classes.jar'
//    extract='.../app/build/tmp/transformClassesWithClueTransformForDebug/xxx'
//    output='.../app/build/intermediates/transforms/ClueTransform/debug/jars/1/5/xxx.jar'}

    JarInfo(String original, String extract, String output) {
        this.original = original
        this.extract = extract
        this.output = output
    }

    @Override
    public String toString() {
        return "JarInfo{" +
                "original='" + original + "\'\n" +
                ", extract='" + extract + "\'\n" +
                ", output='" + output + "\'\n" +
                '}';
    }
}