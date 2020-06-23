/**
 * Copyright 2020 Dropbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pantsbuild.jarjar;

import org.pantsbuild.jarjar.util.EntryStruct;
import org.pantsbuild.jarjar.util.JarProcessor;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class ServicesProcessor implements JarProcessor
{
    private static final String FILE_CAPTURE_GROUP = "FILE";
    private static final String SERVICES_PREFIX = "META-INF/services/";
    private static final Pattern SERVICES_FILE = Pattern.compile(
            String.format("^%s(?<%s>.+)", SERVICES_PREFIX, FILE_CAPTURE_GROUP));

    private PackageRemapper pr;

    public ServicesProcessor(PackageRemapper pr) {
        this.pr = pr;
    }

    public boolean process(EntryStruct struct) throws IOException {
        final Matcher matcher = SERVICES_FILE.matcher(struct.name);
        if (matcher.matches() && !struct.skipTransform) {
            StringBuilder sb = new StringBuilder();
            try (Stream<String> stream = Arrays.stream(new String(struct.data).split("\n"))) {
                stream.forEach(s -> {
                    final Object mp = pr.mapValue(s);
                    sb.append(mp == null ? s : mp);
                });
            }
            struct.data = sb.toString().getBytes();

            String newFileName = pr.mapValue(matcher.group(FILE_CAPTURE_GROUP)).toString();
            if (newFileName != null) {
                System.err.println("KOSKELA: Renaming " + struct.name + " to " + newFileName);
                struct.name = SERVICES_PREFIX + newFileName;
            }
        }
        return true;
    }
}
    
