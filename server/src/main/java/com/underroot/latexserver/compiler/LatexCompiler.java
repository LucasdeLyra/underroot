package com.underroot.latexserver.compiler;
//  ------ GERADO TOTALMENTE PELO GEMINI ------

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class LatexCompiler {

    public record CompilationResult(boolean success, byte[] fileBytes, String log) {}

    public CompilationResult compile(String docId, String content) {
        try {
            Path cwd = Path.of(System.getProperty("user.dir"));
            Path texFile = cwd.resolve(docId + ".tex");
            Files.writeString(texFile, content);

            ProcessBuilder pb = new ProcessBuilder(
                "pdflatex",
                "-interaction=nonstopmode",
                "-output-directory=.",
                texFile.getFileName().toString()
            );
            pb.directory(cwd.toFile());
            
            // Redirect error stream to output stream so we can read both
            pb.redirectErrorStream(true);

            Process process = pb.start();

            boolean finished = process.waitFor(30, TimeUnit.SECONDS); // 30-second timeout
            String log = new String(process.getInputStream().readAllBytes());

            if (!finished || process.exitValue() != 0) {
                System.err.println("pdflatex compilation failed for " + docId);
                System.err.println("Log: " + log);
                return new CompilationResult(false, null, log);
            }

            // 3. Read the resulting PDF from the server current directory (./)
            Path pdfFile = cwd.resolve(docId + ".pdf");
            if (Files.exists(pdfFile)) {
                byte[] pdfBytes = Files.readAllBytes(pdfFile);
                return new CompilationResult(true, pdfBytes, log);
            } else {
                return new CompilationResult(false, null, "PDF file not found after compilation.\n" + log);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Server error during compilation: " + e.getMessage());
            return new CompilationResult(false, null, "Server error during compilation: " + e.getMessage());
        }
    }
}
