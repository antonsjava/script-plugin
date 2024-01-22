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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author antons
 */
public class FindMains {
    private List<String> mains = new ArrayList<>();

    private Pattern pattern = Pattern.compile("\n *public +static [^\n]* main *\\(");
    public static FindMains instance() { return new FindMains(); }

    public void find(String path) {
        File f = new File(path);
        find(f, f.getAbsolutePath());
    }
    private void find(File file, String root) {
        if(file.isFile()) {
            String name = file.getName();
            if(name.endsWith(".java")) {
                String text = Fs.readTextFile(file.getAbsolutePath(), "utf-8");
                Matcher matcher = pattern.matcher(text);
                //if(text.contains("static void main")) {
                if(matcher.find()) {
                    String pcg = file.getAbsolutePath();
                    pcg = pcg.substring(root.length()+1, pcg.length()-5);
                    pcg = pcg.replace('/', '.');
                    pcg = pcg.replace('\\', '.');
                    mains.add(pcg);
                }
            }
        } else {
            File[] children = file.listFiles();
            if(children != null) {
                for(File child : children) {
                    find(child, root);
                }
            }
        }
    }

    public List<String> mains() { return mains; }

}
