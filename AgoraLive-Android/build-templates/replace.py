#!/usr/bin/python
# -*- coding: UTF-8 -*-
import re
import os

def main():
    keystore = ""
    password = ""
    alias = ""

    if "keystore" in os.environ:
        keystore = os.environ["keystore"]

    if "password" in os.environ:
        password = os.environ["password"]

    if "alias" in os.environ:
        alias = os.environ["alias"]

    f = open("./app/build.gradle", 'r+')
    content = f.read()
    contentNew = re.sub(r'azure_keystore_file', keystore, content)
    contentNew = re.sub(r'azureKeystorePassword', password, contentNew)
    contentNew = re.sub(r'azure_keystore_alias', alias, contentNew)
    f.seek(0)
    f.write(contentNew)
    f.truncate()


if __name__ == "__main__":
    main()