/*
 * Copyright 2024 Anton Straka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.antons.script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 *
 * @author antons
 */
public class Fs {
    public static String readTextFile(String path, String encoding) {
        try {
            return readTextFile(new FileInputStream(path), encoding);
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String readTextFile(InputStream is, String encoding) {
        if(is == null) return null;
        if(encoding == null) encoding = "utf-8";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, encoding));
            StringBuilder sb = new StringBuilder();

            String line = reader.readLine();
            while(line != null) {
                if(sb.length() > 0 ) sb.append('\n');
                sb.append(line);
                line = reader.readLine();
            }

            reader.close();

            return sb.toString();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read file data with charset '"+encoding+"'", e);
        }
    }

    public static void makeExecutable(String path) {
        if(path == null) return;
        File f = new File(path);
        if(!f.exists()) return;
        f.setExecutable(true, false);
    }

    private static void ensureParent(String filename) {
        File f = new File(filename);
        File p = f.getParentFile();
        if(!p.exists()) p.mkdirs();
    }
    public static void save(String filename, String encoding, String text) {
        if(encoding == null) encoding = "utf-8";
        try {
            ensureParent(filename);
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), encoding));
            if(text != null) writer.write(text);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to save file '"+filename+"' with charset '"+encoding+"'", e);
        }
    }

    public static void copy(String from, String to) {
        try {
            if(from == null) return;
            File f = new File(from);
            if(!f.exists()) return ;
            if(f.isDirectory()) {
                File t = new File(to);
                if(!f.exists()) t.mkdirs();
                File[] children = f.listFiles();
                if(children != null) {
                    for(File child : children) {
                        copy(child.getAbsolutePath(), to + "/" + child.getName());
                    }
                }
            } else {
                File t = new File(to);
                ensureParent(to);
                Files.copy(f.toPath(), t.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch(Exception e) {
            throw new IllegalArgumentException("Unable to copu file '"+from+"' to '"+to+"'", e);
        }
    }

}
