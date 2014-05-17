ANTFindTask
===========

ANT Task - utility to recursively search for files with a string value - GREP 
3 values are in the build.xml file that need to be edited :
 
 1. term : this is the string value, with which to search through the files found for.
 2. initialpath : this is the initial path, from with which to recursively start searching for the term.
 3. outputfile : this is the output file, where the files, and line numbers where the term is found, recursivley searching
    from the initial path is found.
    
You can also overide, whatever the property are set to, in the build.xml file, with command line arugements, eg:
ant -Dterm=foo -DinitialPath=/User/test -Doutputfile=/tmp/report.txt
