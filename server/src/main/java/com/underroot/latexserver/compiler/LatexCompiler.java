package com.underroot.latexserver.compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class LatexCompiler {

    public record CompilationResult(boolean success, byte[] fileBytes, String log) {}

    public CompilationResult compile(String docId, String content) {
        Path tempDir = null;
        try {
            // 1. Create a temporary directory for the compilation
            tempDir = Files.createTempDirectory("latex-compile-" + docId);
            Path texFile = tempDir.resolve(docId + ".tex");
            Files.writeString(texFile, content);

            // 2. Run the pdflatex command
            //    THIS IS A PLACEHOLDER. A real implementation needs robust error handling,
            //    and security checks (e.g., preventing directory traversal).
            ProcessBuilder pb = new ProcessBuilder(
                "pdflatex",
                "-interaction=nonstopmode",
                "-output-directory=" + tempDir.toAbsolutePath(),
                texFile.toAbsolutePath().toString()
            );
            
            // Redirect error stream to output stream so we can read both
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Capture the output/log
            String log = new String(process.getInputStream().readAllBytes());

            // Wait for the process to complete
            boolean finished = process.waitFor(30, TimeUnit.SECONDS); // 30-second timeout

            if (!finished || process.exitValue() != 0) {
                System.err.println("pdflatex compilation failed for " + docId);
                System.err.println("Log: " + log);
                return new CompilationResult(false, null, log);
            }

            // 3. Read the resulting PDF
            Path pdfFile = tempDir.resolve(docId + ".pdf");
            if (Files.exists(pdfFile)) {
                byte[] pdfBytes = Files.readAllBytes(pdfFile);
                return new CompilationResult(true, pdfBytes, log);
            } else {
                return new CompilationResult(false, null, "PDF file not found after compilation.\n" + log);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new CompilationResult(false, null, "Server error during compilation: " + e.getMessage());
        } finally {
            // 4. Clean up the temporary directory
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                         .map(Path::toFile)
                         .forEach(File::delete);
                } catch (IOException e) {
                    // Log cleanup failure but don't re-throw
                    System.err.println("Failed to clean up temp directory: " + tempDir);
                    e.printStackTrace();
                }
            }
        }
    }
}
