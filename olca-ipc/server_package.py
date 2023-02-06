# this packages the IPC server as a stand-alone server.

import os
from dataclasses import dataclass
from subprocess import call
from typing import Optional


def main():
    # call_mvn()
    pass


def call_mvn():
    mvn = "mvn" if os.name == "posix" else "mvn.cmd"
    call([mvn, "clean", "package", "-P", "server-app"])


@dataclass
class License:
    code: str
    name: str
    url: str
    matches: list["str"]

    @staticmethod
    def getall() -> list["License"]:
        return [
            License(
                "Apache2",
                "Apache License 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0.txt",
                [
                    "Apache 2",
                    "Apache 2.0",
                    "Apache License, Version 2.0",
                    "Apache-2.0",
                    "The Apache Software License, Version 2.0"
                ],
            ),
            License(
                "BSD-3",
                "BSD 3-Clause License",
                "https://raw.githubusercontent.com/s-a/license/master/bsd-3-clause.txt",
                [
                    "BSD-3-Clause",
                    "The BSD 3-Clause License",
                ]
            )
        ]


@dataclass
class LibInfo:
    licenses: list[str]
    name: str
    libid: str
    url: str

    @staticmethod
    def getall() -> list["LibInfo"]:
        infos = []
        with open("target/dist/THIRD-PARTY.txt", "r") as inp:
            for line in inp:
                l = line.strip()
                if not l.startswith("("):
                    continue
                if (info := LibInfo.parse(l)) is not None:
                    infos.append(info)
        return infos

    @staticmethod
    def parse(line: str) -> Optional["LibInfo"]:
        licenses = []
        for part in line.split(")"):
            p = part.strip()
            if p.startswith("("):
                licenses.append(p[1:])
                continue
            pp = p.split("(")
            name = pp[0].strip()
            ppp = pp[1].split(" - ")
            libid = ppp[0].strip()
            url = ppp[1].strip()
            return LibInfo(licenses, name, libid, url)

    @property
    def license(self) -> str:
        return self.licenses[0]


if __name__ == "__main__":
    s = set()
    for info in LibInfo.getall():
        s.add(info.license)

    licenses = list(s)
    licenses.sort()
    for license in licenses:
        print(license)
