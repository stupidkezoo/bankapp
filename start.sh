#!/bin/bash
mvn compile
cat banks.txt | while read line
do
x-terminal-emulator --hold -e  mvn exec:java -Dexec.mainClass=org.kezoo.bankapp.Application "\""-Dexec.args=$line"\"" &
done
