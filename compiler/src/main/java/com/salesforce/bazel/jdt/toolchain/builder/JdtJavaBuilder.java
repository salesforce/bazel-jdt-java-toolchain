// Copyright 2017 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Derived from:
// com.google.devtools.build.buildjar.VanillaJavaBuilder and
// com.google.devtools.build.buildjar.javac.JavacOptions.java

package com.salesforce.bazel.jdt.toolchain.builder;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import com.google.devtools.build.lib.worker.ProtoWorkerMessageProcessor;
import com.google.devtools.build.lib.worker.WorkRequestHandler;
import com.google.devtools.build.lib.worker.WorkRequestHandler.WorkRequestCallback;
import com.google.devtools.build.lib.worker.WorkRequestHandler.WorkRequestHandlerBuilder;
import com.google.devtools.build.lib.worker.WorkerProtocol.WorkRequest;

public class JdtJavaBuilder {

    public static void main(String[] args) {
    	JdtJavaBuilder builder = new JdtJavaBuilder();
        if (args.length == 1 && args[0].equals("--persistent_worker")) {
            WorkRequestHandler workerHandler =
                new WorkRequestHandlerBuilder(
                        new WorkRequestCallback(builder::parseAndBuild),
                        System.err,
                        new ProtoWorkerMessageProcessor(System.in, System.out))
                    .setCpuUsageBeforeGc(Duration.ofSeconds(10))
                    .build();
            try {
              workerHandler.processRequests();
            } catch (IOException e) {
              System.err.println(e.getMessage());
              System.exit(1);
            }
          } else {
            PrintWriter pw =
                new PrintWriter(new OutputStreamWriter(System.err, Charset.defaultCharset()));
            int returnCode;
            try {
              returnCode = builder.parseAndBuild(Arrays.asList(args), pw);
            } finally {
              pw.flush();
            }
            System.exit(returnCode);
          }
    }

    public int parseAndBuild(WorkRequest request, PrintWriter pw) {
    	return parseAndBuild(request.getArgumentsList(), pw);
    }

    public int parseAndBuild(List<String> args, PrintWriter pw) {
        try {
          try (BatchCompilerBuilder builder = 
                  new BatchCompilerBuilder()) {

            return build(builder, args, pw);
          }
        } catch (InvalidCommandLineException e) {
          pw.println("JdtJavaBuilder threw exception: " + e.getMessage());
          return 1;
        } catch (Exception e) {
          e.printStackTrace();
          return 1;
        }
      }
    
    /**
     * Uses {@code builder} to build the target passed in {@code arguments}. All errors and
     * diagnostics should be written to {@code err}.
     *
     * @return An error code, 0 is success, any other value is an error.
     */
    protected int build(
    		BatchCompilerBuilder builder, List<String> arguments, Writer err)
        throws Exception {
      JdtJavaBuilderResult result = builder.run(arguments);
//      for (FormattedDiagnostic d : result.diagnostics()) {
//        err.write(d.getFormatted() + "\n");
//      }
      err.write(result.output());
      return result.isOk() ? 0 : 1;
    }
 
}
