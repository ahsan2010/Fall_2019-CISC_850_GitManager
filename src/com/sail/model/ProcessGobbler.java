package com.sail.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Run a process, grab all from stdout, return that.
 */
final class ProcessGobbler {
    private static final class Gobbler extends Thread {
        private final InputStream stream;
        private String contents = null;
        private boolean failed = false;

        Gobbler(InputStream stream) {
            this.stream = stream;
            start();
        }

        private String getContents() {
            if (failed)
                return null;
            
            return contents;
        }

        @Override
        public final void run() {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            final StringBuilder contents = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    contents.append(line);
                }
            } catch (IOException e) {
                failed = true;
                return;
            }
            this.contents = contents.toString();
        }
    }

    private final Process process;
    private final Gobbler stdout;
    private final Gobbler stderr;
    private Integer returnCode = null;

    ProcessGobbler(String... args) throws IOException {
        process = new ProcessBuilder(args).start();
        stdout = new Gobbler(process.getInputStream());
        stderr = new Gobbler(process.getErrorStream());
    }

    int getReturnCode() {
        try {
            returnCode = process.waitFor();
        } catch (InterruptedException e) {
            // Too bad
        }
        return returnCode;
    }

    String getStderr() {
        try {
            stderr.join();
        } catch (InterruptedException e) {
            return null;
        }
        return stderr.getContents();
    }

    String getStdout() {
        try {
            stdout.join();
        } catch (InterruptedException e) {
            return null;
        }
        return stdout.getContents();
    }
}