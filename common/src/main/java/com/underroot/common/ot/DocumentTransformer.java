package com.underroot.common.ot;
//  ------ GERADO TOTALMENTE PELO GEMINI ------

import java.util.LinkedList;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

public class DocumentTransformer {

    private final DiffMatchPatch dmp = new DiffMatchPatch();

    public String applyPatch(String content, String patch) {
        LinkedList<DiffMatchPatch.Patch> patches = (LinkedList<DiffMatchPatch.Patch>) dmp.patchFromText(patch);
        Object[] results = dmp.patchApply(patches, content);
        return (String) results[0];
    }

    /**
     * Compute how much the caret should shift so it continues pointing to the same
     * logical character after applying the given patch to {@code content}.
     *
     * @param content the original document content
     * @param patchText the diff-match-patch text representation of the patch
     * @param caretPosition the current caret position (index in original content)
     * @return an integer delta to add to the caret position (may be negative)
     */
    public int computeCaretShift(String content, String patchText, int caretPosition) {
        LinkedList<DiffMatchPatch.Patch> patches = (LinkedList<DiffMatchPatch.Patch>) dmp.patchFromText(patchText);
        int shift = 0;

        for (DiffMatchPatch.Patch p : patches) {
            // start1 is the start index in the original text where this patch is applied
            int idx = p.start1;
            for (DiffMatchPatch.Diff diff : p.diffs) {
                DiffMatchPatch.Operation op = diff.operation;
                String text = diff.text;
                if (op == DiffMatchPatch.Operation.INSERT) {
                    // An insertion at idx shifts subsequent content to the right.
                    if (idx <= caretPosition) {
                        shift += text.length();
                    }
                    // insert does not advance idx through the original text
                } else if (op == DiffMatchPatch.Operation.DELETE) {
                    int delLen = text.length();
                    // If deletion starts before the caret, it may remove chars the caret was pointing to
                    if (idx < caretPosition) {
                        int affected = Math.min(delLen, caretPosition - idx);
                        shift -= affected;
                    }
                    // deletion advances idx over the removed original text
                    idx += delLen;
                } else { // EQUAL
                    idx += text.length();
                }
            }
        }

        return shift;
    }

    public String createPatch(String oldContent, String newContent) {
        LinkedList<DiffMatchPatch.Patch> patches = dmp.patchMake(oldContent, newContent);
        return dmp.patchToText(patches);
    }
}


