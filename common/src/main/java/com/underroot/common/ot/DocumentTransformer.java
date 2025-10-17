package com.underroot.common.ot;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import java.util.LinkedList;

public class DocumentTransformer {

    private final DiffMatchPatch dmp = new DiffMatchPatch();

    public String applyPatch(String content, String patch) {
        LinkedList<DiffMatchPatch.Patch> patches = (LinkedList<DiffMatchPatch.Patch>) dmp.patchFromText(patch);
        Object[] results = dmp.patchApply(patches, content);
        return (String) results[0];
    }

    public String createPatch(String oldContent, String newContent) {
        LinkedList<DiffMatchPatch.Patch> patches = dmp.patchMake(oldContent, newContent);
        return dmp.patchToText(patches);
    }
}


