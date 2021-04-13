# This is just a dev-utility script for generating the Java source code from the
# proto code. You do not need to run this when building or using this project as
# the Maven configuration will also generate the Java sources. But for editing
# and updaing the proto files it can be faster to use this script instead of
# the Maven compile cycle...
#
# In order to run this script you need to have the following tools in your path:
# * the protoc compiler:
#   https://github.com/protocolbuffers/protobuf/releases
# * the protoc-gen-grpc-java plugin:
#   https://search.maven.org/artifact/io.grpc/protoc-gen-grpc-java
#   (just download the binary from search.maven.org and put it into your path)

import os
import subprocess
import shutil


def main():
    proto_src = './src/main/proto'

    # clean the output folders
    proto_dest = './target/generated-sources/protobuf/java'
    grpc_dest = './target/generated-sources/protobuf/grpc-java'
    for d in (proto_dest, grpc_dest):
        print('prepare target folder', d)
        shutil.rmtree(d, ignore_errors=True)
        os.makedirs(d)

    # compile the sources
    for p in os.listdir(proto_src):
        proto = proto_src + '/' + p

        # message types
        print('compile message types of:', proto)
        subprocess.call([
            'protoc',
            '-I' + proto_src,
            '--java_out=' + proto_dest,
            proto])

        # services
        print('compile services of:', proto)
        subprocess.call([
            'protoc',
            '-I' + proto_src,
            '--plugin=protoc-gen-grpc-java.exe',  # Windows only currently
            '--grpc-java_out=' + grpc_dest,
            proto])


if __name__ == '__main__':
    main()
