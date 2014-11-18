package de.fhwedel.opengl;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ShaderCodeLoader {

    public static String[] readSourceFile(String pathToSource) {
        String codeInOneString = "";
        try {
            List<String> lines = FileUtils.readLines(new File(pathToSource));

            for (String line : lines) {
                codeInOneString += line + "\n";
            }

//            System.out.println(String.format("content of %s:\n%s", pathToSource, codeInOneString));

        } catch (IOException e) {
            System.err.println("could not read file: " + pathToSource);
        }

        return new String[]{codeInOneString};
    }
}
